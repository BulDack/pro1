package com.example.demo.web.controller;

import com.example.demo.Exception.CustomException;
import com.example.demo.Exception.ErrorCode;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.TokenResponseDto;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //silentRefresh방식을 통해 refresh토큰 재발급
    @PostMapping("api/auth/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDto>>reissue(
            @CookieValue(name = "refresh_token",required=false) String refreshToken,
            HttpServletResponse response){

        // 1.요청쿠키에 refreshToken 존재여부 검증
        if (refreshToken ==null || refreshToken.isBlank()){
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        //2.DB 검증 및 RTR 토큰 재발급 로직 실행
        TokenResponseDto tokenResponse=authService.reissue(refreshToken);

        //3.RTR로 교체된 새 RefreshToken을 httpOnly 쿠키에 다시 세팅
        ResponseCookie cookie=ResponseCookie.from("refresh_token",tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false) // HTTPS 환경 필수 (로컬 테스트 시 false로 조정 가능)
                .path("/")
                .maxAge(7*24*60*60) //쿠키 유효기간 7일
                .sameSite("Lax") //csrf공격 방어
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());

        //4.새 AccessToken은 ResponseBody(ApiResponse)로 반환
        return ResponseEntity.ok(ApiResponse.success("refresh성공",
                new TokenResponseDto(tokenResponse.getAccessToken())
        ));
    }

    //HTTP Status 401 감지 후 재발급 (Axios Interceptor 활용) — 가장 일반적
    //Access Token이 만료된 상태에서 API 요청을 보냈을 때 백엔드가 401 Unauthorized를 응답하면,
    //Axios 인터셉터가 이를 낚아채서 토큰을 재발급받은 뒤 기존 요청을 재시도(Retry)하는 방식

    //[React App]                       [Spring Boot Backend]
    //    |                                       |
    //    |---- 1. API 요청 (만료된 AccessToken) -->|
    //    |<--- 2. 401 Unauthorized 응답 ---------|  (토큰 만료 확인)
    //    |                                       |
    //    |---- 3. POST /api/v1/auth/refresh ---->|  (쿠키의 RefreshToken 자동 첨부)
    //    |<--- 4. 새 AccessToken 반환 -----------|  (RefreshToken 검증 및 RTR 적용)
    //    |                                       |
    //    |---- 5. 실패했던 1번 API 재요청 ------>|  (새 AccessToken 첨부)
    //    |<--- 6. 정상 응답 (200 OK) ------------|

}
