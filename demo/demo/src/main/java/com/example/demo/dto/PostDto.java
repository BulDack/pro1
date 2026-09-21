package com.example.demo.dto;

import com.example.demo.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PostDto {

    //게시물작성 dto
    public record createRequestDto(

      @NotBlank
      @Size(max=200)
      String title,

      @NotBlank
      String content
    ){}

    //게시물수정 dto
    public record updateRequestDto(

            @NotBlank
            @Size(max=200)
            String title,

            @NotBlank
            String content
    ){}


    //게시물 1개 불러오는 dto
    public record postDetailResponse(
            Long id,
            String title,
            String content,
            String loginId,
            Long likeCount,
            LocalDateTime createdDate
    ){}

    //게시글 목록용 dto
    public record postListResponse(
            Long id,
            String title,
            Long likeCount,
            String memberLoginId,
            LocalDateTime createdDate
    ){  //생성자 팩토리 메서드 Java Record의 생성자 팩토리 메서드에서 값이 매칭되는 핵심 원리는 "개발자가 직접 작성한 Getter 호출 결과가 Record의 생성자 파라미터 순서대로 대입되는 것"입니다.
        // Java의 Record는 선언한 필드 순서대로 모든 필드를 받는 기본 생성자(Canonical Constructor)를 자동으로 생성합니다.
        // 팩토리 메서드는 엔티티에서 값을 꺼내 이 기본 생성자에 순서대로 넣어주는 역할을 합니다.
        public static postListResponse from(Post post){
            return new postListResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getPostLike(),
                    post.getMember().getLoginId(),
                    post.getCreatedDate()
            );
        }

    }

    public record commentDto(
      @NotBlank(message = "댓글내용은 공백일수 없습니다.")
      @Size(max=500,message = "댓글은 500자 이내로만 작성해주세요.")
      String content
    ){}

    public record searchRequestDto(
            @NotBlank(message = "검색내용은 공백일수 없습니다.")
            String title,
            //기간조회
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,

            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate endDate
    ){}

}
