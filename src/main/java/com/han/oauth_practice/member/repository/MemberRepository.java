package com.han.oauth_practice.member.repository;

import com.han.oauth_practice.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByUsername(String username);
}
