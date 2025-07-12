package com.han.oauth_practice.security.controller;

import com.han.oauth_practice.security.dto.SignupDto;
import com.han.oauth_practice.security.service.LoginService;
import com.han.oauth_practice.security.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;
    private final OAuthService oAuthService;

    @GetMapping("/")
    public String getMainPage(Model model) {
        return "main";
    }

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        List<ClientRegistration> clients = oAuthService.getOAuthRegistration();
        model.addAttribute("clients", clients);

        return "login";
    }

    @GetMapping("/signup")
    public String getSignupPage(Model model) {
        return "signup";
    }

    // TODO: 이미 연동된 계정이 있는 경우 조회 후 연동하도록 로그인 페이지로 이동
    @PostMapping("/signup")
    @Transactional
    public String signup(HttpServletRequest servletRequest, HttpServletResponse servletResponse, @ModelAttribute SignupDto dto, Model model) throws Exception {
        if (!dto.getPassword().equals(dto.getPasswordRe())) {
            model.addAttribute("error", "pw");
            return "signup";
        }

        String url = loginService.signupForm(servletRequest.getCookies(), dto, servletResponse);
        String[] splitted = url.split("\\?");
        if (splitted.length > 1) { // error 발생
            String[] query = splitted[1].split("=");
            model.addAttribute(query[0], query[1]);
            return splitted[0];
        }

        return url;
    }
}
