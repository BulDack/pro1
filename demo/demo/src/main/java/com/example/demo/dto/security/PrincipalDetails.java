package com.example.demo.dto.security;


//member엔티티를 갖고있으면서 스프링시큐리티가 이해할수 있게 UserDetails,OAuth2User를 동시구현

import com.example.demo.entity.ennum.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class PrincipalDetails implements UserDetails, OAuth2User {

    private final TokenMemberDto tokenMemberDto;
    private Map<String,Object>attributes; //소셜로그인에서 받은 정보

    //일반 로그인용 생성자
    public PrincipalDetails(TokenMemberDto tokenMemberDto){
        this.tokenMemberDto=tokenMemberDto;
    }
    //oAuth2 소셜 로그인용 생성자
    public PrincipalDetails(TokenMemberDto tokenMemberDto,Map<String,Object>attributes){
        this.tokenMemberDto=tokenMemberDto;
        this.attributes=attributes;
    }

    //OAuth2User 인터페이스 메서드
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return tokenMemberDto.getId().toString(); //서비스 내부 식별자 반환
    }

    //UserDetails 인터페이스 메서드
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return Collections.singletonList(
//                new SimpleGrantedAuthority("ROLE_" + tokenMemberDto.getRole().name())
//        );
        Collection<GrantedAuthority>authorities=new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority(tokenMemberDto.getRole().name()));

        return authorities;
    }


    @Override
    public String getPassword() {
        return null; //소셜로그인은 패스워드가 없음
    }

    @Override
    public String getUsername() {
        return tokenMemberDto.getMemberId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public Long getId(){
        return tokenMemberDto.getId();
    }

    public Role getRole(){ return tokenMemberDto.getRole(); }


}
