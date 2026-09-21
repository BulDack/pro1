package com.example.demo.repository.order;

import com.example.demo.entity.item.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    /**
     * 특정 장바구니(cartId) 안에 특정 상품(ItemId)이 이미 담겨있는지 단건 조회
     * (이미 담겨있다면 새 로우를 파지 않고 수량만 더해주는 addCount() 로직을 실행하기 위함)
     */

    Optional<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);

    List<CartItem> findAllByIdInAndUserId(List<Long> cartItemId,Long memberId);

    // 사용자의 장바구니에 속한 지정된 ID 목록의 아이템들을 한 번에 삭제
    @Modifying(clearAutomatically = true) //Spring Data JPA에서 UPDATE, DELETE 같은 변경 쿼리를 실행한 후 영속성 컨텍스트를 자동으로 비워주는 옵션(값이 업데이트 되고 나면 실제 db랑 영속성 컨텍스트랑 다를수 있기때문)
    @Query("DELETE FROM CartItem ci WHERE ci.id IN :cartItemIds AND ci.cart.member.id = :memberId")
    void deleteAllByIdsAndMemberId(@Param("cartItemIds")List<Long>cartItemIds,@Param("memberId")Long memberId);

    // Member ID로 장바구니 아이템 목록과 상품(Item) 정보를 한 번에 조회
    @Query("SELECT ci FROM CartItem ci " +
            "JOIN FETCH ci.item i " +
            "WHERE ci.cart.member.id = :memberId " +
            "ORDER BY ci.id DESC")
    List<CartItem> findAllByMemberIdFetchItem(@Param("memberId") Long memberId);

    //member 테이블은 조인되지 않음 (최적화)
    //Member 엔티티의 다른 필드(이름, 이메일 등)를 조회하는 것이 아니라 외래키 값인 member.id 조건만 확인하는 것이기 때문에, JPA가 알아서 최적화하여 member 테이블까지는 JOIN하지 않고 cart 테이블만 조인합니다.
    @Query("SELECT ci FROM CartItem ci " +
            "WHERE ci.id = :cartItemId AND ci.cart.member.id = :memberId")
    Optional<CartItem> findByIdAndMemberId(@Param("cartItemId") Long cartItemId, @Param("memberId") Long memberId);
}
