package com.han.oauth_practice.member;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping
    public String getProfile(HttpServletRequest request, Model model) {
        Cookie[] cookies = request.getCookies();
        List<String> providers = memberService.getMemberOAuthProviders(cookies);
        model.addAttribute("providers", providers);

        List<ClientRegistration> clients = memberService.getOAuthRegistration();

        model.addAttribute("clients", clients);
        return "profile";
    }
}
