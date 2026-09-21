package com.example.demo.dto.security;

import com.example.demo.entity.ennum.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenMemberDto {

            Long id;
            String memberId;
            Role role;
}
