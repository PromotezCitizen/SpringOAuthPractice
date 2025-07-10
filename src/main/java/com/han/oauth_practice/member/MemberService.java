package com.han.oauth_practice.member;

import com.han.oauth_practice.member.entity.MemberOAuth;
import com.han.oauth_practice.member.repository.MemberOAuthRepository;
import com.han.oauth_practice.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final MemberRepository memberRepository;
    private final MemberOAuthRepository memberOAuthRepository;

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
}
