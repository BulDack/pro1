package com.example.demo.entity.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


//모든 카테고리의 핵심 속성을 ITEM 테이블에 다 때려 박는 대신,검색 필터 전용 연결 테이블(ItemSearchAttribute)을 딱 하나만 파서 1:N 관계로 묶는 방식
//        이렇게 하면 ITEM 테이블에 NULL 컬럼이 늘어나는 것을 완벽하게 막을 수 있음.
@Entity
@Getter
@Table(name="item_search_attribute")
@NoArgsConstructor(access = AccessLevel.PROTECTED) //jpa엔티티는 @NoargsConstructor가 필수,builder를 붙이려면 따로
public class ItemSearchAttribute {

    @Id@GeneratedValue
    @Column(name="attribute_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "item_id")
    private Item item;

    private String attributeKey; //예:"RAM"
    private String attributeValue; //예: "16" (숫자도 문자열로 저장후 쿼리시 변환)
    private Integer numberValue;

    //
    @Builder(access = AccessLevel.PRIVATE)
    private ItemSearchAttribute(Item item,String attributeKey,String attributeValue,Integer numberValue){
        this.item=item;
        this.attributeKey=attributeKey;
        this.attributeValue=attributeValue;
        this.numberValue=numberValue;
    }

    public static ItemSearchAttribute createStringAttribute(Item item,String key,String value){
        return ItemSearchAttribute.builder()
                .item(item)
                .attributeKey(key)
                .attributeValue(value)
                .build();
    }
    public static ItemSearchAttribute createNumberAttribute(Item item,String key,Integer value){
        return ItemSearchAttribute.builder()
                .item(item)
                .attributeKey(key)
                .numberValue(value)
                .build();
    }

}
