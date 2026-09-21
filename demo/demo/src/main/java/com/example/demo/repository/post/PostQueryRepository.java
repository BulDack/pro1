package com.example.demo.repository.post;


import com.example.demo.dto.PostDto;
import com.example.demo.ennum.PostSort;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.example.demo.entity.QPost.post;
import static com.example.demo.entity.QPostLike.postLike;
import static com.example.demo.entity.QMember.member;
import static com.example.demo.dto.PostDto.searchRequestDto;
import static com.example.demo.dto.PostDto.postDetailResponse;
import static com.example.demo.dto.PostDto.postListResponse;

//1. 일반 join을 쓸 때 (selectFrom(post).join(post.member, member))
//DB SQL: INNER JOIN member 구문이 나갑니다.
//JPA 내부 동작: DB에서 JOIN 연산은 수행하지만, SELECT 절에는 Post 엔티티의 컬럼만 가지고 옵니다. Member 데이터는 가져오지 않고 가짜 객체(프록시)로 채워둡니다.
//결과: 나중에 post.getMember().getNickname()을 호출하는 순간 지연 로딩이 터지면서 Member를 조회하는 추가 쿼리가 실행(N+1 문제 발생)됩니다.
//
//2. fetchJoin을 쓸 때 (selectFrom(post).join(post.member, member).fetchJoin())
//DB SQL: 똑같이 INNER JOIN member 구문이 나갑니다.
//JPA 내부 동작: SELECT 절에 Post 데이터뿐만 아니라 Member 데이터까지 모조리 다 포함시켜 가져옵니다.
//결과: 이미 진짜 Member 객체에 데이터가 꽉 채워져 있으므로, 나중에 post.getMember()를 호출해도 추가 쿼리가 나가지 않습니다 (쿼리 1번으로 완결).

//DTO 조회(Projection) 때는 fetchJoin이 아니라 join을 해도 됨
//DTO 조회(select(Projections.fields(...)))는 JPA 영속성 컨텍스트나 지연 로딩(Lazy Loading)이라는 개념 자체가 개입하지 않는 순수 SQL 조회 방식입니다.
//따라서 DTO 조회 시에는 fetchJoin()이라는 키워드를 아예 쓸 수도 없으며, 그냥 일반 .join()만 걸어주고 SELECT 절에 필요한 필드(member.loginId)를 적어주기만 하면 깔끔하게 1번의 쿼리로 처리됩니다.
//
//한 줄 요약
//DTO로 바로 가져올 때: 일반 join만 써도 쿼리 1번으로 완결됨 (fetchJoin 적용 불가능).
//Entity로 가져와서 자바로 조작할 때: 일반 join을 쓰면 지연 로딩(N+1)이 터지므로 반드시 fetchJoin을 써야 쿼리 1번으로 완결됨.
@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    //선택한 게시글 1개 조회하기
    public postDetailResponse findDetailPost(Long postId){
        return queryFactory
                .select(
                        Projections.fields(
                                postDetailResponse.class,
                                post.id,
                                post.title,
                                post.content,
                                post.member.loginId,
                                postLike.count().as("likeCount"),// DTO 필드명과 매핑
                                post.createdDate
                        ))
                .from(post)
                .join(post.member,member)
                .leftJoin(postLike)
                .on(postLike.post.eq(post)) //조인 조건
                .where(post.id.eq(postId))
                .groupBy(post.id)
                .fetchOne();
    }

    //모든 게시물 불러오기
    public Page<postListResponse>findAllPost(Pageable pageable, PostSort sort){

        List<postListResponse>content=queryFactory
                .select(
                        Projections.fields(
                                postListResponse.class,
                                post.id,
                                post.title,
                                postLike.count().as("likeCount"),
                                post.member.loginId,
                                post.createdDate
                ))
                .from(post)
                .join(post.member,member)
                .leftJoin(postLike)
                .on(postLike.post.eq(post)) //조인 조건
                .orderBy(getOrderSpecifiers(sort))
                .groupBy(post.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery=queryFactory
                .select(post.count())
                .from(post);

        return PageableExecutionUtils.getPage(content,pageable,countQuery::fetchOne);
    }


    //제목으로 포스팅 검색하기
    public Page<postListResponse>searchPosts(searchRequestDto requestDto, Pageable pageable,PostSort sort){

        List<postListResponse> content=queryFactory
                .select(Projections.fields(
                        postListResponse.class,
                        post.id,
                        post.title,
                        postLike.count().as("likeCount"),
                        post.member.loginId,
                        post.createdDate
                ))
                .from(post)
                .join(post.member,member)
                .where(
                        titleContains(requestDto.title()),
                        createdAtAfter(requestDto.startDate())
                )
                .orderBy(getOrderSpecifiers(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery=queryFactory
                .select(post.count())
                .from(post)
                .where(titleContains(requestDto.title()),
                        createdAtAfter(requestDto.startDate()));

        return PageableExecutionUtils.getPage(content,pageable,countQuery::fetchOne);

    }

    //제목 2글자 이상으로 검색
    private BooleanExpression titleContains(String keyword){

        if(!StringUtils.hasText(keyword) || keyword.trim().length()<2){
            return null;
        }
        // LIKE '%keyword%' 조건 생성
        return post.title.containsIgnoreCase(keyword);
    }

    //포스팅 생성일 조건으로 검색
    private BooleanExpression createdAtAfter(LocalDate from) {

        if (from == null) {
            return null;
        }
        // 선택한 날짜의 00:00:00부터 검색
        return post.createdDate.goe(from.atStartOfDay());

    }

    // 2. 종료일 조건 (~ to)====이것도 아직안봄
    private BooleanExpression createdAtBefore(LocalDate to) {
        if (to == null) {
            return null;
        }
        // 선택한 날짜의 23:59:59까지 검색
        return post.createdDate.loe(to.atTime(LocalTime.MAX));
    }

    // 3. (선택) 기간 통합 조건============이거아직 안봣음
    private BooleanExpression createdAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return null;
        }
        if (from != null && to != null) {
            return post.createdDate.between(from.atStartOfDay(), to.atTime(LocalTime.MAX));
        }
        return from != null ? createdAtAfter(from) : createdAtBefore(to);
    }

    //orderby 조건(pagable의 sort를 변환해줌)

    private OrderSpecifier<?>[] getOrderSpecifiers(PostSort sort) {

        return switch (sort) {

            case LATEST -> new OrderSpecifier<?>[]{
                    post.createdDate.desc()
            };

            case OLDEST -> new OrderSpecifier<?>[]{
                    post.createdDate.asc()
            };

            case TITLE -> new OrderSpecifier<?>[]{
                    post.title.asc(),
                    post.createdDate.desc()
            };

            case POPULAR -> new OrderSpecifier<?>[]{
                    post.viewCount.desc(),
                    post.createdDate.desc()
            };
        };
    }
}
