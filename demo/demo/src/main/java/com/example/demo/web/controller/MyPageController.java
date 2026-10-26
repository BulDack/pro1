package com.example.demo.web.controller;

import com.example.demo.config.oauth.dto.PrincipalDetails;
import com.example.demo.dto.ApiResponse;
import com.example.demo.service.MemberService;
import com.example.demo.service.item.ItemService;
import com.example.demo.service.order.OrderService;
import com.example.demo.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import static com.example.demo.dto.my.MyResponse.MemberResponse;
import static com.example.demo.dto.my.MyResponse.PostResponse;
import static com.example.demo.dto.my.MyResponse.ItemResponse;
import static com.example.demo.dto.my.MyResponse.OrderResponse;


@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
//마이페이지
public class MyPageController {

    private final MemberService memberService;
    private final ItemService itemService;
    private final OrderService orderService;
    private final PostService postService;

    //내 회원 정보 조회
    @GetMapping
    public ApiResponse<MemberResponse> getMe(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ){
        System.out.println("getMe 컨트롤러 실행");
        Long memberId = userDetails.getId();

        System.out.println("Principal 존재: " + (userDetails != null));
        return ApiResponse.success("마이페이지를 엽니다.",
                memberService.getMe(memberId));

    }

    //내가 등록한 상품조회
    @GetMapping("/items")
    public ApiResponse<Page<ItemResponse>>getMyItems(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            Pageable pageable
    ){

        return ApiResponse.success( "내 상품조회합니다",
                itemService.getMyItems(
                        userDetails.getId(),
                        pageable
                )
        );

    }

    //내가 등록한 게시글 조회
    @GetMapping("/items")
    public ApiResponse<Page<PostResponse>>getMyPosts(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            Pageable pageable
    ){

        return ApiResponse.success( "내 게시물을 조회합니다",
                postService.getMyPosts(
                        userDetails.getId(),
                        pageable
                )
        );

    }

//    //내 주문 조회
//    @GetMapping("/orders")
//    public ApiResponse<Page<OrderSummaryResponse>>getMyItems(
//            @AuthenticationPrincipal PrincipalDetails userDetails,
//            Pageable pageable
//    ){
//
//        return ApiResponse.success( "내 상품조회합니다",
//                postService.getMyOrders(
//                        userDetails.getId(),
//                        pageable
//                )
//        );
//
//    }

}
