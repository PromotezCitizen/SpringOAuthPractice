package com.han.oauth_practice.security.service;

import com.han.oauth_practice.member.entity.MemberOAuth;
import com.han.oauth_practice.member.repository.MemberOAuthRepository;
import com.han.oauth_practice.member.repository.MemberRepository;
import com.han.oauth_practice.security.dto.OAuthRenewResponseDto;
import com.han.oauth_practice.security.objects.OAuth2ClientSecret;
import com.han.oauth_practice.security.objects.OAuth2UnlinkProperties;
import com.han.oauth_practice.security.objects.OAuth2UnlinkProperty;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OAuthService {
    private final WebClient webClient;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final MemberRepository memberRepository;
    private final MemberOAuthRepository memberOAuthRepository;
    private final OAuth2UnlinkProperties oAuth2UnlinkProperties;

    public void unlinkOAuth(String provider, OAuth2UnlinkProperty unlinkProperty, String refreshToken, OAuth2ClientSecret secret, String sub) {
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> body = new HashMap<>();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(unlinkProperty.getUnlinkUri());
        setParamsAndHeader(
                headers,
                body,
                form,
                provider,
                unlinkProperty.getTokenUri(),
                refreshToken,
                secret,
                sub
        );

        HttpMethod requestHttpMethod = switch (provider) {
            case "kakao", "google" -> HttpMethod.POST;
            case "github" -> HttpMethod.DELETE;
            default -> HttpMethod.POST;
        };

        URI uri = uriBuilder.build().toUri();
        WebClient.RequestBodySpec spec = webClient.method(requestHttpMethod)
                .uri(uri)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .contentType(new MediaType(MediaType.APPLICATION_JSON));
        WebClient.ResponseSpec retrieved = switch(provider) {
            case "kakao", "google" -> spec.body(BodyInserters.fromFormData(form)).retrieve();
            case "github" -> spec.bodyValue(body).retrieve();
            default -> null;
        };
        if (retrieved != null) {
            retrieved.bodyToMono(Object.class).block();
        }
    }

    private void setParamsAndHeader(HttpHeaders headers,
                                     Map<String, String> body,
                                     MultiValueMap<String, String> form,
                                     String provider,
                                     String tokenUri,
                                     String refreshToken,
                                     OAuth2ClientSecret secret,
                                     String sub) {
        OAuthRenewResponseDto tokens;
        switch (provider) {
            case "kakao": // POST
                // https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#unlink'
                tokens = getNewTokens(provider, tokenUri, refreshToken, secret);
                headers.setContentType(new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8));
                headers.setBearerAuth(tokens.getAccessToken());
                form.add("target_id_type", "user_id");
                form.add("target_id", sub);
                break;
            case "google": // POST
                // https://developers.google.com/identity/protocols/oauth2/javascript-implicit-flow?hl=ko#oauth-2.0-endpoints_6
                headers.setContentType(new MediaType(MediaType.APPLICATION_FORM_URLENCODED));
                form.add("token", refreshToken);
                break;
            case "github": // DELETE
                // https://docs.github.com/ko/enterprise-cloud@latest/rest/apps/oauth-applications?apiVersion=2022-11-28
                tokens = getNewTokens(provider, tokenUri, refreshToken, secret);
                headers.set("Accept", "application/vnd.github+json");
                headers.setBasicAuth(secret.getClientId(), secret.getClientSecret());
                headers.add("X-GitHub-Api-Version", "2022-11-28");
                body.put("access_token", tokens.getAccessToken());
                break;
            default:
                break;
        }
    }

    private OAuthRenewResponseDto getNewTokens(String provider, String renewTokenUri, String refreshToken , OAuth2ClientSecret secret) {
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

    public List<String> getMemberOAuthProviders(Cookie[] cookies) {
        return Arrays.stream(cookies).filter(cookie -> "X-User-Id".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .flatMap(uidString -> Optional.of(UUID.fromString(uidString)))
                .flatMap(memberRepository::findById)
                .map(memberOAuthRepository::findByMember)
                .map(oAuths -> oAuths.stream().map(MemberOAuth::getProvider).toList())
                .orElseGet(ArrayList::new);
    }

    public List<ClientRegistration> getOAuthRegistration() {
        Iterable<ClientRegistration> clientRegistrations = null;
        if (clientRegistrationRepository instanceof Iterable) {
            clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        }
        List<ClientRegistration> clients = new ArrayList<>();
        clientRegistrations.forEach(clients::add);

        return clients;
    }

    public Map<String, OAuth2UnlinkProperty> getTokenUriMap() { return  oAuth2UnlinkProperties.getProvider(); }
    public Map<String, OAuth2ClientSecret> getOAuthSecretMap() { return oAuth2UnlinkProperties.getRegistration(); }
}
