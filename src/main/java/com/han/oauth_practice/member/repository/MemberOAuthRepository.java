package com.han.oauth_practice.member.repository;

import com.han.oauth_practice.member.entity.Member;
import com.han.oauth_practice.member.entity.MemberOAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberOAuthRepository extends JpaRepository<MemberOAuth, Long> {
    Optional<MemberOAuth> findByProviderAndSub(String provider, String sub);
    List<MemberOAuth> findByMemberId(UUID uid);
    List<MemberOAuth> findByMember(Member member);
}
