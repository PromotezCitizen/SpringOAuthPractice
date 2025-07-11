package com.han.oauth_practice.security.controller;

import com.han.oauth_practice.security.service.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class LoginRestController {
    private final LoginService loginService;

    @PostMapping("/signout")
    public ResponseEntity<?> signout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        loginService.signOut(cookies, response);
        return ResponseEntity.noContent().build();
    }
}
