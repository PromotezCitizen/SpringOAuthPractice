# 한 계정 다중 OAuth 연동 프로젝트

## 📝 프로젝트 소개
본 프로젝트는 한 명의 사용자가 여러 소셜 로그인을 연동하여 하나의 계정으로 관리할 수 있는 기능을 구현한 토이 프로젝트입니다. 

Spring Boot와 Spring Security의 OAuth2 Client를 활용하여 핵심 로직을 개발했습니다.

## ✨ 주요 기능
- 일반 회원가입 및 로그인: 기본적인 이메일/패스워드 방식의 인증 지원
- 소셜 로그인 (OAuth 2.0): Google, Naver, Kakao 등 여러 OAuth Provider를 통한 간편 로그인 지원
- 다중 OAuth 계정 연동: 하나의 사용자 계정에 여러 소셜 계정을 연결하고 관리
- 프로필 정보 확인: 로그인된 사용자의 정보 및 연동된 소셜 계정 목록을 확인 가능

## 🛠️ 기술 스택
- Backend: Java 17, Spring Boot 3.5.3
- Security: Spring Security, Spring Security OAuth2 Client
- Database: MariaDB (with Spring Data JPA)
- Cache/Session: Custom Spring Data Store
- View: Thymeleaf
- Build Tool: Gradle

## 🚀 실행 방법

### 1. 사전 요구사항
- Java 17
- Gradle
- MariaDB

### 2. 설정
`src/main/resources/properties/` 경로에 있는 .yaml 파일들에 실제 환경에 맞는 설정값을 입력해야 합니다.


- mariadb.yaml: 데이터베이스 연결 정보
    ``` yaml
    # 환경변수에 값이 없다면 기본값 설정 
    spring:
        datasource:
            url: jdbc:mariadb://${DB_HOST:localhost}:${DB_PORT:3306}/oauthpractice
            driver-class-name: org.mariadb.jdbc.Driver
            username: ${DB_USERNAME:root}
            password: ${DB_PASSWORD:mariadb}
    ```
- oauth-provider.yaml: 각 OAuth Provider (Google, Kakao 등)에서 발급받은 Client ID 및 Secret 기입
    - kakao의 경우 master key를 넣어 renew token 진행
    - instagram의 경우 직접 구현해야 함...
    - provider를 더 추가하고 싶다면 더 추가해서 쓰면 됩니다!
- aes-data.yaml: 양방향 암호화를 위한 AES 키
    ``` yaml
    data:
        aes-cipher:
            iv-length: 16
            transformation: ${AES_TRANSFORMATION} # 이 프로젝트에서는 AES/CBC/PKCS5Padding 사용
            secret-key: ${AES_SECRET} # 16(aes-128), 24(aes-192), 32(aes-256)byte의 랜덤 문자열
    ```