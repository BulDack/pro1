package com.example.demo.entity;

import com.example.demo.dto.JoinRequestDto;
import com.example.demo.entity.ennum.Role;
import com.example.demo.entity.item.Cart;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//대부분의 RDBMS(MySQL 포함)에서 인덱스가 자동으로 생성되는 컬럼은 다음 경우
//PK (Primary Key): 기본 키
//UNIQUE 제약조건: @Column(unique = true)가 설정된 컬럼()
//외래키일경우
public class Member {

    @Id
    @GeneratedValue
    @Column(name="member_id")
    private Long id;

    private String username;

    private String email;

    private String loginId;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String provider;    // google, kakao 등
    private String providerId;  // 외부 서비스의 고유 유저 ID

    @Embedded
    private Address address;

    @OneToOne(mappedBy = "member",fetch = FetchType.LAZY)
    private Cart cart;

    @OneToMany(mappedBy = "member",cascade = CascadeType.ALL)
    private List<Order>orders= new ArrayList<>();

    public static Member createMember(JoinRequestDto form, Address address, Role role){
        Member member =new Member();
        member.username=form.getUsername();
        member.password=form.getPassword();
        member.loginId=form.getLoginId();
        member.address=address;
        member.role=role;
        return member;
    }

    @Builder //빌더를 메서드 단위에 놓으면 원하는 필드로만 구성할수 있음
    public Member(String email, String username, String provider, String providerId, Role role) {
        this.email = email;
        this.username = username;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
    }

    public Member update(String name){
        this.username=name;
        return this;
    }

    public void updatePassword(String encodedPassword) {
        this.password=encodedPassword;
    }

    public void changeName(String username) {
        this.username=username;
    }
}
