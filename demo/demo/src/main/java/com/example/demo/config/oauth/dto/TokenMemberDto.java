package com.example.demo.config.oauth.dto;

import com.example.demo.entity.Member;
import com.example.demo.entity.ennum.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//pricipleDetail을 통해 값을 편하게 불러오고 싶으면 member속성을 추가 해도 됨
public class TokenMemberDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;          // DB PK
    private String memberId;  // 사용자 식별자 (이메일 또는 ID)
    private Role role;        // 권한 (ROLE_USER, ROLE_ADMIN)

    // Member 엔티티에서 DTO로 정적 변환하는 팩토리 메서드 (선택 사항 - 편의성)
    public static TokenMemberDto from(Member member) {
        return TokenMemberDto.builder()
                .id(member.getId())
                .memberId(member.getEmail()) // 또는 member.getMemberId()
                .role(member.getRole())
                .build();
    }
}
