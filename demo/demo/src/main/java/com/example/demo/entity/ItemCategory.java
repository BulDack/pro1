package com.example.demo.entity;

import com.example.demo.entity.item.Item;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "item_category",
        /*
         * 동일한 상품이 동일한 카테고리에
         * 중복 등록되지 않도록 제약 조건을 설정한다.
         *
         * 예:
         * item_id=100, category_id=4
         * 조합은 한 번만 저장할 수 있다.
         *
         * 단, item_id=100, category_id=5는 허용된다.
         */
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_item_category",
                        columnNames = {"item_id", "category_id"}
                )
        }
)
//상품 하나가 여러 카테고리에 동시에 속할 수 있으므로 category와 item의 다대다 관계를 중간에 하나 더 만들어 풀어줌
public class ItemCategory {

    @Id@GeneratedValue
    @Column(name="Category_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="item_id",nullable = false)
    private Item item;

    public ItemCategory(Item item,Category category){
        this.item=item;
        this.category=category;
    }
}
