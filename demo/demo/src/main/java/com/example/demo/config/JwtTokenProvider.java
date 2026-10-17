package com.example.demo.config;

import com.example.demo.config.oauth.dto.TokenMemberDto;
import com.example.demo.entity.Member;
import com.example.demo.entity.ennum.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

//이 클래스는 JWT 토큰의 생성, 파싱(검증), 만료 시간 확인 등 토큰에 관련된 모든 지저분한 로직을 한데 모아 처리하는 일종의 '유틸리티 컴포넌트' 역할
/**
 * JWT 생성과 검증, 회원 정보 및 만료 시각 추출을 담당합니다.
 *
 * 주요 claim 구분:
 * - sub: 로그인 아이디
 * - id: 회원의 DB 기본키
 * - role: 회원 권한 — Access Token에만 저장
 * - jti: 토큰 자체의 고유번호 — 회원 ID와 별개
 * - exp: 토큰 만료 시각
 */
@Component
public class JwtTokenProvider {

    // 💡 application.properties 또는 yml 파일에 등록한 비밀키와 만료시간을 가져옴

    private final String secretKeyString; //비밀키 만들 문자열
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;
    private SecretKey secretKey; //비밀키객체

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKeyString,
            @Value("${jwt.access-token-expiration-time}") long accessTokenExpirationTime,
            @Value("${jwt.refresh-token-expiration-time}") long refreshTokenExpirationTime) {
        this.secretKeyString = secretKeyString;
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    // 객체 생성 후 물리적인 SecretKey 객체로 변환해 둡니다.
    @PostConstruct
    protected void init(){

        this.secretKey= Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    //access token 생성

    /**
     * 방법 A: 개별 필드로 토큰 생성
     * (소셜 로그인 Success Handler나 DTO 기반 일반 로그인에서 쓰기 좋습니다!)
     * 굳이 member엔티티를 만들필요 없는 곳에서 쓰기좋아서 오버로딩했음!!
     */
    public String createAccessToken(Long id, String loginId, Role role) {
        Claims claims = Jwts.claims()
                .subject(loginId)
                .add("id", id)
                .add("role", role.name())
                .build();

        return createToken(claims,accessTokenExpirationTime);

    }
    //매개변수가 TokenMemberDto인 버전
    public String createAccessToken(TokenMemberDto dto){

        return createAccessToken(dto.getId(),
                dto.getMemberId(),
                Role.valueOf(String.valueOf(dto.getRole())));

    }

    public String createAccessToken(Member member){
        return createAccessToken(
                member.getId(),
                member.getLoginId(),
                member.getRole()
        );
    }
    //1. 📦 Payload (페이로드)란?
    //쉽게 말해 "토큰에 담아서 보낼 실제 데이터 상자(내용물)"입니다.
    //
    //서버와 클라이언트(리액트)가 로그인 후 주고받을 유저의 정보들이 여기에 담깁니다. 이 상자 안에 들어가는 데이터 한 줄 한 줄을 복잡한 말로 클레임(Claim)이라고 부릅니다.

    //2. 👤 Subject (서브젝트)란?
    //Payload 데이터 상자 안에서도 "이 토큰의 주인(식별자)이 누구인가?"를 나타내는 가장 핵심적인 표준 데이터입니다. JWT 규격에서는 글자 수를 줄이기 위해 축약어인 sub라는 이름(Key)으로 저장됩니다.
    //
    //보통 유저를 고유하게 식별할 수 있는 아이디(loginId)나 DB의 PK값(User ID 시퀀스)을 여기에 집어넣습니다.
    //
    //아까 우리가 백엔드 JwtTokenProvider에서 코드를 짤 때 아래와 같이 세팅했던 부분이 바로 이 Subject를 지정한 것입니다.


    /**
     * Refresh Token 생성 (개별 필드 기반)
     */
    public String createRefreshToken(Long id,String loginId){
        Claims claims= Jwts.claims()
                .subject(loginId) //subject 자리에는 관례적으로 숫자인 PK보다 사용자를 식별할 수 있는 유니크한 문자열(아이디, 이메일 등)을 넣는 경우가 많음
                .add("id",id) //누구의 재생성 열쇠인지 식별하기 위해 pk를 담음
                .build();
        return createToken(claims,refreshTokenExpirationTime);
    }

    /**
     * [오버로딩 편의 메서드] Member 엔티티 기반 토큰 생성 (일반 로그인 서비스용)
     */
    public String createRefreshToken(Member member) {

        return createRefreshToken(member.getId(), member.getLoginId());
    }

    //accessToken과 refreshToken의 공통 메서드
    private String createToken(Claims claims,long expirationMillis){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .claims(claims)
                // 같은 회원이 같은 초에 요청하더라도
                // 토큰 문자열이 서로 달라지도록 고유번호를 넣습니다.
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    //토큰에서 유저 loginId(subject)추출
    public String getLoginId(String token){

        return getClaims(token).getSubject();
    }
    /**
     * 회원의 DB 기본키를 읽음.
     *
     * Claims.getId()는 jti, 즉 토큰 고유번호를 반환하므로
     * 회원 ID를 얻기 위해 사용하면 안 됨
     */
    public Long getId(String token){

        Long memberId=getClaims(token).get("id", Long.class);

        if(memberId==null){
            throw new IllegalArgumentException(
                    "토큰에 회원 id가 없습니다."
            );
        }
        return memberId;
    }

    public Claims getClaims(String token){

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Access Token에 저장된 권한 문자열을 Enum으로 변환
     * Refresh Token에는 role이 없으므로 이 메서드를 사용하지 않음
     */
    public Role getRole(String token) {
        String role = getClaims(token).get("role", String.class);

        if (role == null) {
            throw new IllegalArgumentException(
                    "토큰에 권한 정보가 없습니다."
            );
        }

        return Role.valueOf(role);
    }

    /**
     * 발급한 JWT의 실제 만료 시각을 DB 저장용으로 변환.
     * 별도로 현재 시각에 7일을 더하지 않아 만료 기준이 일치.
     *
     * 현재 프로젝트의 LocalDateTime 저장 방식에 맞춰
     * 서버 기본 시간대를 사용.
     */
    public LocalDateTime getExpiryDate(String token) {
        return LocalDateTime.ofInstant(
                getClaims(token).getExpiration().toInstant(),
                ZoneId.systemDefault()
        );
    }

    //토큰 유효성 및 만료기간 검증
    public boolean validateToken(String token){
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e){
            // 토큰이 변조되었거나, 만료되었거나, 비어있을 때 예외가 터짐
            return false;
        }
    }

}
