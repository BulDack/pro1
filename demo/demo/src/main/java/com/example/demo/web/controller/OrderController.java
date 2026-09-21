package com.example.demo.web.controller;

import com.example.demo.dto.CartItemDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.dto.security.PrincipalDetails;
import com.example.demo.dto.Result;
import com.example.demo.service.ItemService;
import com.example.demo.service.MemberService;
import com.example.demo.service.OrderQueryService;
import com.example.demo.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.dto.OrderDto.OrderSearch;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderQueryService orderQueryService;
    private final MemberService memberService;
    private final ItemService itemService;


    //@RequestBody는 jackson라이브러리가 json문자열을 가공해서 객체로 매핑해줌
    //@ModelAttribute는 클라이언트가 보낸 여러개의 http요청 파라미터(Query String이나 Form데이터)를 자바 객체로 한방에 묶어서 바인딩해줌

    //주문생성 api
    @PostMapping("/order")
    public ResponseEntity<OrderDto.Response> order(@AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody OrderDto.OrderItemRequestDto requestDto){
        OrderDto.Response response=orderService.order(principalDetails.getId(),requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //장바구니를 통한 주문
    @PostMapping("api/cart/order")
    public ResponseEntity<OrderDto.Response> cartOrder(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody CartItemDto.CreateOrderRequest requestDto){

        OrderDto.Response response=orderService.createCartOrder(principalDetails.getId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    //내 주문목록 조회
    @GetMapping("/orders")
    public Result getMyOrders(
            @AuthenticationPrincipal PrincipalDetails userDetails, //인증된 토큰에서 로그인한 유저 정보를 안전하게 꺼냄
            @ModelAttribute("orderSearch") OrderSearch orderSearch,
            Pageable pageable
     ){
        // 로그인한 본인의 ID를 강제로 세팅하여 남의 데이터 조회를 원천 차단!(로그인 아이디가 아닌 pk값을 썼음)
        Long loggedInMemberId= userDetails.getId();
        orderSearch.setMemberId(loggedInMemberId);

        Page<OrderDto.Response> orders= orderQueryService.searchOrders(orderSearch,pageable);

//        List<OrderDto.Response>result= orders.stream()
//                .map(OrderDto.Response::new)
//                .collect(Collectors.toList());

        //나중에 프론트엔드 개발자가 "주문 목록 상단에 [총 주문 건수: 2건]이라는 숫자를 띄우고 싶으니, 전체 개수 데이터도 같이 보내주세요!"라고 요청하면 문제가 복잡해짐.
        //현재 JSON의 최상위 노드가 대괄호([])로 시작하는 배열이기 때문에, 자바 코드를 수정하지 않고는 데이터 개수(count) 같은 새로운 필드를 끼워 넣을 자리가 없음.
        // 만약 억지로 넣으려면 JSON 구조 전체를 뜯어고쳐야 하므로, 이미 이 API를 쓰고 있던 리액트 코드들이 다 깨지게 됩니다.
        //@@ 해결책: 껍데기 객체 Result로 감싸기
        //이 문제를 아주 우아하게 해결하기 위해, 자바 객체(배열)를 한 번 더 감싸줄 Result라는 클래스를 우리가 직접 선언해서 쓴 것.


        return new Result(orders.getSize(),orders.getContent());//---------이거 아직 잘모른채 했음 다음에 바로 알아보기!!!!!
    }

    //주문취소
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId){
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("주문이 취소 되었습니다");
    }

//    @GetMapping(value="/order")
//    public String createForm(Model model){
//        List<Member>members=memberService.findMembers();
//        List<Item>items=itemService.findItems();
//
//        model.addAttribute("members",members);
//        model.addAttribute("items",items);
//
//        return "order/orderForm";
//    }
    //뒤에 매개변수들은 requestparam생략햇음
    //상품주문
//    @PostMapping(value="/order")
//    public String order(<>){
//        orderService.order(memberId,orderDto);
//        return "redirect:/orders";
//    }
//
//    //주문목록
//    @GetMapping(value="/orders")
//    public String orderList(@ModelAttribute("orderSearch")OrderSearch orderSearch,Model model){
//        List<Order>orders=orderService.findOrders(orderSearch); //아직 구현안함
//        model.addAttribute("orders",orders);
//        return "order/orderList";
//    }
//
//    @PostMapping(value="/orders/{orderId}/cancel")
//    public String cancelOrder(@PathVariable Long orderId){
//        orderService.cancelOrder(orderId);
//        return "redirect:/orders";
//    }
}
