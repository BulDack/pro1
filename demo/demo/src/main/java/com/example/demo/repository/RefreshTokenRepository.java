package com.example.demo.repository;

import com.example.demo.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {


    //토큰값으로 조회(재발급및 유효성 검증용
    Optional<RefreshToken> findByToken(String token);

    //로그아웃시 회원 pk로 토큰삭제
    void deleteByMemberId(Long memberId);

    // ⭐️ 정확히 토큰 값과 회원 PK가 매칭되는 데이터 행 하나만 타겟팅해서 삭제
    // 다중 로그인 환경일때 필요(아래와 같은 상태일테니까)
    //[RefreshToken 테이블 상태]
    //1. id: 101 | memberId: 5 (노트북 토큰)
    //2. id: 102 | memberId: 5 (스마트폰 토큰)
    void deleteByTokenAndMemberId(String token,Long memberId);

    //RTR 보안 침해 감지 시 해당 회원의 모든 토큰 일괄 삭제 (강제 로그아웃)
    void deleteAllByMemberId(Long memberId);
}
