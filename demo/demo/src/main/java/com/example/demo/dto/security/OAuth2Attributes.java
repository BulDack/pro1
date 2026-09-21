package com.example.demo.dto.security;

import com.example.demo.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

import static com.example.demo.entity.ennum.Role.ROLE_USER;


//각 공급자별 데이터 통합 dto(구글,네이버,카카오등 주는 json key값이 다 달라서
// OAuth2User.getAttributes()의 map을 공통포맷으로 파싱해주는 객체
@Getter
public class OAuth2Attributes {

    private Map<String, Object>attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String provider;
    private String providerId;


    @Builder
    public OAuth2Attributes(Map<String,Object>attributes,String nameAttributeKey,String name,
                            String email,String provider,String providerId){
        this.attributes=attributes;
        this.nameAttributeKey= nameAttributeKey;
        this.name= name;
        this.email= email;
        this.provider= provider;
        this.providerId= providerId;

    }

    //서비스별 구별자 파싱 구문
    public static OAuth2Attributes of(String registrationId,String userNameAttributeName,Map<String,Object>attributes){
        if("kakao".equals(registrationId)){
            return ofKakao(userNameAttributeName,attributes);
        }
        return ofGoogle(userNameAttributeName,attributes);
    }

    private static OAuth2Attributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuth2Attributes.builder()
                .name((String)attributes.get("name"))
                .email((String)attributes.get("email"))
                .provider("google")
                .providerId((String)attributes.get(userNameAttributeName))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuth2Attributes ofKakao(String userNameAttributeName,Map<String,Object>attributes){
        Map<String,Object>kakaoAccount= (Map<String, Object>) attributes.get("kakao_account");
        Map<String,Object>profile=(Map<String, Object>)kakaoAccount.get("profile");

        return OAuth2Attributes.builder()
                .name((String)profile.get("nickname"))
                .email((String)kakaoAccount.get("email"))
                .provider("kakao")
                .providerId(String.valueOf(attributes.get(userNameAttributeName)))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // DTO 데이터를 바탕으로 자체 User 엔티티 생성 메서드
    public Member toEntity(){
        return Member.builder()
                .username(name)
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .role(ROLE_USER)
                .build();
    }
}
