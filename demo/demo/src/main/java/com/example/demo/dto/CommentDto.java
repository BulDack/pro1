package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {


    public record commentResponseDto(
            Long id,
            String content,
            Long memberId,
            String nickname,
            LocalDateTime createdDate,
            boolean isAuthor
    ){

    }

    public record commentRequestDto(
            @NotBlank(message = "댓글내용은 공백일수 없습니다.")
            @Size(max=500,message = "댓글은 500자 이내로만 작성해주세요.")
            String content,
            Long postId,
            Long memberId
    ){}

}
