package com.example.demo.entity;

import com.example.demo.convert.ItemDetailConverter;
import com.example.demo.entity.item.Item;
import com.example.demo.entity.item.ItemDetailSpec;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name="order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 외부에서 new를통해 생성자객체 생성 방지
public class OrderItem {

    @Id@GeneratedValue
    @Column(name="order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="item_id")
    private Item item;

    private int orderPrice;
    private int count;

//    쇼핑몰 운영 중 관리자가 상품의 CPU 사양을 바꾸거나 옷 정보를 수정하더라도, 사용자의 기존 주문서 데이터는 보존되어야 합니다.
//    따라서 OrderItem에도 주문 당시의 JSON 스펙 객체를 통째로 복사(스냅샷)해서 저장합니다.
    @Convert(converter = ItemDetailConverter.class)
    @Column(columnDefinition = "json",name="spec_snapshot")
    private ItemDetailSpec specSnapshot;

    public static OrderItem createOrderItem(Item item,int count){
        OrderItem orderItem=new OrderItem();
        orderItem.item=item;
        orderItem.orderPrice=item.getPrice();
        orderItem.count=count;

        // 중요: 주문 시점의 JSON 객체를 스냅샷으로 그대로 복사하여 저장
        // (깊은 복사 처리를 하거나, 단순 매핑을 위해 객체를 그대로 대입)
        orderItem.specSnapshot=item.getDetailSpec();

        // 상품 재고 차감
        item.removeStock(count);
        return orderItem;
    }
    public int getTotalPrice() {
        return getOrderPrice()*getCount();
    }

    public void cancel() {
        //주문 취소시 재고 원복
        getItem().addStock(count);
    }

    public void addOrder(Order order) {
        this.order=order;
    }
}
