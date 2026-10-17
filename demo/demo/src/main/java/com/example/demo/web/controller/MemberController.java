package com.example.demo.web.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JoinRequestDto;
import com.example.demo.dto.login.LoginDto;
import com.example.demo.config.oauth.dto.PrincipalDetails;
import com.example.demo.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MemberController {

    private final MemberService memberService;

    //회원가입 api
    //ResponseEntity는 화면에 보여줄 데이터가 없을 때만 쓸 수 있음,
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Void>>join(@RequestBody @Valid JoinRequestDto requestDto){
        memberService.joinMember(requestDto);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
    }

    //로그인 api
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginDto.AccessResponseDto>>login(@RequestBody LoginDto.RequestDto requestDto
                                                     ){

        // 1.서비스에서 로그인 로직 수행 후 토큰 및 회원 정보 생성
        LoginDto.ResponseDto responseDto=memberService.login(requestDto);

        // 2. 서버측에서 refreshToken을 담은 httpOnly 쿠키 생성
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", responseDto.getRefreshToken())
                .httpOnly(true)                  // JS 접근 차단 (XSS 방어)
                .secure(true)                    // HTTPS 환경 전송 (로컬 개발 환경 상황에 맞게 설정)
                .path("/")                       // 모든 URL 요청 시 쿠키 전송
                .maxAge(7 * 24 * 60 * 60)        // 유효기간 7일
                .sameSite("Lax")                // React - Spring Boot CROS 환경 시 "None" (동일 오리진 시 "Lax")
                .build();


        // Access Token만 담은 깔끔한 DTO로 프론트엔드에게 응답 바디 전달
        LoginDto.AccessResponseDto responseBody= LoginDto.AccessResponseDto.builder()
                .accessToken(responseDto.getAccessToken())
                .username(responseDto.getUsername())
                .build();

        //Set-Cookie 헤더와 함께 200 OK 응답 반환 ()
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,refreshTokenCookie.toString())
                .body(ApiResponse.success("로그인이 완료되었습니다.",responseBody));
    }

    //로그아웃 api

    //로그아웃할 때는 DB/Redis에서 토큰을 지우는 것뿐만 아니라, 브라우저에 남아있는 쿠키도 같이 만료(삭제)시켜 주어야 함
    //쿠키를 지우는 방법은 의외로 간단한데, 유효기간(MaxAge)을 0으로 세팅한 똑같은 이름의 빈 쿠키를 응답으로 덮어씌워 보내주면 브라우저가 알아서 쿠키를 증발시킴.
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                       @CookieValue(value = "refresh_token", required = false) String refreshToken
                                         ){
        // 1.서비스단에서 우리db의 refreshToken 삭제
        //로그인한 상태(Authentication 존재)이거나 쿠키에 RefreshToken이 존재하는 경우
        if(principalDetails !=null && refreshToken !=null){
            memberService.logout(refreshToken,principalDetails.getId());
        }

        // 2. ⭐️ 브라우저의 쿠키를 지우기 위해 만료시간이 0인 쿠키를 밀어 넣음
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token","")
                .httpOnly(true)
                .secure(false) // ⚠️ 운영 환경(HTTPS)인 경우 true, 로컬 HTTP 개발 시 false로 세팅 (설정파일 분리 권장)
                .path("/")
                .maxAge(0) // 즉시 만료 (삭제)
                .sameSite("Lax") // ⚠️ React - Spring Boot 크로스 오리진 환경인 경우 "None", 동일 오리진이면 "Lax"
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,deleteCookie.toString())
                .body(ApiResponse.success("로그아웃이 완료되었습니다."));
    }
}
