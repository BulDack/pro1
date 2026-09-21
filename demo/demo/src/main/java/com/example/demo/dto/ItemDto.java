package com.example.demo.dto;

import com.example.demo.entity.Category;
import com.example.demo.entity.item.Item;
import com.example.demo.entity.item.ItemSearchAttribute;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

//한눈에 보는 실무 가이드라인
//1) 이런 곳엔 static 생성 메서드를 : JPA 엔티티 전체
//객체가 생성될 때 무조건 채워져야 하는 필수 규칙(도메인 제약)이 존재하는 객체
//연관관계 편의 메서드(addItemCategory 등)가 생성 시점에 함께 실행되어야 하는 객체
//
//2) 이런 곳엔 Builder 패턴을:모든 DTO 객체
//생성할 때 넘겨야 하는 파라미터가 4~5개 이상으로 너무 많아서 생성자 순서가 헷갈리는 객체
//선택적 필드(Optional 필드, 있어도 되고 없어도 되는 값)가 많은 객체
public final class ItemDto {

    // 1. 상품 등록 요청 DTO
    @Getter
    @NoArgsConstructor
    public static class CreateRequest{
        @NotBlank
        private String name;
        @Positive
        private Integer price;
        @NotNull
        private Integer stockQuantity;
        private String description;
        @NotNull
        private Long categoryId;
        private List<AttributeDto>attributes;

        //dto->엔티티 변환 편의 메서드
        public List<ItemSearchAttribute>toAttributeEntries(Item item){
            if(this.attributes==null)return List.of();

            return this.attributes.stream()
                    .map(dto->{
                        if(dto.stringValue()!=null){
                            return ItemSearchAttribute.createNumberAttribute(item, dto.key(), dto.numberValue());
                        }else{
                            return ItemSearchAttribute.createStringAttribute(item,dto.key(),dto.stringValue());
                        }
                    }).toList();
        }
    }

    @Getter
    @NoArgsConstructor
    // 2. 상품 수정 요청 DTO
    public static class UpdateRequest{
        private String name;
        private Integer price;
        private Integer stockQuantity;
        private String description;
        private List<AttributeDto>attributes;

        public List<ItemSearchAttribute>toAttributeEntries(Item item){
            if(this.attributes==null)return List.of();

            return this.attributes.stream()
                    .map(dto->{
                        if(dto.stringValue()!=null){
                            return ItemSearchAttribute.createNumberAttribute(item, dto.key(), dto.numberValue());
                        }else{
                            return ItemSearchAttribute.createStringAttribute(item,dto.key(),dto.stringValue());
                        }
                    }).toList();
        }
    }

    // 3. 상품 목록 조회 응답 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private Long id;
        private String name;
        private Integer price;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateReponse{

        private Long id; // 👈 서버에서 생성된 고유 ID (프론트가 다음 페이지 이동 시 필수)
        private String name;
        private Integer price;
        private String description;
        private Integer stockQuantity;

        // 💡 누가 등록했는지 프론트가 알 수 있도록 최소한의 정보만 노출 (순환참조 방지)
        private Long sellerId;
        private String sellerName;

        private LocalDateTime createAt; //등록시간

        /**
         * 엔티티(Entity)를 DTO로 안전하게 변환해주는 편의 메서드 (팩토리 메서드 패턴)
         */
        public static CreateReponse from(Item item){
            return CreateReponse.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .price(item.getPrice())
                    .description(item.getDescription())
                    .stockQuantity(item.getStockQuantity())
                    // item.getMember()가 null이 아닐 때만 안전하게 가져오기 (관리자 공용 상품일 경우 대비)
//                    .sellerId(item.getMember() != null ? item.getMember().getId() : null)
//                    .sellerName(item.getMember() != null ? item.getMember().getLoginId() : "SYSTEM(ADMIN)")
//                    .createdAt(item.getCreatedAt()) // BaseEntity를 쓰신다면 존재
                    .build();

        }

    }



    //DTO 영역: record가 롬복을 거의 완전히 대체했습니다. 더 이상 DTO 파일 위에 롬복 어노테이션을 도배할 필요가 없어졌습니다.
    //JPA 엔티티 영역: 여기는 여전히 롬복을 써야 합니다. JPA 엔티티는 프록시 기술, 지연 로딩, 기본 생성자(NoArgsConstructor),
    // 그리고 값의 수정(더티 체킹)이 자유로워야 하므로, 불변 객체인 record를 엔티티 클래스로 사용하는 것은 불가능합니다.

    //record 객체의 핵심 특징 (꼭 알아야 할 점)
    //불변성 (Immutable): record로 만든 객체는 한 번 값이 채워지면 절대로 수정할 수 없습니다.
    // 모든 필드가 자동으로 final이 되기 때문에 Setter 메서드 자체가 존재하지 않습니다.
    // 안전한 데이터 전달에 최적화되어 있습니다.
    //상속 불가능: record는 내부적으로 이미 자바의 java.lang.Record 클래스를 상속받고 있기 때문에, 다른 클래스를 extends 할 수 없습니다.
    // (단, 인터페이스 implements는 가능합니다.)
    public record AttributeDto(
            @NotBlank
            String key,
            String stringValue,//문자열 속성값
            Integer numberValue //숫자형 속성값
    ){
        // record의 콤팩트 생성자를 이용한 커스텀 검증
        public AttributeDto {
            // 둘 다 null이거나, 둘 다 값이 채워져 있으면 에러 발생
            if ((stringValue == null && numberValue == null) || (stringValue != null && numberValue != null)) {
                throw new IllegalArgumentException("속성값은 문자열과 숫자중 하나만 입력해야합니다");
            }
        }
    }

    //인스턴스화 방지
    private ItemDto() {};
}
