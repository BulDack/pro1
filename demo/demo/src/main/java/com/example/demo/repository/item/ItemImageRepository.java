package com.example.demo.repository.item;

import com.example.demo.entity.item.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemImageRepository extends JpaRepository<ItemImage,Long> {

    /*
     * 해당 상품에 속한 이미지를 조회한다.
     *
     * findByItemId:
     * ItemImage.item.id를 기준으로 검색한다.
     *
     * OrderBySortOrderAsc:
     * 이미지 순서가 낮은 것부터 정렬한다.
     *
     * 따라서 Item 엔티티에
     * List<ItemImage>가 없어도 조회할 수 있다.
     */

    List<ItemImage>findByItemIdOrderBySortOrderAsc(Long itemId);

}
