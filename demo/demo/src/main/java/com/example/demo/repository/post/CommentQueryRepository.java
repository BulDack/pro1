package com.example.demo.repository.post;


import com.example.demo.ennum.CommentSort;
import com.example.demo.ennum.PostSort;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import com.example.demo.dto.CommentDto.commentResponseDto;

import static com.example.demo.entity.Post.QComment.comment;
import static com.example.demo.entity.QMember.member;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class CommentQueryRepository {

    private final JPAQueryFactory queryFactory;


    public Page<commentResponseDto> getComments(Long postId,Long memberId, Pageable pageable, CommentSort sort){

        List<commentResponseDto> content=queryFactory
                .select(Projections.constructor(
                        commentResponseDto.class,
                        comment.id,
                        comment.content,
                        comment.member.id,
                        comment.member.nickname,
                        comment.createdDate,
                        memberId == null
                        ? Expressions.constant(false)
                                :new CaseBuilder()
                                .when(comment.member.id.eq(memberId))
                                .then(true)
                                .otherwise(false)
                ))
                .from(comment)
                .join(comment.member,member)
                .where( comment.post.id.eq(postId)
                )
                .orderBy(getOrderSpecifiers(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery=queryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.post.id.eq(postId));


        return PageableExecutionUtils.getPage(content,pageable,countQuery::fetchOne);


    }

    private OrderSpecifier<?>[] getOrderSpecifiers(CommentSort sort) {

        return switch (sort) {

            case LATEST -> new OrderSpecifier<?>[]{
                    comment.createdDate.desc()
            };

            case OLDEST -> new OrderSpecifier<?>[]{
                    comment.createdDate.asc()
            };

        };
    }
}
