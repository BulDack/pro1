package com.example.demo.entity;

import com.example.demo.entity.item.Item;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
//상품 하나가 여러 카테고리에 동시에 속할 수 있으므로 category와 item의 다대다 관계를 중간에 하나 더 만들어 풀어줌
public class ItemCategory {

    @Id@GeneratedValue
    @Column(name="Category_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="item_id")
    private Item item;

    public void addItem(Item item) {
        this.item=item;
    }
    public void addCategory(Category category){
        this.category=category;
    }
}
