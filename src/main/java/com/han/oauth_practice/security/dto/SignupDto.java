package com.han.oauth_practice.security.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SignupDto {
    private String username;
    private String password;
    private String passwordRe;
}
