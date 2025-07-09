package com.han.oauth_practice.security.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final RestTemplate restTemplate;
    private final DefaultOAuth2UserService delegate;
    private final List<String> registrationIds;

    public CustomOAuth2UserService() {
        restTemplate = new RestTemplate();
        delegate = new DefaultOAuth2UserService();
        registrationIds = new ArrayList<>();
        registrationIds.add("naver");
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        ClientRegistration client = userRequest.getClientRegistration();
        String registrationId = client.getRegistrationId();
        Map<String, Object> attrs;
        String nameAttributeKey = client.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        if (registrationIds.contains(registrationId)) {
            String userInfoUri = client.getProviderDetails().getUserInfoEndpoint().getUri();
            String accessToken = userRequest.getAccessToken().getTokenValue();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            attrs = response.getBody();

            if ("naver".equals(registrationId)) attrs = (Map<String, Object>) attrs.get("response");
        } else {
            OAuth2User loadedUser = delegate.loadUser(userRequest);
            attrs = loadedUser.getAttributes();
        }

        assert attrs != null;
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attrs,
                nameAttributeKey
        );
    }
}
