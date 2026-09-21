package com.example.demo.config;

import com.example.demo.dto.security.TokenMemberDto;
import com.example.demo.entity.Member;
import com.example.demo.repository.MemberJpaRepository;
import com.example.demo.dto.security.OAuth2Attributes;
import com.example.demo.dto.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
//CustomOAuth2UserService는 스프링 시큐리티의 필터 체인 내부에서 인증을 처리할 때 자동으로 동작하는 서비스
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberJpaRepository memberRepository;


    //OAuth2UserRequest 는 clientRegistration(우리 서비스의 OAuth2 설정 정보)과
    // accessToken(소셜 공급자가 발급해준 엑세스 토큰)등으로 이루어져 있음
    //스프링 시큐리티가 받아온 엑세스 토큰과 yml파일의 설정 정보를 하나로 묶어서 OAuth2UserRequest객체를 조립함
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        //1.기본 DefaultOAuth2UserService를 통해 provider로부터 유저 정보 (attributes)가져오기
        OAuth2User oAuth2User=super.loadUser(userRequest);

        //2.어느 소셜 서비스인지 확인
        String registrationId=userRequest.getClientRegistration().getRegistrationId();

        //3.소셜 고유의 pk키값 이름 획득(구글은 sub,카카오는 id)
        String userNameAttributeName=userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        //4.provider별 응답 객체를 공통 dto(OAuth2Attributes)로 파싱
        OAuth2Attributes attributes= OAuth2Attributes.of(
                registrationId,
                userNameAttributeName,
                oAuth2User.getAttributes()
        );

        //5. 서비스 자체 member엔티티로의 매핑및 저장/수정 로직 실행
        Member member=saveOrUpdate(attributes);

        // 6. ⭐️ Entity -> DTO 변환
        TokenMemberDto tokenMemberDto=new TokenMemberDto(member.getId(), member.getLoginId(), member.getRole());

        // 7. DTO를 PrincipalDetails에 담아서 반환
        return new PrincipalDetails(tokenMemberDto,attributes.getAttributes());


    }

    //db조회후 가입 혹은 업데이트를 수행하는 메서드
    @Transactional // 영속성 컨텍스트의 더티 체킹(Dirty Checking)을 통한 자동 수정을 위해 필수!
    private Member saveOrUpdate(OAuth2Attributes attributes) {

        // 1. 소셜 서비스에서 제공하는 유일한 식별값(예: 이메일 또는 제공자 고유 ID)으로 기존 회원 조회
        Member member=memberRepository.findByEmail(attributes.getEmail())

                .map(existingMember-> {
                    // 2. [기존 회원] 존재한다면 정보 수정
                    // 영속 상태인 엔티티의 필드 값을 변경했으므로, 메서드가 정상 종료(트랜잭션 커밋)될 때
                    // JPA가 더티 체킹을 수행하여 자동으로 UPDATE 쿼리를 날려줍니다! (save 호출 불필요)

                    // 3. [기존 회원] 이미 존재하는 사용자라면 소셜의 최신 정보(닉네임, 프로필 등)로 업데이트
                    return existingMember.update(attributes.getName());
                })
                // 4. 비회원일 경우 새로 빌드해서 회원가입 진행
                // 비영속 객체이므로 DB에 반영하기 위해 명시적으로 repository.save()를 호출
                .orElseGet(() ->{
                    Member newMember= attributes.toEntity();
                    return memberRepository.save(newMember);

                } );

        // 5. 저장 또는 변경된 회원 엔티티 반환
        return memberRepository.save(member);
    }
}
