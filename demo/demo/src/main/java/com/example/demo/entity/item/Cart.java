package com.example.demo.entity.item;

import com.example.demo.entity.Member;
import com.example.demo.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

//비즈니스적으로 주가 되는 테이블(주로 접근하는 테이블)에 외래 키를 두는 것이 JPA 개발 시 훨씬 편하고 성능상 유리함.
//단, 향후 비즈니스가 확장되어 1:1 관계가 1:N(일대다) 관계로 변할 가능성이 조금이라도 보인다면,
// 대상 테이블에 외래 키를 두고 유니크 제약조건을 거는 것이 DB 설계 변경을 최소화하는 방법임.
@Entity
@Getter
@Table(name = "cart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {

    @Id@GeneratedValue
    @Column(name="cart_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "member_id",unique = true)
    private Member member;

    @OneToMany(mappedBy = "cart",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<CartItem> cartItems=new ArrayList<>();

    //연관 편의 메서드:장바구니에 아이템추가
    public void addCartItem(CartItem cartItem){
        this.cartItems.add(cartItem);
        cartItem.addCart(this);
    }

    public static Cart createCart(Member member){
        Cart cart= new Cart();
        cart.member=member;
        return cart;
    }
//2. 왜 Cart에 외래 키를 두는 게 좋을까?
//① 비즈니스적 확장성 (가장 큰 이유)
//지금은 "회원 한 명당 장바구니 한 개"인 일대일(1:1) 관계이지만, 커머스 서비스가 커지다 보면 비즈니스 요구사항이 바뀔 수 있습니다.
//ex) "일반 장바구니 말고 '선물하기 장바구니', '정기구독 장바구니'처럼 회원이 장바구니를 여러 개 만들 수 있게 해주세요."
//만약 Cart에 member_id가 있다면, Cart 테이블의 유니크(Unique) 제약조건만 제거하면 별도의 테이블 구조 변경 없이 바로 일대다(1:N) 관계로 전환할 수 있습니다
// 반대로 Member에 cart_id를 뒀었다면 테이블 구조를 완전히 갈아엎는 대공사를 해야 합니다.
//
//② 객체의 생명주기와 데이터 정제성!!
//보통 회원이 가입할 때 장바구니가 바로 생기기도 하지만, 장바구니는 비어있거나 아예 안 만들었다가 첫 상품을 담을 때 생성되는 경우도 많습니다.
//만약 Member에 cart_id가 있다면, 장바구니가 없는 회원은 cart_id에 null이 들어가게 됩니다.
//반면 Cart에 member_id를 두면, Member 테이블은 깔끔하게 유지되고 장바구니가 필요할 때만 Cart 레코드를 생성하면 되므로 데이터 관리가 훨씬 깔끔합니다

//⚠️ 주의할 점 (JPA 지연 로딩 특성)
//앞서 설명드렸듯 대상 테이블(Cart)에 외래 키를 두면 한 가지 단점이 존재합니다.
//Member를 조회할 때 cart를 지연 로딩(FetchType.LAZY)으로 설정하더라도,
//Member 입장에서는 자신에게 FK가 없기 때문에 DB의 Cart 테이블을 열어보기 전까지는 내 장바구니가 존재하긴 하는지(null인지 프록시인지) 알 방법이 없습니다.
// 그래서 Member를 조회할 때 Cart를 확인하는 쿼리가 추가로 나가는 현상(프록시 한계로 인한 즉시 로딩화)이 발생할 수 있습니다.
//💡 해결 책:
//어차피 회원 마이페이지나 주문 화면 등에서 Member와 Cart를 동시에 쓰는 경우가 많으므로,
// 이 관계는 조회 시점에 **JPQL 패치 조인(fetch join)**이나 **QueryDSL의 fetchJoin()**을 사용해서 한 번에 묶어서 가져오는 방식으로 성능을 최적화하는 것이 실무 정석입니다.
}
