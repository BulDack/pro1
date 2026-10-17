package com.example.demo.service;

import com.example.demo.Exception.CustomException;
import com.example.demo.Exception.ErrorCode;
import com.example.demo.config.JwtTokenProvider;
import com.example.demo.dto.TokenResponseDto;
import com.example.demo.entity.Member;
import com.example.demo.entity.RefreshToken;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    //silent Refresh(access토큰이 만료되면 자동으로 refresh해서 재발급)
    @Transactional
    public TokenResponseDto reissue(String refreshToken){
        //1. JWT자체서명 및 만료시간 검증
        if(!jwtTokenProvider.validateToken(refreshToken)){
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        //2.refresh token에서 사용자 식별자(Id)pk 추출
        Long memberId =jwtTokenProvider.getId(refreshToken);

        //3.db에서 해당 사용자의 refreshToken엔티티 조회
        RefreshToken savedToken =refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(()->new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        //4.보안 핵심-RTR 및 탈취 감지
        //DB에 저장된 토큰 문자열과 클라이언트에서 보낸 토큰 문자열이 일치하지 않는 경우
        if(!savedToken.getToken().equals(refreshToken)){
            // 탈취 가능성이 있으므로 DB의 RefreshToken 정보를 즉시 삭제하여 강제 로그아웃 유도
            refreshTokenRepository.deleteByMemberId(memberId);
            throw new CustomException(ErrorCode.TOKEN_TAMPERED);
        }

        //5.[만료시간 확인] db상 토큰 만료시간이 지났는지 확인하고 지난경우
        if (savedToken.getExpiryDate().isBefore(LocalDateTime.now())){
            refreshTokenRepository.delete(savedToken);
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        Member member=memberRepository.findById(memberId)
                .orElseThrow(()->new CustomException(ErrorCode.USER_NOT_FOUND));

        //6.새로운 accessToken및 RefreshToken 생성(RTR 패턴 적용)
        String newAccessToken=jwtTokenProvider.createAccessToken(member);
        String newRefreshToken =jwtTokenProvider.createRefreshToken(member);

        //jpa 더티체킹(변경감지)를 통해 db의 토큰과 만료일 갱신
        LocalDateTime newExpiryDate=LocalDateTime.now().plusDays(7); //7일후 만료
        savedToken.updateToken(newRefreshToken,newExpiryDate);

        return new TokenResponseDto(newAccessToken,newRefreshToken);
    }
}
