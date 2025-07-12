package com.han.oauth_practice.runner;

import com.han.oauth_practice.utils.cookie.CookieUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CookieDomainInitializerRunner implements ApplicationRunner {
    @Value("${data.redirect.frontend-uri}")
    private String cookieDomain;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        CookieUtil.setCookieDomain(cookieDomain);
    }
}
