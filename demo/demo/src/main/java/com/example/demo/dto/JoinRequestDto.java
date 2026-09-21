package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class JoinRequestDto {

    @NotEmpty(message = "회원이름은 필수입니다.")
    private String username;

    private String loginId;

    private String password;

    private String city;
    private String street;
    private String zipcode;
}
