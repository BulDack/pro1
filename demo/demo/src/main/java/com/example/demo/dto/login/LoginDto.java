package com.example.demo.dto.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

public final class LoginDto {

    @Data
    public static class RequestDto {

        @NotBlank(message = "아이디를 입력해주세요")
        private String loginId;

        @NotBlank(message = "비밀번호를 입력해주세요")
        private String password;
    }

    @Data
    @Builder
    public static class ResponseDto {

        String accessToken;
        String refreshToken;
        String username;
    }

    @Data
    @Builder
    public static class AccessResponseDto {

        String accessToken;
        String username;
//        String role; //여기서 보내는건 그냥 프론트엔드 버튼 활성화용도 정도에 불과,검증은 결국 백엔드에서
    }


}
