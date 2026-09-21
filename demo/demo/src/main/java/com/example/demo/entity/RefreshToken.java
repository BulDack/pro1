package com.example.demo.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="RefreshToken_id")
    private Long id;

    @Column(nullable = false,unique = true)
    private String token;

    @Column(nullable = false)
    private long memberId;  //연관관계 매핑대신 성능과 결합도를 낮추기 위해 id값만 저장

    @Column(nullable = false)
    public boolean isUsed; // RTR정책 :이미 사용된 토큰인지 여부

    @Builder
    public RefreshToken(String token,Long memberId,boolean isUsed){
        this.token=token;
        this.memberId=memberId;
        this.isUsed=isUsed;
    }

    // 토큰 사용 완료 처리 (더티 체킹용)
    public void changeToUsed(){
        this.isUsed=true;
    }
}
