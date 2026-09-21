package com.example.demo.repository.order;

import com.example.demo.entity.item.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long> {

    /**
     * 회원 ID를 기반으로 장바구니를 조회하는 메서드
     * (장바구니 담기 서비스 등에서 유저의 기존 장바구니가 있는지 확인할 때 사용합니다.)
     */

    Optional<Cart> findByMemberId(Long memberId);
}
