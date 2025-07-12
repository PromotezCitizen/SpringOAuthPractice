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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final MemberOAuthRepository memberOAuthRepository;
    private final MemberRepository memberRepository;
    private final GlobalDataStore globalDataStore;

    @Value("${data.redirect.frontend-uri}")
    private String frontendUri;
    @Value("${data.redirect.frontend-protocol}")
    private String frontendProtocol;

    @Override
    @Transactional
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
                            try {
                                ifMemberOAuthisPresent(oauth, response, registrationId, sub, tempUUID, refreshToken);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        () -> {
                            Cookie[] cookies = request.getCookies();
                            assert refreshToken != null; // 첫 oauth 로그인에는 무조건 refresh token이 온다.
                            try {
                                ifMemberOAuthisNotPresent(cookies, response, registrationId, sub, tempUUID, refreshToken);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
    }

    @Transactional
    private void ifMemberOAuthisPresent(MemberOAuth memberOAuth,
                                        HttpServletResponse response,
                                        String registrationId,
                                        String sub,
                                        UUID tempUUID,
                                        OAuth2RefreshToken refreshToken) throws IOException {
        Member member = memberOAuth.getMember();
        if (refreshToken != null) memberOAuth.setRefreshToken(refreshToken.getTokenValue());

        if (member == null) { // 연동 안됨
            setTempInfo(response, tempUUID, registrationId, sub);
            response.sendRedirect(getFrontendUri("/signup"));
        } else { // 연동 됨
            CookieUtil.add(response, "X-User-Id", member.getId().toString(), 60 * 60 * 24, true, true);
            response.sendRedirect(getFrontendUri("/member"));
        }
    }

    @Transactional
    private void ifMemberOAuthisNotPresent(Cookie[] cookies,
                                           HttpServletResponse response,
                                           String registrationId,
                                           String sub,
                                           UUID tempUUID,
                                           OAuth2RefreshToken refreshToken) throws IOException {
        MemberOAuth memberOAuth = new MemberOAuth(registrationId, sub, refreshToken.getTokenValue());

        Optional<Cookie> cookieOpt = Arrays.stream(cookies).filter(cookie -> "X-User-Id".equals(cookie.getName()))
                .findFirst();
        if (cookieOpt.isPresent()) {
            String uid = cookieOpt.get().getValue();
            memberRepository.findById(UUID.fromString(uid)).ifPresent(memberOAuth::setMember);
            response.sendRedirect(getFrontendUri("/member"));
        } else {
            setTempInfo(response, tempUUID, registrationId, sub);
            response.sendRedirect(getFrontendUri("/signup"));
        }
        memberOAuthRepository.save(memberOAuth);
    }

    private void setTempInfo(HttpServletResponse response, UUID tempUUID, String registrationId, String sub) {
        CookieUtil.add(response, "X-User-Temp", tempUUID.toString(), 60 * 10, true, true);
        OAuthClientInfo info = OAuthClientInfo.builder()
                .provider(registrationId)
                .sub(sub)
                .build();
        globalDataStore.put(tempUUID.toString(), info);
    }

    @Cacheable
    private String getFrontendUri(String subUri) {
        return frontendProtocol + "://" + frontendUri + subUri;
    }
}
