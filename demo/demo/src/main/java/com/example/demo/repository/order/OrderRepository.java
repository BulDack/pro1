package com.example.demo.repository.order;

import com.example.demo.entity.Order;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    //데이터가 없으면 null이 아니라 비어있는 리스트(Collections.emptyList())가 반환됨.
    // 따라서 이 메서드를 쓰고 나서는 if (orders == null) 같은 null 체크를 할 필요가 없음.
    List<Order>findByMemberId(Long memberId);
}
