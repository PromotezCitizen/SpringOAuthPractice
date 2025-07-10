package com.han.oauth_practice.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "member",
        indexes = {
                @Index(name = "idx_username", columnList = "username")
        }
)
@NoArgsConstructor
@ToString
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberOAuth> oauthAccounts;

    public Member(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = MemberRole.USER;
        this.oauthAccounts = new ArrayList<>();
    }
}
