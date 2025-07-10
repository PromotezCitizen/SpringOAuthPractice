package com.han.oauth_practice.security.objects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OAuthClientInfo {
    private String provider;
    private String sub;
}
