package com.example.demo.config;

import com.example.demo.dto.security.PrincipalDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //1.로그인에 성공한 유저 정보(PrincipleDetails)꺼내기
        PrincipalDetails principalDetails=(PrincipalDetails) authentication.getPrincipal();

        //2.jwt토큰 생성하기
        String accessToken= jwtTokenProvider.createAccessToken(
                principalDetails.getId(),
                principalDetails.getUsername(),
                principalDetails.getRole()
        );

        //3.클라이언트 페이지로 토큰을 들고 리다이렉트
        // (예: 프론트엔드가 주소창의 쿼리 파라미터에서 토큰을 파싱해서 로컬스토리지에 저장하도록 함)
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("token", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
