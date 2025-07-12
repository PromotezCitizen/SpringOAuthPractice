package com.han.oauth_practice.utils.cookie;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;

public class CookieUtil {
    @Value("${data.redirect.frontend-uri}")
    private String cookieDomain;

    private static String COOKIE_DOMAIN;

    @PostConstruct
    public void init() {
        COOKIE_DOMAIN = cookieDomain.split(":")[0];
    }

    public static void delete(HttpServletResponse response,
                              String name,
                              boolean isHttpOnly,
                              boolean isSecure) {
        Cookie cookie = createCookie(name, "", 0, isHttpOnly, isSecure);
        response.addCookie(cookie);
    }

    public static void delete(HttpServletResponse response, String name) {
        delete(response, name, false, false);
    }

    public static void delete(HttpServletResponse response, Cookie cookie) { delete(response, cookie.getName(), cookie.isHttpOnly(), cookie.getSecure()); }

    public static void add(HttpServletResponse response,
                           String name,
                           String value,
                           int maxAge,
                           boolean isHttpOnly,
                           boolean isSecure) {
        Cookie cookie = createCookie(name, value, maxAge, isHttpOnly, isSecure);
        response.addCookie(cookie);
    }

    public static void add(HttpServletResponse response,
                           String name,
                           String value,
                           boolean isHttpOnly,
                           boolean isSecure) {
        add(response, name, value, -1, isHttpOnly, isSecure);
    }

    public static void add(HttpServletResponse response,
                           String name,
                           String value,
                           int maxAge) {
        add(response, name, value, maxAge, false, false);
    }

    public static void add(HttpServletResponse response, String name, String value) {
        add(response, name, value, -1,false, false);
    }

    private static Cookie createCookie(String name,
                                       String value,
                                       int maxAge,
                                       boolean isHttpOnly,
                                       boolean isSecure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(isHttpOnly);
        cookie.setSecure(isSecure);
        return cookie;
    }
}
