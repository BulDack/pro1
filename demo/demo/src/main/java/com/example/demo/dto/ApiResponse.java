package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success; // 성공여부
    private String message; // 클라이언트에게 보여줄 메세지
    private T data; //실제 리액트가 쓸 데이터(없으면 null)

    // 편리하게 객체를 생성하기 위한 정적 팩토리 메서드들
    public static <T>ApiResponse<T> success(String message,T data){
        return new ApiResponse<>(true,message,data);
    }
    //List<OrderDto> dtos = orderService.findOrders();
    //
    //// 1단계: 리스트를 객체로 포장해서 count를 심는다. (작은 상자)
    //Result result = new Result(dtos.size(), dtos);
    //
    //// 2단계: 그 상자를 성공 메시지와 함께 택배 박스에 넣는다. (큰 택배 박스)
    //ApiResponse response = ApiResponse.success("조회 성공", result);
    //
    //// 3단계: 택배를 송장(HTTP 상태코드 200)을 붙여서 리액트에게 쏜다!
    //return ResponseEntity.ok(response);
    public static <T>ApiResponse<T> success(String message){
        return new ApiResponse<>(true,message,null);
    }
    public static <T>ApiResponse<T> fail(String message){
        return new ApiResponse<>(true,message,null);
    }

    //JSON
    //{
    //  "success": true,                       // ──┐
    //  "message": "주문 목록 조회 성공",       //   │ [1층: ApiResponse 영역]
    //  "data": {                              // ──┘ (모든 API가 이 세 필드를 가짐)
    //
    //    "count": 2,                          // ──┐
    //    "data": [                            //   │ [2층: Result 영역]
    //      { "orderId": 1, "status": "ORDER" },//   │ (리스트 데이터의 한계를 극복)
    //      { "orderId": 2, "status": "CANCEL" }// ──┘
    //    ]
    //
    //  }
    //}
    //문자열 하나를 보내더라도 성공 여부(success), 메시지(message), 결과 데이터(data) 구조를 갖춘 공통 응답 객체(DTO)를 만들어서
    // ResponseEntity<ApiResponse<T>> 형태로 반환하는 것이 현대 웹 개발의 글로벌 표준.
}
