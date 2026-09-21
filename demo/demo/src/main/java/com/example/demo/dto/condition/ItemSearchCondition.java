package com.example.demo.dto.condition;

import lombok.*;

@Getter@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSearchCondition {

    // 1. 상품 공통 기본 검색 조건
    private String name;     //상품명 키워드(예:맥북,맨투맨)
    private Integer priceLoe;  //가격 상한선
    private Integer priceGoe;  //가격 하한선
    private Long categoryId;   //카테고리 id(대분류/중분류/소분류 클릭 시)

    //2.동적 카테고리별 핵심 필터 속성(key-value)매핑용
    //화면에서 넘겨주는 데이터 타입에 맞춰 선언함
    private Integer minRam;  //전자기기용: 최소RAM용량(예:16)
    private Integer maxRam;  //전자기기용: 최대RAM용량(예:512)
    private String clothingSize; //의류용: 사이즈(예:"L","XL")
    private String color; //공통 : 색상("WHITE","BLACK")

    //3.정렬조건(디폴트 속성)
    private String sortBy;  //정렬기준(예: "LATEST", "PRICE_ASC", "PRICE_DESC", "POPULAR")
}
