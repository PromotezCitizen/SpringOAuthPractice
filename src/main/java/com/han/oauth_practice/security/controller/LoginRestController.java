package com.han.oauth_practice.security.controller;

import com.han.oauth_practice.member.entity.MemberOAuth;
import com.han.oauth_practice.member.repository.MemberOAuthRepository;
import com.han.oauth_practice.member.repository.MemberRepository;
import com.han.oauth_practice.security.dto.OAuthRenewResponseDto;
import com.han.oauth_practice.security.objects.OAuth2ClientSecret;
import com.han.oauth_practice.security.objects.OAuth2UnlinkProperties;
import com.han.oauth_practice.security.objects.OAuth2UnlinkProperty;
import com.han.oauth_practice.utils.cookie.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class LoginRestController {
    private final OAuth2UnlinkProperties oAuth2UnlinkProperties;
    private final MemberOAuthRepository memberOAuthRepository;
    private final MemberRepository memberRepository;

    private final WebClient webClient;

    @PostMapping("/signout")
    public ResponseEntity<?> signout(HttpServletRequest request, HttpServletResponse response) {
        Arrays.stream(request.getCookies())
                .filter(cookie -> "X-User-Id".equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> {
                    String uid = cookie.getValue();
                    Map<String, OAuth2UnlinkProperty> propertyMap = oAuth2UnlinkProperties.getProvider();
                    Map<String, OAuth2ClientSecret> secretMap = oAuth2UnlinkProperties.getRegistration();

                    List<MemberOAuth> oAuthList = memberOAuthRepository.findByMemberId(UUID.fromString(uid));
                    List<MemberOAuth> removeOAuthList = new ArrayList<>();
                    for (MemberOAuth memberOAuth: oAuthList) {
                        String provider = memberOAuth.getProvider();
                        OAuth2UnlinkProperty property = propertyMap.get(provider);
                        OAuth2ClientSecret secret = secretMap.get(provider);
                        if (property.getUnlinkUri() == null || property.getUnlinkUri().isEmpty()) {
                            removeOAuthList.add(memberOAuth);
                            continue;
                        }
                        try {
                            unlinkOAuth(provider, property, memberOAuth.getRefreshToken(), secret, memberOAuth.getSub());
                            removeOAuthList.add(memberOAuth);
                        } catch (Exception e) {}
                    }
                    memberOAuthRepository.deleteAll(removeOAuthList);

                    CookieUtil.delete(response, cookie.getName());

                    memberRepository.deleteById(UUID.fromString(uid));
                });
        return ResponseEntity.noContent().build();
    }

    private void unlinkOAuth(String provider, OAuth2UnlinkProperty unlinkProperty, String refreshToken, OAuth2ClientSecret secret, String sub) {
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> body = new HashMap<>();
        MultiValueMap<String, String> bodyParam = new LinkedMultiValueMap<>();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(unlinkProperty.getUnlinkUri());
        String requestType = "";

        OAuthRenewResponseDto tokens = null;
        switch (provider) {
            case "kakao": // POST
                // https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#unlink'
                tokens = getNewTokens(provider, unlinkProperty.getTokenUri(), refreshToken, secret);
                headers.setContentType(new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8));
                headers.setBearerAuth(tokens.getAccessToken());
                bodyParam.add("target_id_type", "user_id");
                bodyParam.add("target_id", sub);
                requestType = "post";
                break;
            case "google": // POST
                // https://developers.google.com/identity/protocols/oauth2/javascript-implicit-flow?hl=ko#oauth-2.0-endpoints_6
                headers.setContentType(new MediaType(MediaType.APPLICATION_FORM_URLENCODED));
                bodyParam.add("token", refreshToken);
                requestType = "post";
                break;
            case "github": // DELETE
                // https://docs.github.com/ko/enterprise-cloud@latest/rest/apps/oauth-applications?apiVersion=2022-11-28
                tokens = getNewTokens(provider, unlinkProperty.getTokenUri(), refreshToken, secret);
                headers.set("Accept", "application/vnd.github+json");
                headers.setBasicAuth(secret.getClientId(), secret.getClientSecret());
                headers.add("X-GitHub-Api-Version", "2022-11-28");
                body.put("access_token", tokens.getAccessToken());
                requestType = "delete";
                break;
            default:
                break;
        }

        if (true) {
            URI uri = uriBuilder.build().toUri();

            WebClient.ResponseSpec retrieved = null;
            switch (requestType) {
                case "post":
                    WebClient.RequestBodySpec spec = webClient.post()
                            .uri(uri)
                            .headers(httpHeaders -> httpHeaders.addAll(headers))
                            .contentType(new MediaType(MediaType.APPLICATION_JSON));
                    retrieved = switch(provider) {
                        case "kakao" -> spec.body(BodyInserters.fromFormData(bodyParam)).retrieve();
                        case "google" -> spec.body(BodyInserters.fromFormData(bodyParam)).retrieve();
                        default -> null;
                    };
                    break;
                case "delete":

                    retrieved = switch (provider) {
                        case "github" -> webClient.method(HttpMethod.DELETE)
                                .uri(uri)
                                .headers(header -> header.addAll(headers))
                                .contentType(new MediaType(MediaType.APPLICATION_JSON))
                                .bodyValue(body)
                                .retrieve();
                        default -> null;
                    };
                    break;
                default:
                    break;
            }
            if (retrieved != null) retrieved
                    .bodyToMono(Object.class)
                    .block();
        }
    }

    private OAuthRenewResponseDto getNewTokens(String provider, String renewTokenUri, String refreshToken ,OAuth2ClientSecret secret) {
        HttpHeaders headers = new HttpHeaders();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", secret.getClientId());
        params.add("client_secret", secret.getClientSecret());
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", refreshToken);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(renewTokenUri);
        switch (provider) {
            case "github":
                headers.setContentType(new MediaType(MediaType.APPLICATION_FORM_URLENCODED));
                break;
            case "kakao":
                headers.setContentType(new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8));
                params.add("redirect_uri", secret.getRedirectUri());
                break;
        }

        return webClient.post()
                .uri(uriBuilder.toUriString())
                .accept(MediaType.APPLICATION_JSON)
                .headers(header -> header.addAll(headers))
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(OAuthRenewResponseDto.class)
                .block();
    }
}
