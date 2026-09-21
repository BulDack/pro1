package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
//장바구니 담기 요청 dto
public class CartItemDto {

    //record를 사용하면 자동 생성되는 것: 생성자,getter(정확히는 accessor),equals(),hashCode(),toString()
    //ex)request.itemId get을 붙일필요 없음
    //record는 값을 기준으로 비교함(다른객체일지라도)

    //참조 자체는 불변이지만, 참조하는 객체까지 불변인 것은 아닙니다. 예를 들어 record Order(List<String> items)에서 items 리스트는 내부 요소를 수정할 수 있습니다.
    //완전한 불변성을 원한다면 생성자에서 List.copyOf(items)처럼 방어적 복사를 사용하는 것이 좋습니다.
    //JPA 엔티티에는 일반적으로 적합하지 않습니다. JPA는 기본 생성자, 프록시, 상태 변경 등을 요구하는 경우가 많기 때문입니다.
    //DTO, API 요청/응답, 설정 객체, 값 객체(Value Object)처럼 데이터를 표현하는 용도에서 가장 큰 장점을 발휘합니다.

    //record는 "데이터를 담기 위한 불변 객체를 간결하게 표현하는 특별한 클래스"입니다.
    // 반복적인 생성자와 접근자, equals(), hashCode(), toString() 구현을 자동으로 제공하여 코드의 양을 크게 줄이고, 값 중심 객체를 안전하게 표현할 수 있습니다.
    // 특히 현대 Java와 Spring Boot에서는 DTO와 값 객체를 작성할 때 가장 많이 활용되는 기능 중 하나입니다.


    //장바구니 담기 요청
    public record Request(
            @NotNull Long itemId,
            @Min(1) int quantity
    ){}

    public record CreateOrderRequest(
            @NotEmpty List<Long> cartItemIds,
            @Valid ShippingAddress address

            ){}

    public record Response(
            Long cartItemid

    ){}

    public record DeleteRequest(
            @NotEmpty(message = "삭제할 상품을 하나 이상 선택해주세요.")
            List<Long> cartIds
    ){}

    //장바구니 전체목록 응답
    public record CartListResponse(
            List<CartItemReponse> cartItems,
            int totalPrice // 총결제 예정금액
    ){
        public static CartListResponse of(List<CartItemReponse>cartItems){
            int total=cartItems.stream()
                    .mapToInt(CartItemReponse::sumPrice)
                    .sum();
            return new CartListResponse(cartItems,total);
        }
    }

    //장바구니 개별 아이템 응답
    public record CartItemReponse(
            Long cartItemId,
            Long itemId,
            String itemName,
            int price,
            int quantity,
            //String imageUrl
            int sumPrice //price*quantity 항목별 총금액

    ){}

    public record CartItemQuantityUpdateRequest(
            @Min(value=1,message = "수량은 최소 1개 이상이어야 합니다.")
            int quantity
    ){}

    public record ShippingAddress(
            @NotBlank String zipcode,
            @NotBlank String city,
            @NotBlank String street
    ) {}

}
