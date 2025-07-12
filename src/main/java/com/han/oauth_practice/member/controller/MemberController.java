package com.han.oauth_practice.member.controller;

import com.han.oauth_practice.member.MemberService;
import com.han.oauth_practice.member.dto.UnlinkSingleOAuthDto;
import com.han.oauth_practice.security.service.OAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final OAuthService oAuthService;
    private final MemberService memberService;

    @GetMapping
    public String getProfile(HttpServletRequest request, Model model) {
        Cookie[] cookies = request.getCookies();
        List<String> providers = oAuthService.getMemberOAuthProviders(cookies);
        model.addAttribute("providers", providers);

        List<ClientRegistration> clients = oAuthService.getOAuthRegistration();

        model.addAttribute("clients", clients);
        return "profile";
    }

    @PostMapping("/unlink/oauth")
    public String unlinkOAuth(HttpServletRequest request, @ModelAttribute UnlinkSingleOAuthDto unlinkOauthDto) {
        Arrays.stream(request.getCookies())
                .filter(cookie -> "X-User-Id".equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> {
                    memberService.unlinkOAuth(cookie, unlinkOauthDto);
                });

        return "redirect:/member";
    }
}
