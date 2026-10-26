package com.example.demo.dto.home;

import com.example.demo.entity.Post.Post;

public record PostSummaryResponse(

        //게시글 상세이동pk
        Long postId,

        String title,

        //작성자 표시용
        String nickname,

        long viewCount,

        long likeCount

) {

}
