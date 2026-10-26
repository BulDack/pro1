package com.example.demo.dto.login;

public class MypageDto {

    public record MyPageResponse(
            Long id,
            String loginId,
            String nickname,
            String role
    ){}
}
