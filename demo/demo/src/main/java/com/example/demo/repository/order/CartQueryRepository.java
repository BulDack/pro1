package com.example.demo.repository.order;

import com.example.demo.entity.item.CartItem;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.demo.entity.item.QCartItem.cartItem;
import static com.example.demo.entity.item.QItem.item;

@Repository
@RequiredArgsConstructor
public class CartQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 회원의 장바구니 아이템들을 상품 정보와 함께 한 번에 긁어오는 최적화 쿼리
     */
    public List<CartItem>findCartItemsWithItem(Long memberId){
        return queryFactory
                .selectFrom(cartItem)
                // fetchJoin을 통해 상품(Product)까지 쿼리 1번으로 묶어서 가져옴 (N+1 방지)
                .join(cartItem.item,item).fetchJoin()
                .where(cartItem.cart.member.id.eq(memberId))
                .orderBy(cartItem.id.desc()) // 최근에 담은 순서대로
                .fetch();


    }
}
