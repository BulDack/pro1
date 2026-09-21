package com.example.demo.web.controller;

import com.example.demo.dto.CartItemDto;
import com.example.demo.dto.security.PrincipalDetails;
import com.example.demo.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.dto.CartItemDto.Request;
import static com.example.demo.dto.CartItemDto.Response;
import static com.example.demo.dto.CartItemDto.CartListResponse;
import static com.example.demo.dto.CartItemDto.CartItemQuantityUpdateRequest;

@RestController
@RequestMapping("api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    //장바구니에 상품추가
    @PostMapping("/items")
    public ResponseEntity<Response>addCartItem(
            @AuthenticationPrincipal PrincipalDetails principalDetails, //인증된 사용자
            @Valid @RequestBody Request request){
        //카트에 아이템 담고 cartItemId값 반환
        Long cartItemId=cartService.addCart(principalDetails.getId(), request.itemId(), request.quantity());
        Response response=new Response(cartItemId);
        return ResponseEntity.ok(response);

    }

    //장바구니에 상품삭제
    @DeleteMapping("/items/")
    public ResponseEntity<Void>removeCartItem(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody CartItemDto.DeleteRequest request){

        // 본인의 장바구니 아이템인지 확인 및 삭제 로직 실행
        cartService.removeCartItem(principalDetails.getId(),request.cartIds());

        // 별도의 반환 데이터 없이 200 OK (또는 204 No Content) 응답
        return ResponseEntity.ok().build();
    }

    //장바구니 목록 조회
    @GetMapping("/items")
    public ResponseEntity<CartListResponse>getCartItems(
            @AuthenticationPrincipal PrincipalDetails principalDetails
            ){
        CartListResponse response=cartService.getCartList(principalDetails.getId());
        return ResponseEntity.ok(response);
    }

    //장바구니 수량 수정
    @PatchMapping("items/{cartItemId}")
    public ResponseEntity<Void>updateCartItemQuantity(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable ("cartItemId") Long cartItemId,
            @Valid @RequestBody CartItemQuantityUpdateRequest request
    ){
        cartService.updateCartItemQuantity(principalDetails.getId(),cartItemId,request.quantity());

        return ResponseEntity.ok().build();
    }

}
