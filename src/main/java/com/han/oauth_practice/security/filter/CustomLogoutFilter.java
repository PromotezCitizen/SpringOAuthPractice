package com.han.oauth_practice.security.filter;

import com.han.oauth_practice.utils.cookie.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

public class CustomLogoutFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if ("/logout".equals(request.getRequestURI()) && "POST".equals(request.getMethod())) {
            Arrays.stream(request.getCookies())
                    .filter(cookie -> "X-User-Id".equals(cookie.getName()))
                    .findFirst()
                    .ifPresent(cookie -> CookieUtil.delete(response, cookie));
        }
        filterChain.doFilter(request, response);
    }
}
