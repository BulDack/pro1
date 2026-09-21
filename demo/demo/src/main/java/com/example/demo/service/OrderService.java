package com.example.demo.service;

import com.example.demo.dto.CartItemDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.entity.*;
import com.example.demo.entity.ennum.DeliveryStatus;
import com.example.demo.entity.item.CartItem;
import com.example.demo.entity.item.Item;
import com.example.demo.repository.order.CartItemRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.MemberJpaRepository;
import com.example.demo.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;

    //그냥 바로 주문할때
    @Transactional
    public OrderDto.Response order(Long memberId,
                      OrderDto.OrderItemRequestDto orderDto){
        //item테이블을 select...for update(비관적 락)로 직접 잠금조회
        Item item=itemRepository.findByIdWithPessimisticLock(orderDto.getItemId()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 상품입니다"));

        //내부적으로 재고를 깎고 당시의 json스펙 객체를 스냅샷 필드에 대입함
        OrderItem orderItem=OrderItem.createOrderItem(item, orderDto.getCount());
        long totalPrice=item.getPrice()*orderDto.getCount();


        Member memberRef=memberJpaRepository.getReferenceById(memberId);
        Address address=new Address(orderDto.getAddress().city(),orderDto.getAddress().street(),orderDto.getAddress().zipcode());
        Delivery delivery=Delivery.createDelivery(address, DeliveryStatus.READY);

        Order order=Order.createOrder(memberRef,delivery, List.of(orderItem),totalPrice);
        orderRepository.save(order);
        return new OrderDto.Response(order);
    }

    //장바구니에서 주문할때
    @Transactional
    public OrderDto.Response createCartOrder(Long memberId, CartItemDto.CreateOrderRequest request){
        //단순히 findAllById(request.cartItemIds())로 조회하지 않고 뒤에 AndUserId를 붙인 이유는 보안(권한 검증) 때문입니다.
        //⚠️ 만약 findAllById(cartItemIds)만 사용한다면?
        //문제점 (취약점): 악의적인 사용자가 API 요청 시 cartItemIds에 다른 사람의 장바구니 아이템 ID를 몰래 끼워 넣을 수 있습니다.
        //결과: 타인의 장바구니 아이템이 내 주문으로 들어가거나, 타인의 장바구니 아이템이 삭제되는 보안 사고(IDOR 취약점)가 발생합니다.

        //✅ AndUserId를 추가했을 때
        //조회 조건에 userId가 강제로 포함되므로, 요청한 cartItemIds 중 실제 로그인한 사용자의 장바구니에 속한 아이템만 조회됩니다.
        //다른 사용자의 장바구니 ID를 입력하더라도 DB 조회 단계에서 필터링되어 안전하게 무시되거나 예외 처리가 가능해집니다.

        //1.주문할 장바구니 항목들 조회("현재 로그인한 사용자의 장바구니 항목 중, 요청된 ID 목록에 해당하는 항목만 안전하게 조회")
        List<CartItem> cartItems= cartItemRepository.findAllByIdInAndUserId(request.cartItemIds(),memberId);
        if(cartItems.isEmpty()){
            throw new IllegalArgumentException("주문할 장바구니 항목이 존재하지 않습니다.");
        }

        // 상품 ID 추출 및 락 적용 조회
        List<Long> itemIds = cartItems.stream().map(ci -> ci.getItem().getId()).toList();
        Map<Long, Item> itemMap = itemRepository.findAllByIdInWithPessimisticLock(itemIds)
                .stream().collect(Collectors.toMap(Item::getId, i -> i));

        List<OrderItem>orderItems= new ArrayList<>();
        long totalPrice=0;

        for (CartItem cartItem: cartItems){
            Item item=itemMap.get(cartItem.getItem().getId());
            int quantity=cartItem.getCount();
            //재고차감
            item.removeStock(quantity);

            //주문 상품 생성
            OrderItem orderItem=OrderItem.createOrderItem(item,quantity);
            orderItems.add(orderItem);

            totalPrice+=item.getPrice()*quantity;
        }

        //SELECT 쿼리를 생략하고 프록시 객체만 전달하기 (성능 최적화)
        Member memberRef=memberJpaRepository.getReferenceById(memberId);
        Address address= new Address(request.address().city(), request.address().street(),request.address().zipcode());
        Delivery delivery=Delivery.createDelivery(address,DeliveryStatus.READY);

        Order order=Order.createOrder(memberRef,delivery,orderItems,totalPrice);
        orderRepository.save(order);

        //주문한 장바구니 항목 삭제(장바구니 비우기)
        cartItemRepository.deleteAll(cartItems);

        return new OrderDto.Response(order);
    }

    @Transactional
    public void cancelOrder(Long orderId){

        Order order=orderRepository.findById(orderId).orElseThrow(()->new IllegalArgumentException("해당하는 주문이 없습니다."));
        order.cancel();
    }

//    public List<Order> findOrders(Long memberId,OrderSearch orderSearch) {
//        List<Order> orders=orderRepository.findByMemberId(memberId);
//
//        return orders;
//    }
}
