package com.example.demo.repository.order;


import com.example.demo.dto.OrderDto;
import com.example.demo.entity.Order;
import com.example.demo.entity.QOrder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import static com.example.demo.entity.QOrder.order;
import static com.example.demo.dto.OrderDto.Response;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Response> searchOrders(OrderDto.OrderSearch orderSearch, Pageable pageable){

        //1.실제 데이터 조회 쿼리
        List<Response> content=queryFactory
                .select(Projections.fields(
                        Response.class,
                        order.id,
                        order.orderdate,
                        order.status
                ))
                .from(order)
                .where(
                        order.member.id.eq(orderSearch.getMemberId()),
                        order.status.eq(orderSearch.getOrderStatus()),
                        dateBetween(orderSearch.getStartDate(),orderSearch.getEndDate())
                )
                .offset(pageable.getOffset()) //페이지 시작번호 (0부터시작)
                .limit(pageable.getPageSize())  // 한 페이지에 보여줄 개수
                .fetch();

        //2.전체 데이터 개수(COUNT)조회 쿼리
        JPAQuery<Long> countQuery= queryFactory
                .select(order.count())
                .from(order)
                .where(
                        order.member.id.eq(orderSearch.getMemberId()),
                        order.status.eq(orderSearch.getOrderStatus()),
                        dateBetween(orderSearch.getStartDate(),orderSearch.getEndDate())
                );

        //3.page객체 생성및 반환(최적화가능)
        //이 유틸리티 메서드는 똑똑하게도 카운트 쿼리를 실행하지 않아도 되는 상황일 때는 카운트 쿼리를 생략해 줌
        //(1)첫 페이지인데 가져온 데이터 건수가 페이지 사이즈보다 적을 때 (예: 한 페이지에 20개씩 보기로 했는데 전체 데이터가 12개뿐일 때)
        //(2)마지막 페이지일 때 (Offset + 현재 데이터 건수 = 전체 데이터 개수임을 알 수 있을 때)
        return PageableExecutionUtils.getPage(content,pageable,countQuery::fetchOne);
    }

    private BooleanExpression dateBetween(LocalDate startDate,LocalDate endDate){
        //파라미터가 localdate니까 비교할때는 localDateTime으로 변환

        LocalDateTime startAt = startDate != null ? startDate.atStartOfDay() : null; // 00:00:00
        LocalDateTime endAt = endDate != null ? endDate.atTime(LocalTime.MAX) : null; // 23:59:59.999999

        if(startAt==null && endAt==null){
            return null; //둘다 없으면 조건 생략
        }
        if(startAt==null){
            return order.orderdate.loe(endAt); //시작일이 없으면 종료일 이전만 검색
        }
        if(endAt ==null){
            return order.orderdate.goe(startAt); //종료일만 없으면 시작일 이후만 검색
        }
        return order.orderdate.between(startAt,endAt);
    }
}
