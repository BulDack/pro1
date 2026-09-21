package com.example.demo.entity.item;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

//실제 어떤 상품이 몇 개 담겼는지는 이 테이블에서 관리합니다.
//여기서 아주 중요한 설계 포인트가 있습니다.
// OrderItem(주문 상품)과 달리 CartItem은 당시의 가격이나 JSON 스펙을 스냅샷으로 복사해 둘 필요가 전혀 없습니다.
// 장바구니는 늘 '현재 판매 중인 실시간 데이터'를 보여주어야 하기 때문입니다.
// 따라서 단순하게 Item 테이블을 참조(@ManyToOne)하기만 하면 됩니다.
//cart와 item사이의 다대다(N:M) 관계의 한계 해소,중간 엔티티에 필수 데이터 추가(단순히 다대다 연결만 하는게 아니라 필요한 속성들을 추가 할수 있음)
@Entity
@Getter
@Table(name = "cart_item")
@NoArgsConstructor
public class CartItem {

    @Id@GeneratedValue
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;  //실시간 상품엔티티 참조

    @Column(nullable = false)
    private int count;  //장바구니에 담은 수량

    public static CartItem createCartItem(Cart cart,Item item,int count){
        CartItem cartItem=new CartItem();
        cartItem.item=item;
        cartItem.count=count;
        cart.addCartItem(cartItem);
        return cartItem;
    }

    public void addCart(Cart cart){
        this.cart=cart;
    }

    //비즈니스 로직

    //이미 장바구니에 있는 상품을 또 담았을때 수량만 증가시키는 로직
    public void addCount(int count){
        this.count+=count;
    }

    //장바구니 화면에서 직접 수량을 변경할때 사용하는 로직

    public void updateCount(int count){
        if(count<=0){
            throw new IllegalArgumentException("수량은 1개이상이여야 합니다");
        }
        this.count=count;
    }


}
