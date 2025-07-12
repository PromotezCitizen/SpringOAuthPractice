package com.han.oauth_practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OauthPracticeApplication {

	public static void main(String[] args) {
		SpringApplication.run(OauthPracticeApplication.class, args);
	}

}
