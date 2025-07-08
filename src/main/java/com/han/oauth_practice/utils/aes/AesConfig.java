package com.han.oauth_practice.utils.aes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;

@Configuration
public class AesConfig {
    @Value("${data.aes-cipher.secret-key}")
    private String secretKey;

    @Bean
    public SecretKeySpec secretKeySpec() {
        return new SecretKeySpec(secretKey.getBytes(), "AES");
    }

    @Bean
    public AesUtil aesUtil(SecretKeySpec secretKeySpec) {
        return new AesUtil(secretKeySpec);
    }
}
