package com.example.demo.dto.home;

public record ItemSummaryResponse(
        //상품상세페이지로 이동할 때 사용할 pk
        Long id,

        //상품카드에 표시할 이름
        String name,

        //메인화면에서는 가격정도만 있으면 됨
        int price,

        //대표이미지 1장만 사용
        String thumbnailUrl,

        int likeCount

){
}
