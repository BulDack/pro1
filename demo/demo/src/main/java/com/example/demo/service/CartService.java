package com.example.demo.service;

import com.example.demo.entity.Member;
import com.example.demo.entity.item.Cart;
import com.example.demo.entity.item.CartItem;
import com.example.demo.entity.item.Item;
import com.example.demo.repository.order.CartItemRepository;
import com.example.demo.repository.order.CartRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.demo.dto.CartItemDto.CartListResponse;
import static com.example.demo.dto.CartItemDto.CartItemReponse;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartService {

    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final MemberJpaRepository memberRepository;
    private final CartItemRepository cartItemRepository;


    @Transactional
    //아이템 카트에 추가
    public Long addCart(Long memberId,Long ItemId,int count){

        //1.담으려는 상품조회
        Item item=itemRepository.findById(ItemId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 상품입니다"));

        //2.해당회원의 장바구니 조회(없으면 새로 생성)
        Cart cart=cartRepository.findByMemberId(memberId).orElseGet(()->{
            // findById 대신 getReferenceById를 사용하면 Member 조회 SELECT 쿼리조차 실행되지 않고
            // memberId를 가진 프록시(가짜) 객체만 할당하여 Cart 생성
            Member memberRef=memberRepository.getReferenceById(memberId);
            return cartRepository.save(Cart.createCart(memberRef));
        });

        // 3. CartItem 조회 및 수량 추가 or 신규 저장
        return cartItemRepository.findByCartIdAndItemId(cart.getId(),item.getId())
                .map(cartItem -> {
                    //이미 카트상품이 있다면 수량만 더하고 cartItemID반환
                    cartItem.addCount(count);
                    return cartItem.getId();
                }).orElseGet(()->{
                    //카트 상품이 없다면 새로 생성및 연관관계 맺고 저장후 ID반환
                    CartItem newCartItem= CartItem.createCartItem(cart,item,count);
                    return cartItemRepository.save(newCartItem).getId();
                });

        //장바구니 마스터만 영속화(cascadeType.all 설정덕분에 자식인 cartItem도 함께 db에 저장됨)
        // cartRepository.save(cart);

    }


    //카트에서 아이템 삭제
    @Transactional
    public void removeCartItem(Long id, List<Long> cartItemId) {

        if (cartItemId==null || cartItemId.isEmpty()){
            return;
        }

        // 본인 장바구니의 아이템만 삭제되도록 memberId 조건 포함
        cartItemRepository.deleteAllByIdsAndMemberId(cartItemId,id);

    }

    //카트조회
    @Transactional
    public CartListResponse getCartList(Long memberId) {

        List<CartItem> cartItems=cartItemRepository.findAllByMemberIdFetchItem(memberId);

        List<CartItemReponse> cartItemReponses=cartItems.stream()
                .map(item->new CartItemReponse(
                        item.getId(),
                        item.getItem().getId(),
                        item.getItem().getName(),
                        item.getItem().getPrice(),
                        item.getCount(),
                        item.getItem().getPrice()*item.getCount()
                )).toList();

        return CartListResponse.of(cartItemReponses);
    }

    //카트에서 아이템 수량 조절
    public void updateCartItemQuantity(Long memberId, Long cartItemId, int quantity) {

        CartItem cartItem = cartItemRepository.findByIdAndMemberId(cartItemId, memberId)
                .orElseThrow(() -> new AccessDeniedException("해당 장바구니 상품이 없거나 수정 권한이 없습니다."));
        cartItem.updateCount(quantity);
    }



}
