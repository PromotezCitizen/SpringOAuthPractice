package com.han.oauth_practice.member;

import com.han.oauth_practice.member.entity.MemberOAuth;
import com.han.oauth_practice.member.repository.MemberOAuthRepository;
import com.han.oauth_practice.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final MemberRepository memberRepository;
    private final MemberOAuthRepository memberOAuthRepository;

    @GetMapping
    public String getProfile(HttpServletRequest request, Model model) {
        Arrays.stream(request.getCookies()).filter(cookie -> "X-User-Id".equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> {
                    String uid = cookie.getValue();
                    memberRepository.findById(UUID.fromString(uid))
                            .ifPresent(member -> {
                                List<MemberOAuth> oAuths = memberOAuthRepository.findByMember(member);
                                List<String> providers = oAuths.stream().map(oauth ->
                                        oauth.getProvider() + " " + oauth.getSub() + " "
                                ).toList();
                                model.addAttribute("providers", providers);
                            });
                });

        Iterable<ClientRegistration> clientRegistrations = null;
        if (clientRegistrationRepository instanceof Iterable) {
            clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        }
        List<ClientRegistration> clients = new ArrayList<>();
        clientRegistrations.forEach(clients::add);

        model.addAttribute("clients", clients);
        return "profile";
    }
}
