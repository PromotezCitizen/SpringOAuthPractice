package com.han.oauth_practice.member;

import com.han.oauth_practice.member.dto.UnlinkSingleOAuthDto;
import com.han.oauth_practice.member.repository.MemberOAuthRepository;
import com.han.oauth_practice.member.repository.MemberRepository;
import com.han.oauth_practice.security.service.OAuthService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class MemberService {
    private final OAuthService oAuthService;
    private final MemberOAuthRepository memberOAuthRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void unlinkOAuth(Cookie cookie, UnlinkSingleOAuthDto dto) {
        UUID uid = UUID.fromString(cookie.getValue());
        String provider = dto.getProvider().toLowerCase();

        memberOAuthRepository.findByMemberIdAndProvider(uid, provider)
                .ifPresent(memberOAuth -> {
                    oAuthService.unlinkOAuth(memberOAuth);
                    memberOAuthRepository.delete(memberOAuth);
                });
    }
}
