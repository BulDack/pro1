package com.example.demo.config;

import com.example.demo.dto.security.PrincipalDetails;
import com.example.demo.dto.security.TokenMemberDto;
import com.example.demo.entity.ennum.Role;
import com.example.demo.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
//자동 감지 (Spring Boot 기준)
//스프링 시큐리티는 인증을 처리할 때 컨테이너에 UserDetailsService 인터페이스를 구현한 빈이 있는지 찾음.
//CustomUserDetailService에 @Service 어노테이션이 붙어 있다면, 이를 기본 인증 처리기(DaoAuthenticationProvider)에 자동으로 주입하여 사용.
public class CustomUserDetailService implements UserDetailsService {

    private final MemberJpaRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        /*
        // 1. 🔥 매 요청마다 DB를 직접 조회
        Member member=memberRepository.findByLoginId(loginId).orElseThrow(()->new UsernameNotFoundException("존재하지 않는회원입니다."));

        //2. DB에서 회원의 현재상태 (정지 여부 등)를 실시간 체크
        if(member.getStatus()==MemberStatus.BANNED){
            throw new DisabledException("정지된 회원입니다.");
        }
        // 3. Entity -> DTO 변환 후 UserDetails 반환
        TokenMemberDto dto=new TokenMemberDto(member.getId(), member.getLoginId(), member.getRole());
        return new CustomUserDetails(dto); */

       throw new UsernameNotFoundException("jwt인증구조에서는 이 메서드를 직접 사용하지 않습니다.");
    }

    // ⭐ JWT 전용 UserDetails 생성 메서드
    public PrincipalDetails loadUserByTokenClaims(String loginId, Long id, Role role){

        TokenMemberDto member= new TokenMemberDto(id,loginId,role);
        // DB 조회 없이, 넘겨받은 Claim 정보로만 UserDetails를 생성하여 반환!
        return new PrincipalDetails(member);
    }
}
