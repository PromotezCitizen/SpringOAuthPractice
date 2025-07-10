package com.han.oauth_practice.security.objects;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
@Getter
@Setter
public class OAuth2UnlinkProperties {
    private Map<String, OAuth2ClientSecret> registration;
    private Map<String, OAuth2UnlinkProperty> provider;
}
