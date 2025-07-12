package com.han.oauth_practice.security.filter;

import com.han.oauth_practice.member.repository.MemberRepository;
import com.han.oauth_practice.member.entity.Member;
import com.han.oauth_practice.security.service.OAuthService;
import com.han.oauth_practice.utils.aes.AesUtil;
import com.han.oauth_practice.utils.cookie.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomUsernamePasswordAuthenticationFilter extends OncePerRequestFilter {
    private final AesUtil aesUtil;
    private final MemberRepository memberRepository;
    private final OAuthService oAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if ("/login".equals(request.getRequestURI()) && HttpMethod.POST.toString().equals(request.getMethod())) {

            String username = request.getParameter("username");
            String password = request.getParameter("password");

            Optional<Member> memberOpt = memberRepository.findByUsername(username);
            if (memberOpt.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }
            Member member = memberOpt.get();
            String encryptedPassword = member.getPassword();

            String decryptedPassword;
            try {
                decryptedPassword = aesUtil.decrypt(encryptedPassword);
                if (!decryptedPassword.equals(password)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            oAuthService.linkOAuth(request.getCookies(), member, response);

            UserDetails userDetails = User.builder()
                    .username(username)
                    .password(decryptedPassword)
                    .roles(String.valueOf(member.getRole()))
                    .build();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            CookieUtil.add(response, "X-User-Id", member.getId().toString(), 60 * 60 * 24, true, true);
            response.sendRedirect("/");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
