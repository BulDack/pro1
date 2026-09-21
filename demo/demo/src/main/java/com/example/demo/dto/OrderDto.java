package com.example.demo.dto;

import com.example.demo.entity.Order;
import com.example.demo.entity.ennum.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderDto {


    @Getter
    @NoArgsConstructor
    public static class OrderItemRequestDto {
        private Long ItemId; //주문할 상품 id
        private int count; //주문 수량
        private ShippingAddress address;
    }

    //주문하기 요청 dto
    @Getter
    @NoArgsConstructor
    public static class Request {
        private List<OrderItemRequestDto> orderItems;

        private String city;
        private String street;
        private String zipcode;
    }

    //주문내역 결과 dto
    //record는 정의할 때 적은 매개변수 구조가 그대로 필수 생성자(AllArgsConstructor)가 됨
    //record는 불변(immutable) 객체라 빈껍데기(new Record())로는 생성불가-생성시점에 반드시 값이 정해져 있어야 한다는 뜻
    public record Response(
            Long orderId,
            LocalDateTime orderDate,
            OrderStatus orderStatus,
            int totalPrice
    ){
        // 컴팩트 생성자: 엔티티를 받아서 바로 맵핑해주는 또 다른 생성자 정의
        public Response(Order order){
            this(
                    order.getId(),
                    order.getOrderdate(),
                    order.getStatus(),
                    order.getTotalPrice()
            );
        }

    }

    //주문검색 조건(동적)
    @Getter@Setter
    public static class OrderSearch {

        private OrderStatus orderStatus;

        private Long memberId;

        //기간조회
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }

    public record ShippingAddress(
            String zipcode,
            String city,
            String street
    ) {}

}
