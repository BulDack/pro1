package com.example.demo.dto.login;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogoutDto {

    private String refreshToken;
}
