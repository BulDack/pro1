package com.example.demo.config;


import com.example.demo.config.oauth.dto.PrincipalDetails;
import com.example.demo.entity.ennum.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//🔄 전체 동작 흐름 한눈에 보기
//1.[리액트] 토큰 보관 및 전송: 로그인이 성공하면 리액트는 토큰을 로컬 스토리지에 저장해 둡니다. 그리고 이후에 마이페이지 조회 같은 다른 API를 호출할 때, 아까 우리가 만든 Axios 인터셉터를 이용해 요청 헤더(Authorization: Bearer <토큰>)에 토큰을 실어 보냅니다.
//
//2.[스프링 가로채기]: 스프링 부트 서버에 요청이 도착하면, 컨트롤러로 가기 전에 스프링 시큐리티의 필터(Filter)가 요청을 낚아챕니다.
//
//3.[시큐리티 컨텍스트 장착]: 필터가 토큰이 유효한지 검증한 후, 토큰 내부의 유저 정보(Subject, Role)를 꺼내서 시큐리티 전용 출입증(Authentication)을 만든 뒤 SecurityContext에 PrincipleDetails를 저장함.

//세션 방식: CustomOAuth2UserService에서 DB 조회 후 Member 엔티티/DTO를 세션 메모리에 계속 상주시킴.
//JWT 방식: 로그인 시점에만 토큰 발급용으로 CustomOAuth2UserService를 사용하고, 이후 요청은 JwtAuthenticationFilter가 JWT 토큰 복원만으로 TokenMemberDto와 PrincipalDetails를 재구성함.
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailService userDetailService;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException{

        //1.리액트가 보낸 요청 헤더에서 "Bearer<토큰>"문자열을 추출
        String token =resolveToken(request);

        // 2. 토큰이 존재하고, JwtTokenProvider를 통해 검증했을 때 유효하다면?
        if(token!=null && jwtTokenProvider.validateToken(token)){

            // 3. 토큰에서 주인(loginId)을 꺼냄.
            // 🔥 DB 조회 없이 토큰(Claims)에서 직접 값들을 꺼냄.
            String loginId= jwtTokenProvider.getLoginId(token);
            Long id=jwtTokenProvider.getClaims(token).get("id", Long.class);
            Role role=jwtTokenProvider.getClaims(token).get("role", Role.class);

            //DB조회없이 PrincipalDetails 생성,JWT 전용 UserDetails 생성 메서드를 여기서 씀
            PrincipalDetails principleDetails=userDetailService.loadUserByTokenClaims(loginId,id,role);

            // 4. (선택사항) 필요하다면 토큰에서 Role도 꺼내서 시큐리티 권한으로 변환합니다.
            // 여기서는 임시로 간단하게 ROLE_USER 권한을 부여하는 예시입니다.
            // List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            // 5. 🔥 스프링 시큐리티 전용 "출입증(Authentication)" 객체를 만듬.
            UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(principleDetails,null,principleDetails.getAuthorities());

            // 6. 🔥 만든 출입증을 시큐리티 금고(SecurityContext)에 집어넣음!
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 7. 다음 필터나 컨트롤러로 요청을 통과시킴.
        filterChain.doFilter(request,response);

    }

    // 헤더에서 토큰을 파싱하는 유틸리티 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken=request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken)&& bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7); // "Bearer " 뒤의 순수 토큰 문자열만 잘라냄
        }
        return null;
    }
}
