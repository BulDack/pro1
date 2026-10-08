package com.example.demo.entity.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemImage {

    @Id
    private Long id;
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int sortOrder;  //이미지 표시 순서,예를들어 상품목록에서 대표이미지만 보여주려면 sort_order=0번 보여주면됨
}
//product_image
//---------------------------------------------------------
//id    product_id    image_url                         sort_order
//1     1             /uploads/products/xxx.jpg         0
//2     1             /uploads/products/yyy.jpg         1
//3     1             /uploads/products/zzz.jpg         2
