package com.example.demo.convert;

import com.example.demo.entity.item.ItemDetailSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

//JPA 엔티티와 컨버터 설정 DB에는 텍스트(JSON)로 저장되지만, 엔티티 안에서는 ProductDetailSpec이라는 객체로 다루도록 변환기(Converter)를 지정합니다.
@Converter
public class ItemDetailConverter implements AttributeConverter<ItemDetailSpec,String> {
    private final ObjectMapper objectMapper=new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ItemDetailSpec itemDetailSpec) {
        try {
            return objectMapper.writeValueAsString(itemDetailSpec);
        }catch(JsonProcessingException e){
            throw new IllegalArgumentException("json 직렬화 실패",e);
        }
    }

    @Override
    public ItemDetailSpec convertToEntityAttribute(String dbData) {
        try{
            if(dbData==null){
                return null; }
            return objectMapper.readValue(dbData,ItemDetailSpec.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json 역직렬화 실패",e);
        }
    }

//    이렇게 짜두면 비즈니스 로직에서 다음과 같이 안전하게 다형성을 쓸 수 있음
//    if (item.getDetailSpec() instanceof ElectronicsDetail) {
//        ElectronicsDetail electronics = (ElectronicsDetail) item.getDetailSpec();
//        System.out.println("이 컴퓨터의 RAM은: " + electronics.getRam() + "GB");
//    }

}
