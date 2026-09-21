package com.example.demo.service;

import com.example.demo.dto.OrderDto;
import com.example.demo.repository.order.OrderQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderQueryRepository orderQueryRepository;

    public Page<OrderDto.Response>searchOrders(OrderDto.OrderSearch orderSearch, Pageable pageable){
        return orderQueryRepository.searchOrders(orderSearch,pageable);
    }
}
