package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // 역직렬화나 유연성을 위해 기본 생성자도 추가

//Result는 JSON 응답의 최상위를 배열([])이 아닌 객체({}) 형태로 만들기 위해 개발자가 직접 만든 Wrapper(감싸기) 클래스
public class Result<T> {

    private int count; //여기에 전체 개수 같은 메타데이터를 담음
    private T data;  //여기에 실제 리스트(ex.List<orderDTO.response>를 담음

    // Result를 썼을 때의 최종 JSON 모양
    //이렇게 반환하면 최상위 노드가 대괄호([])가 아니라 중괄호({})를 가진 JSON 객체로 이쁘게 포장되어 나갑니다.
    //JSON
    //{
    //  "count": 2,
    //  "data": [
    //    { "orderId": 1, "status": "ORDER" },
    //    { "orderId": 2, "status": "CANCEL" }
    //  ]
    //}
    //👍 이렇게 하면 무엇이 좋은가요? (확장성)
    //나중에 "서버 현재 시간"이나 "페이지 번호" 같은 추가 정보가 필요해지더라도, 기존 구조를 전혀 건드리지 않고 Result 클래스에 필드만 슥 추가하면 끝남.

    //===========================================================================================

    //① Result 객체의 역할: "배열(List) 해결사"
    //존재 이유: 자바의 List 객체를 JSON으로 바꿀 때, 최상단이 대괄호([]) 배열로 나가는 것을 막고 중괄호({}) 객체로 감싸기 위해 만듭니다.
    //
    //담기는 내용: 오직 데이터의 개수(count)와 진짜 알맹이 리스트(data)만 담습니다.
    //
    //② ApiResponse 객체의 역할: "공통 통신 규격 (최상위 박스)"
    //존재 이유: 어떤 API를 호출하든 리액트(클라이언트)가 항상 똑같은 포맷으로 응답을 받아서 처리할 수 있도록 전방위 시스템용으로 만듭니다.
    //
    //담기는 내용: 성공 여부(success), 화면에 띄울 메시지(message), 그리고 진짜 결과물(data)을 담습니다.
}
