package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class TokenResponseDto {

    public String accessToken;

    public String refreshToken;

    public TokenResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }

    public TokenResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
