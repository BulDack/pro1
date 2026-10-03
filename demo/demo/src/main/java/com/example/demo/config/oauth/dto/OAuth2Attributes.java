package com.example.demo.config.oauth.dto;


import com.example.demo.entity.Member;
import com.example.demo.entity.ennum.Provider;
import com.example.demo.entity.ennum.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

//소셜별 로그인 응답 규격 공통화
//각 공급자별 데이터 통합 dto(구글,네이버,카카오등 주는 json key값이 다 달라서
//OAuth2User.getAttributes()의 map을 공통포맷으로 파싱해주는 객체
@Getter
public class OAuth2Attributes {

    private Map<String,Object> attributes;
    private String nameAttributeKey; //OAuth2 로그인 진행시 키가 되는 필드값(pk역할,google은 'sub')
    private String name;
    private String email;
    private Provider provider;
    private String providerId;

    @Builder
    public OAuth2Attributes(Map<String,Object>attributes, String nameAttributeKey, String name, String email, Provider provider, String providerId){
        this.attributes=attributes;
        this.nameAttributeKey=nameAttributeKey;
        this.name=name;
        this.email=email;
        this.provider=provider;
        this.providerId = providerId;

    }

    //OAuth2User에서 반환하는 사용자 정보는 Map형태이므로 값 하나하나를 변환
    //서비스별 구변자 파싱구문
    public static OAuth2Attributes of(String registrationId, String userNameAttributeName, Map<String,Object>attributes){
        if("google".equals(registrationId)){
            return ofGoogle(userNameAttributeName,attributes);
        }
        //구글뿐만아니라 기타등등 소셜 로그인도 추가하기
        throw new IllegalArgumentException("지원하지 않는 소셜로그인 입니다: " + registrationId);
    }

    private static OAuth2Attributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuth2Attributes.builder()
                .name((String)attributes.get("name"))
                .email((String)attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .provider(Provider.GOOGLE)
                .providerId((String)attributes.get(userNameAttributeName))
                .build();
    }

    private static OAuth2Attributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuth2Attributes.builder()
                .name((String) kakaoProfile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                //.picture((String) kakaoProfile.get("profile_image_url"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .provider(Provider.KAKAO) // ★ KAKAO 지정
                .providerId((String)attributes.get(userNameAttributeName))
                .build();
    }

    // DTO 데이터를 바탕으로 자체 Member 엔티티 생성 메서드
    public Member toEntity(){
        return Member.builder()
                .username(name)
                .email(email)
                .role(Role.ROLE_USER)
                .provider(this.provider)
                .providerId(providerId)
                .build();
    }

}
