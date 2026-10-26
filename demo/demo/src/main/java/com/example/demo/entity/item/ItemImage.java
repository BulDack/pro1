package com.example.demo.entity.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemImage {

    @Id
    private Long id;
    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id",nullable = false)
    private Item item;

    @Column(nullable = false)
    private int sortOrder;  //이미지 표시 순서,예를들어 상품목록에서 대표이미지만 보여주려면 sort_order=0번 보여주면됨

    @Builder
    public ItemImage(String imageUrl,int sortOrder,Item item){
        this.imageUrl=imageUrl;
        this.sortOrder=sortOrder;
        this.item=item;
    }
}
//item_image
//---------------------------------------------------------
//id    item_id    image_url                         sort_order
//1     1             /uploads/items/xxx.jpg         0
//2     1             /uploads/items/yyy.jpg         1
//3     1             /uploads/items/zzz.jpg         2
