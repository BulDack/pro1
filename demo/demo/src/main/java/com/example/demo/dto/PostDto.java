package com.example.demo.dto;

import com.example.demo.entity.Post.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PostDto {

    //게시물작성 dto
    public record createRequestDto(

      @NotBlank(message = "제목을 입력해주세요.")
      @Size(max=100, message=" 제목은 100자 이하로 입력해주세요.")
      String title,

      @NotBlank(message = "내용을 입력해주세요.")
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
            Long authorId,// 해당 게시글의 작성자 pk
            String nickname,
            long viewCount,
            long likeCount,
            LocalDateTime createdDate,

            boolean liked, //현재 사용자가 좋아요를 눌렀는지

            // 현재 사용자가 이 게시글을 수정할 수 있는지(UI표시를 돕는 정보일뿐 수정요청을 허용하는 근거는 아님
            // 실제로는 백엔드에서 인증된 사용자와 게시글의 작성자를 다시 비교해야함)
            boolean canEdit,

            // 현재 사용자가 이 게시글을 삭제할 수 있는지
            //이렇게 구분하는 이유는 수정과 삭제의 권한 정책이 나중에 달라질 수도 있기 때문!!
            boolean canDelete
    ){
        public postDetailResponse(
                Long id,
                String title,
                String content,
                Long authorId,
                String nickname,
                long viewCount,
                long likeCount,
                LocalDateTime createdDate,
                boolean liked
        ){
            this(id,title,content,authorId,nickname,viewCount,likeCount,createdDate,liked,false,false);
        }

        public postDetailResponse withPermissions(
                boolean canEdit,
                boolean canDelete
        ) {
            return new postDetailResponse(
                    id,
                    title,
                    content,
                    authorId,
                    nickname,
                    viewCount,
                    likeCount,
                    createdDate,
                    liked,
                    canEdit,
                    canDelete
            );
        }

    }

    //게시글 목록용 dto
    public record postListResponse(
            Long id,
            String title,
            Long viewCount,
            Long likeCount,
            String nickname,
            LocalDateTime createdDate
    ){  //생성자 팩토리 메서드 Java Record의 생성자 팩토리 메서드에서 값이 매칭되는 핵심 원리는 "개발자가 직접 작성한 Getter 호출 결과가 Record의 생성자 파라미터 순서대로 대입되는 것"입니다.
        // Java의 Record는 선언한 필드 순서대로 모든 필드를 받는 기본 생성자(Canonical Constructor)를 자동으로 생성합니다.
        // 팩토리 메서드는 엔티티에서 값을 꺼내 이 기본 생성자에 순서대로 넣어주는 역할을 합니다.
        public static postListResponse from(Post post){
            return new postListResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getViewCount(),
                    post.getLikeCount(),
                    post.getMember().getNickname(),
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
