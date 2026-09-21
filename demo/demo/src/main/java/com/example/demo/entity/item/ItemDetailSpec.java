package com.example.demo.entity.item;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use= JsonTypeInfo.Id.NAME,
        include= JsonTypeInfo.As.PROPERTY,
        property="type" //json 안에 "type": "clothing"형태로 구별자 저장
)
@JsonSubTypes({
        @JsonSubTypes.Type(value=ClothingDetail.class,name="CLOTHING"),
        @JsonSubTypes.Type(value=ElectronicsDetail.class,name="ELECTRONICS")
})
public abstract class ItemDetailSpec {
    //공통 비핵심 정보
}
