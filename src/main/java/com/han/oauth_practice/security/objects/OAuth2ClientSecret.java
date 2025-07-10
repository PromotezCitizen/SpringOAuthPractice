package com.han.oauth_practice.security.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2ClientSecret {
    private String clientId;
    private String clientSecret;
    private String clientMaster;
    private String redirectUri;
}
