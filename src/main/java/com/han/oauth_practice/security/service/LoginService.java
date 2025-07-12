package com.han.oauth_practice.security.service;

import com.han.oauth_practice.member.entity.Member;
import com.han.oauth_practice.member.entity.MemberOAuth;
import com.han.oauth_practice.member.repository.MemberOAuthRepository;
import com.han.oauth_practice.member.repository.MemberRepository;
import com.han.oauth_practice.security.dto.SignupDto;
import com.han.oauth_practice.security.objects.OAuthClientInfo;
import com.han.oauth_practice.utils.aes.AesUtil;
import com.han.oauth_practice.utils.cookie.CookieUtil;
import com.han.oauth_practice.utils.data_store.GlobalDataStore;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final AesUtil aesUtil;
    private final MemberRepository memberRepository;
    private final MemberOAuthRepository memberOAuthRepository;
    private final GlobalDataStore globalDataStore;
    private final OAuthService oAuthService;

    public String signupForm(Cookie[] cookies, SignupDto dto, HttpServletResponse servletResponse) throws Exception {
        Optional<Member> memberOpt = memberRepository.findByUsername(dto.getUsername());
        if (memberOpt.isPresent()) return "signup?error=dup";

        signup(cookies, dto, servletResponse);
        return "login";
    }

    public void signupApi(Cookie[] cookies, SignupDto dto, HttpServletResponse servletResponse) throws Exception {
        Optional<Member> memberOpt = memberRepository.findByUsername(dto.getUsername());
        if (memberOpt.isPresent()) return;

        signup(cookies, dto, servletResponse);

    }

    @Transactional
    private void signup(Cookie[] cookies, SignupDto dto, HttpServletResponse servletResponse) throws Exception {
        String encryptedPassword = aesUtil.encrypt(dto.getPassword());
        Member member = new Member(dto.getUsername(), encryptedPassword);
        memberRepository.save(member);

        Arrays.stream(cookies)
                .filter(cookie -> "X-User-Temp".equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> {
                    OAuthClientInfo clientInfo = (OAuthClientInfo) globalDataStore.get(cookie.getValue());
                    memberOAuthRepository.findByProviderAndSub(clientInfo.getProvider(), clientInfo.getSub())
                            .ifPresent(memberOAuth -> {
                                memberOAuth.setMember(member);
                            });
                    CookieUtil.delete(servletResponse, cookie);
                });
    }

    @Transactional
    public void signOut(Cookie[] cookies, HttpServletResponse response) {
        Arrays.stream(cookies)
                .filter(cookie -> "X-User-Id".equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> {
                    String uid = cookie.getValue();

                    List<MemberOAuth> oAuthList = memberOAuthRepository.findByMemberId(UUID.fromString(uid));
                    for (MemberOAuth memberOAuth: oAuthList) oAuthService.unlinkOAuth(memberOAuth);

                    CookieUtil.delete(response, cookie);
                    memberRepository.deleteById(UUID.fromString(uid));
                });
    }
}
