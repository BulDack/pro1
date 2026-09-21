package com.example.demo.service;

import com.example.demo.dto.ItemDto;
import com.example.demo.dto.condition.ItemSearchCondition;
import com.example.demo.entity.ItemCategory;
import com.example.demo.entity.item.Item;
import com.example.demo.entity.item.ItemSearchAttribute;
import com.example.demo.entity.ennum.ItemStatus;
import com.example.demo.repository.AttributeRepository;
import com.example.demo.repository.ItemCategoryRepository;
import com.example.demo.repository.ItemQueryRepository;
import com.example.demo.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.example.demo.dto.ItemDto.Response;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemQueryRepository itemQueryRepository;
    private final AttributeRepository attributeRepository;// 속성 저장용
    private final ItemCategoryRepository categoryRepository;
    private final CategoryService categoryService;

    //상품등록
    public Long registerItem(ItemDto.CreateRequest request){
        ItemCategory category=categoryRepository.findById(request.getCategoryId()).orElseThrow(()->new EntityNotFoundException("카테고리가 존재하지 않습니다"));
        Item item= Item.createItem(request.getName(), request.getPrice(), request.getStockQuantity(),category,request.getDescription());
        // DB에 저장되면서 영속성 컨텍스트에 의해 item.getId()가 채워집니다.
        itemRepository.save(item);

        // 속성 엔티티 변환 후 벌크 저장 (DTO 내부 메서드 활용으로 깔끔!)
        if(request.getAttributes()!=null){
            List<ItemSearchAttribute> attrs=request.toAttributeEntries(item);
            attributeRepository.saveAll(attrs);
        }
        return item.getId();
    }
    //상품 정보 수정
    @Transactional
    public Response updateItem(Long itemId,ItemDto.UpdateRequest request){
        Item item=itemRepository.findById(itemId).orElseThrow(()->new EntityNotFoundException("상품을 찾을수 없습니다"));
        //엔티티 내부 메서드를 통한 더티체킹 수정
        item.changeItem(request.getName(),request.getPrice());
        //속성은 기존것 지우고 새로 등록하는 전략 취하기
        //1.단순함: 기존에 있던 데이터와 새로 들어온 데이터를 일일이 비교(ID가 같은지, 값이 바뀌었는지 등)해서 UPDATE할지 INSERT할지 분기 처리하는 로직을 짤 필요가 없음.
        //2.정확성: 데이터가 무조건 깔끔하게 최신 상태로 동기화됩니다.
        item.updateAttributes(request.toAttributeEntries(item));

        return new Response(item.getId(),item.getName(),item.getPrice());

    }

    //상품 단건 조회할때 쓰임
    public Response findById(Long itemId){
        Item item=itemRepository.findById(itemId).orElseThrow(()->new EntityNotFoundException("상품을 찾을수 없습니다"));
        return new Response(item.getId(),item.getName(),item.getPrice());
    }

    //상품 전체 조회 or검색 조회
    public Page<Response> findAllItems(ItemSearchCondition condition, Pageable pageable) {
        List<Long> categoryIds  = Collections.emptyList();

        // 검색 조건에 categoryId가 들어온 경우,하위 카테고리 id들 까지 전부 추출
        if(condition !=null && condition.getCategoryId() != null){
            categoryIds= categoryService.getSelfAndSubCategoryIds(condition.getCategoryId());
        }

         Page<Response> items =itemQueryRepository.searchItems(condition,categoryIds,pageable);
         return items;
    }

    //재고 차감(주문시 호출)
    public void detuckStock(Long itemId,int quantity){
        Item item= itemRepository.findById(itemId).orElseThrow(()->new EntityNotFoundException("상품을 찾을수 없습니다"));
        item.removeStock(quantity);
    }
    //재고 증가
    public void addStock(Long itemId,int quantity){
        Item item=itemRepository.findById(itemId).orElseThrow(()->new EntityNotFoundException("상품을 찾을수 없습니다"));
        item.addStock(quantity);
    }





    public void deleteItem(Long itemId) {
        Item item=itemRepository.findById(itemId).orElseThrow(()->new EntityNotFoundException("상품을 찾을수가 없습니다."));
        item.changeStatus(ItemStatus.DELETE);

    }
}
