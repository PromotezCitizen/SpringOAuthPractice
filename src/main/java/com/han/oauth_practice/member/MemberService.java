package com.han.oauth_practice.member;

import com.han.oauth_practice.security.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MemberService {
    private final OAuthService oAuthService;


}
