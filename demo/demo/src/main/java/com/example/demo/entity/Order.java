package com.example.demo.entity;

import com.example.demo.entity.ennum.DeliveryStatus;
import com.example.demo.entity.ennum.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
@Getter
//MySQL(InnoDB)은 FOREIGN KEY 제약조건을 걸면 자동으로 인덱스를 생성해 주지만,
// FK 제약조건 없이 매핑만 한 경우나 명시적 관리를 위해 인덱스 확인이 필수!!.
public class Order {

    @Id@GeneratedValue
    @Column(name="order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<OrderItem> orderItems =new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name="delivery_id")
    private Delivery delivery;

    private LocalDateTime orderdate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Long totalPrice;

    private void setMember(Member member){
        this.member=member;
        member.getOrders().add(this);
    }

    private void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.addOrder(this);
    }

    private void setDelivery(Delivery delivery){
        this.delivery=delivery;
        delivery.setOrder(this);
    }
    //생성메서드
    public static Order createOrder(Member member,Delivery delivery,List<OrderItem> orderItems,Long totalPrice){

        Order order= new Order();
        //연관관계 메서드(order가 생성될때 member와 delievery에도 넣어줌
        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem:orderItems){
            order.addOrderItem(orderItem); //연관관계메서드
        }
        order.totalPrice=totalPrice;
        order.status=OrderStatus.ORDER;
        order.orderdate=LocalDateTime.now();
        return order;
    }
    //주문취소
    public void cancel(){
        if(delivery.getDeliveryStatus()== DeliveryStatus.COMP){
            throw new IllegalStateException("이미 배송완료된 상품은 취소 불가능합니다");
        }
        this.status=OrderStatus.CANCEL;
        for(OrderItem orderItem: orderItems){
            orderItem.cancel();
        }
    }
    public int getTotalPrice(){
        int totalPrice=0;
        for(OrderItem orderItem:orderItems){
            totalPrice+=orderItem.getTotalPrice();
        }
        return totalPrice;
    }



}
