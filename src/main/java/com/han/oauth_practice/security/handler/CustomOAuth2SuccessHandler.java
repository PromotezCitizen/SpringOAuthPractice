package com.han.oauth_practice.security.handler;

import com.han.oauth_practice.member.entity.Member;
import com.han.oauth_practice.member.entity.MemberOAuth;
import com.han.oauth_practice.member.repository.MemberOAuthRepository;
import com.han.oauth_practice.member.repository.MemberRepository;
import com.han.oauth_practice.security.objects.OAuthClientInfo;
import com.han.oauth_practice.utils.cookie.CookieUtil;
import com.han.oauth_practice.utils.data_store.GlobalDataStore;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final MemberOAuthRepository memberOAuthRepository;
    private final MemberRepository memberRepository;
    private final GlobalDataStore globalDataStore;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;

        String registrationId = authenticationToken.getAuthorizedClientRegistrationId();
        String sub = authentication.getName();

        OAuth2AuthorizedClient authorizedClient = authorizedClientRepository.loadAuthorizedClient(
                registrationId,
                authenticationToken,
                request
        );

        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

        UUID tempUUID = UUID.randomUUID();
        memberOAuthRepository.findByProviderAndSub(registrationId, authenticationToken.getName())
                .ifPresentOrElse(
                        oauth -> {
                            Member member = oauth.getMember();
                            if (member == null) { // 연동 안됨
                                setTempInfo(response, tempUUID, registrationId, sub);
                                try {
                                    response.sendRedirect("http://localhost:8080/signup");
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else { // 연동 됨
                                CookieUtil.add(response, "X-User-Id", member.getId().toString());
                                try {
                                    response.sendRedirect("http://localhost:8080");
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        },
                        () -> {
                            assert refreshToken != null;
                            MemberOAuth memberOAuth = new MemberOAuth(registrationId, sub, refreshToken.getTokenValue());

                            Cookie[] cookies = request.getCookies();
                            Arrays.stream(cookies).filter(cookie -> "X-User-Id".equals(cookie.getName()))
                                    .findFirst()
                                    .ifPresentOrElse(
                                            cookie -> {
                                                String uid = cookie.getValue();
                                                memberRepository.findById(UUID.fromString(uid)).ifPresent(memberOAuth::setMember);
                                                try {
                                                    response.sendRedirect("http://localhost:8080");
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            },
                                            () -> {
                                                setTempInfo(response, tempUUID, registrationId, sub);
                                                try {
                                                    response.sendRedirect("http://localhost:8080/signup");
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                    );
                            memberOAuthRepository.save(memberOAuth);
                        }
                );
    }

    private void setTempInfo(HttpServletResponse response, UUID tempUUID, String registrationId, String sub) {
        CookieUtil.add(response, "X-User-Temp", tempUUID.toString(), 60 * 10);
        OAuthClientInfo info = OAuthClientInfo.builder()
                .provider(registrationId)
                .sub(sub)
                .build();
        globalDataStore.put(tempUUID.toString(), info);
    }
}
