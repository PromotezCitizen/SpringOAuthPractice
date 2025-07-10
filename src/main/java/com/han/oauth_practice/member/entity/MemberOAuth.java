package com.han.oauth_practice.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        name = "member_oauth",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "sub"})
        },
        indexes = {
                @Index(name = "idx_member_entity_id", columnList = "member_entity_id")
        }
)
@NoArgsConstructor
public class MemberOAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String provider;
    @Column(nullable = false)
    private String sub;
    private String refreshToken;

    @ManyToOne
    @JoinColumn(name = "member_entity_id")
    private Member member;

    public MemberOAuth(String provider, String sub, String refreshToken) {
        this.provider = provider;
        this.sub = sub;
        this.refreshToken = refreshToken;
    }
}
