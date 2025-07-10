package com.han.oauth_practice.security.objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OAuth2UnlinkProperty {
    private String tokenUri;
    private String unlinkUri;
}
