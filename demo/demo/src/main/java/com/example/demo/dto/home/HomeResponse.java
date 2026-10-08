package com.example.demo.dto.home;


import java.util.List;

//메인 화면에 쓸 dto(여러개의 dto로 구성)
public record HomeResponse(

        //메인 상단에 보여줄 인기 상품목록
        List<ItemSummaryResponse>popularItem,

        //최근 등록된 상품 목록
        List<ItemSummaryResponse>newItem,

        //인기 게시글 목록
        List<PostSummaryResponse>popularPosts,

        //최근 게시글 목록
        List<PostSummaryResponse>recentPosts
) {
}
