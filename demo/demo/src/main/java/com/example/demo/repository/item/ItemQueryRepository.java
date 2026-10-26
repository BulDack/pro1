package com.example.demo.repository.item;

import com.example.demo.dto.home.ItemSummaryResponse;
import com.example.demo.ennum.ItemSort;
import com.example.demo.ennum.SearchOperator;
import com.example.demo.entity.QCategory;
import com.example.demo.entity.ennum.ItemStatus;
import com.example.demo.entity.ennum.OrderStatus;
import com.example.demo.entity.item.QItemSearchAttribute;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.demo.dto.ItemDto.ItemListResponse;
import static com.example.demo.dto.ItemDto.ItemSearchRequest;
import static com.example.demo.dto.ItemDto.ItemDetailResponse;
import static com.example.demo.entity.QItemCategory.itemCategory;
import static com.example.demo.entity.item.QItem.item;
import static com.example.demo.entity.QCategory.category;
import static com.example.demo.entity.QOrder.order;
import static com.example.demo.entity.QOrderItem.orderItem;




@Repository
@RequiredArgsConstructor
public class ItemQueryRepository {

    private final JPAQueryFactory queryFactory;

    //아이템 목록들 조회(검색도 가능)
    public Page<ItemListResponse> searchItems(ItemSearchRequest cond, Long categoryId, Pageable pageable, ItemSort sort){

        //1.실제 데이터 조회 쿼리
        List<ItemListResponse> content=queryFactory
                .select(Projections.constructor(
                        ItemListResponse.class,
                        item.id,
                        item.name,
                        item.price,
                        item.thumbnailUrl
                ))
                .from(item)
                //// Item과 Category가 ItemCategory(다대다 중간 엔티티)로 연결된 경우 조인
                //.leftJoin(item.itemCategorys,itemCategory)
                .where(
                       //nameLike(cond.getName())
                        nameContains(cond.name()),
                        priceGoe(cond.priceGoe()),
                        priceLoe(cond.priceLoe()),
                        categoryIn(categoryId),
                        item.status.eq(ItemStatus.ACTIVE)

                        // ★ 핵심 속성 테이블(Key-Value)에 대응하는 동적 서브쿼리들
                        //hasAttribute("MINRAM",cond.getClothingSize()),
                        //hasAttribute("COLOR",cond.getColor()),
                        //hasAttribute("MINRAM",cond.getMinRam(), GOE)

                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderBy(sort.name()))
                .fetch();

        //2.전체 데이터 개수(COUNT)조회 쿼리
        JPAQuery<Long> countQuery= queryFactory
                .select(item.count())
                //.leftJoin(item.itemCategorys,itemCategory)
                .from(item)
                .where(
                        nameContains(cond.name()),
                        priceGoe(cond.priceGoe()),
                        priceLoe(cond.priceLoe()),
                        categoryIn(categoryId),
                        item.status.eq(ItemStatus.ACTIVE)

                        // ★ 핵심 속성 테이블(Key-Value)에 대응하는 동적 서브쿼리들
                        //hasAttribute("MINRAM",cond.getClothingSize()),
                        //hasAttribute("COLOR",cond.getColor()),
                        //hasAttribute("MINRAM",cond.getMinRam(), GOE)
                );
        //3.page객체 생성및 반환(최적화가능)
         return PageableExecutionUtils.getPage(content,pageable,countQuery::fetchOne);

    }

    //메인화면에 찜 높은순으로 보여지는 쿼리
    public List<ItemSummaryResponse> findPopularItems(int limit) {

        LocalDateTime sevenDaysAgo =
                LocalDateTime.now().minusDays(7);

        return queryFactory
                .select(Projections.constructor(ItemSummaryResponse.class,
                        item.id,
                        item.name,
                        item.price,
                        item.thumbnailUrl,
                        item.likeCount))
                .from(item)
                //.from(orderItem)
                //orderItem이 어떤상품인지 정보를 알기위해 item과 이너조인
                //.join(orderItem.item,item)
                //최근 7일주문인지 확인하려면 order의 주문일시가 필요
                //.join(orderItem.order,order)
                //.where(order.orderdate.goe(sevenDaysAgo),
                //        order.status.eq(OrderStatus.COMPLETE))
//                .groupBy(
//                        item.id,
//                        item.name,
//                        item.price,
//                        item.thumbnailUrl
//                )
//                .orderBy(Item.count.sum().asc())
                .where(item.status.eq(ItemStatus.ACTIVE))
                .orderBy(item.likeCount.desc())
                .limit(limit)
                .fetch();
    }

    //메인화면에 신상품 순으로 보여지는 쿼리
    public List<ItemSummaryResponse> findNewItems(int limit) {

        return queryFactory
                .select(Projections.constructor(ItemSummaryResponse.class,
                        item.id,
                        item.name,
                        item.price,
                        item.thumbnailUrl,
                        item.likeCount))
                .from(item)
                .where(item.status.eq(ItemStatus.ACTIVE))
                .orderBy(item.createdDate.desc())
                .limit(limit)
                .fetch();

    }

    /*
     * 상품명 검색 조건
     *
     * 1. name == null
     *    → 검색 조건 자체가 전달되지 않은 경우
     *
     * 2. name.isBlank()
     *    → 빈 문자열이나 공백만 전달된 경우
     *
     * 위 두 상황에서는 검색 조건을 적용하지 않도록
     * BooleanExpression 대신 null을 반환한다.
     *
     * QueryDSL의 where()는 null인 조건을 무시한다.
     */
    private BooleanExpression nameContains(String name){
        return name==null || name.isBlank()
                ? null
                : item.name.containsIgnoreCase(name);
    }
    /*
     * 최소 가격 검색 조건
     *
     * price가 null이면 최소 가격 제한을 적용하지 않는다.
     * 0원은 유효한 값이므로 null 여부만 검사한다.
     */
    private BooleanExpression priceGoe(Integer price) {
        return price == null
                ? null
                : item.price.goe(price);
    }

    /*
     * 최대 가격 검색 조건
     *
     * price가 null이면 최대 가격 제한을 적용하지 않는다.
     */
    private BooleanExpression priceLoe(Integer price) {
        return price == null
                ? null
                : item.price.loe(price);
    }


    private BooleanExpression categoryIn(Long categoryId) {
        if (categoryId == null){

            System.out.println("카테고리 검색 조건 없음");
            return null;
        }

        QCategory category = new QCategory("searchCategory");
        QCategory parent = new QCategory("parentCategory");
        QCategory grandParent = new QCategory("grandParentCategory");

        /*
         * EXISTS 서브쿼리
         *
         * 현재 조회 중인 Item이 categoryIds에 포함된
         * 카테고리 중 하나에 연결되어 있는지 확인한다.
         *
         * itemCategory.item.eq(item)
         * → 현재 바깥쪽 쿼리의 Item과 연결된 데이터인지 확인
         *
         * itemCategory.category.id.in(categoryIds)
         * → 전달받은 카테고리 목록 중 하나와 일치하는지 확인
         *
         * EXISTS
         * → 조건을 만족하는 ItemCategory가 하나 이상 있으면 true
         */
        return JPAExpressions
                .selectOne()
                .from(itemCategory)
                .join(itemCategory.category,category)
                .leftJoin(category.parent,parent)
                .leftJoin(parent.parent,grandParent) //셀프조인(카테고리 부모의 부모를 조인해옴)
                .where(
                        itemCategory.item.eq(item), //현재 조회중인 상품에 연결된 카테고리인지 확인
                        itemCategory.category.id.in(categoryId)
                                .or(parent.id.in(categoryId))
                                .or(grandParent.id.in(categoryId))

                )
                .exists();
    }
    /**
     * Key-Value 형태의 검색 속성 테이블을 필터링하기 위한 동적 서브쿼리 생성기
     * 이 연결 테이블 방식의 유일한 약점은 검색할 때 조인이나 서브쿼리가 유발됨.
     * 성능 저하를 완벽하게 막으려면 ProductSearchAttribute 테이블에 복합 인덱스를 반드시 걸어주어야 함.
     * CREATE INDEX idx_attr_key_value ON product_search_attribute(attribute_key, attribute_value);
     */
    private BooleanExpression hasAttribute(String key,String value){
        if(value==null||value.isBlank()) return null;

        QItemSearchAttribute subAttr=new QItemSearchAttribute("subAttr");

        JPQLQuery<Long> subQuery= JPAExpressions
                .select(subAttr.item.id)
                .from(subAttr)
                .where(subAttr.attributeKey.eq(key),subAttr.attributeValue.eq(value));

        return item.id.in(subQuery);

    }
    private BooleanExpression hasAttribute(String key, Integer value,SearchOperator operator){
        if(value==null) return null;

        QItemSearchAttribute subAttr=new QItemSearchAttribute("subAttr");

        JPQLQuery<Long> subQuery= JPAExpressions
                .select(subAttr.item.id)
                .from(subAttr)
                .where(subAttr.attributeKey.eq(key), operator.apply(subAttr.numberValue,value));

        //현재 DB의 attributeValue 컬럼은 문자열(VARCHAR) 타입
        //이를 막기 위해 db안에 잇는 attributeValue 안에 들어있는 문자열을
        //'진짜 숫자(UNSIGNED)'로 바꾼 다음에 크기를 비교하는 부분
//        if("goe".equals(operator)){ //goe이부분도 enum으로 표현가능할거같기도
//            subQuery.where(
//                    Expressions.numberTemplate(Integer.class,"CAST({0} AS USSIGNED)",subAttr.attributeValue).goe(value)
//            );
//        }else {
//            subQuery.where(subAttr.attributeValue.eq(String.valueOf(value)));
//        }
//        if("goe".equals(operator)){
//            subQuery.where(subAttr.numberValue.goe(value));
//        }else {
//            subQuery.where(subAttr.numberValue.eq(value));
//        }


        return item.id.in(subQuery);
    }


    //정렬조건을 동적으로 생성하는 헬퍼 메서드
    private OrderSpecifier<?>getOrderBy(String sortBy){
        if(sortBy==null)return item.id.desc();

        //이부분도 왠지 ENUM으로 해도 될거같은데..흐음
        switch (sortBy){
            case "PRICE_ASC": return item.price.asc();
            case "PRICE_DESC": return  item.price.desc();
            default: return item.id.desc();
        }
    }




    //꿀팁 1: null이 절대 안 들어오는 필수 조건은 where에 바로 쓰기
    //예를 들어 cond.getMinRam()은 필수 선택 값이라 항상 숫자가 들어온다면, 굳이 메서드를 만들지 않고 where 절에 직접 작성해도 됨
    //연습삼아 작성해봄

    //꿀팁 2: 한두 번만 쓰는 동적 쿼리는 BooleanBuilder 활용하기
    //메서드를 쪼개는 것조차 귀찮고, 이 쿼리 안에서만 가볍게 동적 쿼리를 처리하고 싶다면 전통적인 BooleanBuilder를 쓰는 게 코드가 더 직관적이고 빠를 수 있음.
    //public List<Product> searchProducts(ProductSearchCondition cond) {
    //    BooleanBuilder builder = new BooleanBuilder();
    //
    //    // 조건이 있을 때만 빌더에 쏙쏙 추가
    //    if (StringUtils.hasText(cond.getName())) {
    //        builder.and(product.name.like(cond.getName()));
    //    }
    //    if (cond.getPriceGoe() != null) {
    //        builder.and(product.price.goe(cond.getPriceGoe()));
    //    }
    //
    //    return queryFactory
    //        .selectFrom(product)
    //        .where(builder) // 빌더 하나만 쏙 넣어주기
    //        .fetch();
    //}

    //꿀팁 3: 의미 있는 조건끼리 '합체'해서 메서드 개수 줄이기
    //이름 검색, 가격 검색을 각각 분리하지 않고, 성격이 비슷한 조건들을 하나의 메서드 안에서 묶어서 처리하면 관리할 메서드 수가 확 줄어듭니다.
    //
    //Java
    //// where 절에서는 메서드 딱 한 개만 호출!
    //.where(priceAndRamCondition(cond.getPriceGoe(), cond.getMinRam()))
    //
    //// 아래에서 두 조건을 조립해서 리턴
    //private BooleanExpression priceAndRamCondition(Integer priceGoe, Integer minRam) {
    //    BooleanExpression expression = null;
    //
    //    if (priceGoe != null) expression = product.price.goe(priceGoe);
    //    if (minRam != null) {
    //        expression = (expression != null) ? expression.and(product.ram.goe(minRam)) : product.ram.goe(minRam);
    //    }
    //    return expression;
    //}



}
