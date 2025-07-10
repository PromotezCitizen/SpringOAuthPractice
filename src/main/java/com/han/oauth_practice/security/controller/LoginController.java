package com.han.oauth_practice.security.controller;

import com.han.oauth_practice.member.repository.MemberOAuthRepository;
import com.han.oauth_practice.member.repository.MemberRepository;
import com.han.oauth_practice.member.entity.Member;
import com.han.oauth_practice.security.dto.SignupDto;
import com.han.oauth_practice.security.objects.OAuthClientInfo;
import com.han.oauth_practice.utils.aes.AesUtil;
import com.han.oauth_practice.utils.cookie.CookieUtil;
import com.han.oauth_practice.utils.data_store.GlobalDataStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private final AesUtil aesUtil;
    private final MemberOAuthRepository memberOAuthRepository;
    private final MemberRepository memberRepository;
    private final GlobalDataStore globalDataStore;

    @GetMapping("/")
    public String getMainPage(Model model) {
        return "main";
    }

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        return "login";
    }

    @GetMapping("/signup")
    public String getSignupPage(Model model) {
        return "signup";
    }

    // TODO: 이미 연동된 계정이 있는 경우 조회 후 연동하도록 로그인 페이지로 이동
    @PostMapping("/signup")
    @Transactional
    public String signup(HttpServletRequest request, HttpServletResponse response, @ModelAttribute SignupDto dto) throws Exception {
        if (!dto.getPassword().equals(dto.getPasswordRe())) return "/signup?error=pass";

        Optional<Member> memberOpt = memberRepository.findByUsername(dto.getUsername());
        if (memberOpt.isPresent()) return "/signup?error=dup";

        String encryptedPassword = aesUtil.encrypt(dto.getPassword());
        Member member = new Member(dto.getUsername(), encryptedPassword);
        memberRepository.save(member);

        Arrays.stream(request.getCookies())
                .filter(cookie -> "X-User-Temp".equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> {
                    OAuthClientInfo clientInfo = (OAuthClientInfo) globalDataStore.get(cookie.getValue());
                    memberOAuthRepository.findByProviderAndSub(clientInfo.getProvider(), clientInfo.getSub())
                                    .ifPresent(memberOAuth -> {
                                        memberOAuth.setMember(member);
                                    });
                    CookieUtil.delete(response, "X-User-Temp");
                });

        return "login";
    }
}
