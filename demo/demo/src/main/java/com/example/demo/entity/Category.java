package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

//하나의 Category 엔티티 내에서 자기 자신을 참조하는 셀프 참조(Self-Referencing) 연관관계를 사용
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id@GeneratedValue
    @Column(name="category_id")
    private Long id;

    private String name;

    // 1. 부모 카테고리 (상위) -> N:1 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private Category parent;

    // 2. 자식 카테고리들 (하위) -> 1:N 관계
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children= new ArrayList<>();


    // 연관관계 편의 메서드
    public void addChildCategory(Category child){
        this.children.add(child);
        child.setParent(this);
    }
    private void setParent(Category parent){
        this.parent=parent;
    }
}
