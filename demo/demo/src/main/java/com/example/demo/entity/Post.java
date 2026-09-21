package com.example.demo.entity;


import com.example.demo.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="post_id")
    private Long id;

    @Column(nullable = false)
    private String title;  //이걸로 인덱스 생성해놓기!!! 아직안함

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;


    private Long viewCount;//조회수

    @OneToMany(mappedBy = "postlike",cascade = CascadeType.REMOVE,orphanRemoval = true)
    private Long postLike;// 좋아요 수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "member_id",nullable = false)
    private Member member;


    //CascadeType.REMOVE: "게시글을 지우면 댓글도 다 지움(부모 엔티티 자체가 삭제될 때)
    //orphanRemoval = true: "게시글을 지울 때도 지워주고, 게시글의 댓글 목록(List)에서 댓글 하나만 쏙 뺐을 때도 DB에서 그 댓글을 지움"
    //(1. 부모 엔티티가 삭제될 때,2. 부모의 리스트에서 자식이 제거될 때)
    @OneToMany(mappedBy = "post",cascade = CascadeType.REMOVE,orphanRemoval = true)
    private List<Comment> commentList=new ArrayList<>();

    public Long getWriterId() {
        return member.getId();
    }

    @Builder
    public Post(String title,String content,Member member){
        this.title=title;
        this.content=content;
        this.member=member;
    }

    public void update(String title,String content){
        this.title=title;
        this.content=content;
    }
}
