package com.han.oauth_practice.security;

import com.han.oauth_practice.security.handler.CustomOAuth2FailureHandler;
import com.han.oauth_practice.security.handler.CustomOAuth2SuccessHandler;
import com.han.oauth_practice.security.resolver.CustomOAuth2AuthorizationRequestResolver;
import com.han.oauth_practice.security.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOAuth2AuthorizationRequestResolver authorizationRequestResolver;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2FailureHandler oauth2FailureHandler;
    private final CustomOAuth2SuccessHandler oauth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login") // <-- 인증이 필요한 경로로 들어갔을 때 리다이렉트 하는 경로
//                        .authorizationEndpoint(endpoint -> endpoint
//                                .authorizationRequestResolver(authorizationRequestResolver)
//                        )
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .failureHandler(oauth2FailureHandler)
                        .successHandler(oauth2SuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // <-- 여기로 POST가 들어오면 로그아웃 진행
                        .logoutSuccessUrl("/") // <-- 로그아웃 후 리다이렉트 하는 경로
                );

        return http.build();
    }
}
