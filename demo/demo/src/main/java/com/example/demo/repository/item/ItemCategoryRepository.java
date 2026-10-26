package com.example.demo.repository.item;

import com.example.demo.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemCategoryRepository extends JpaRepository<ItemCategory,Long> {


    //특정상품에 연결된 카테고리 조회
    List<ItemCategory>findByItemId(Long itemId);

    //특정 카테고리에 연결된 상품조회
    List<ItemCategory> findByCategoryId(Long categoryId);


    /*
     * 상품과 카테고리의 연결 여부 확인
     *
     * 이미 연결된 카테고리를
     * 다시 등록하지 않도록 검사할 때 사용한다.
     */
    boolean existsByItemIdAndCategoryId(
            Long itemId,
            Long categoryId
    );

    /*
     * 상품이 삭제될 때
     * 연결된 카테고리 관계를 제거한다.
     *
     * 이 메서드는 ItemCategory만 삭제한다.
     *
     * Item이나 Category 자체는 삭제하지 않는다.
     */
    void deleteByItemId(Long itemId);

    /*
     * 카테고리에 연결된 상품이 있는지 확인한다.
     *
     * 카테고리 삭제 전에 검사할 때 사용한다.
     */
    boolean existsByCategoryId(Long categoryId);
}
