package com.example.demo.service.item;

import com.example.demo.dto.my.MyResponse;
import com.example.demo.ennum.ItemSort;
import com.example.demo.entity.Category;
import com.example.demo.entity.ItemCategory;
import com.example.demo.entity.Member;
import com.example.demo.entity.item.Item;
import com.example.demo.entity.item.ItemImage;
import com.example.demo.entity.ennum.ItemStatus;
import com.example.demo.repository.AttributeRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.item.*;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ImageStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.access.AccessDeniedException;
import java.util.*;

import static com.example.demo.dto.ItemDto.ItemListResponse;
import static com.example.demo.dto.ItemDto.ItemSearchRequest;
import static com.example.demo.dto.ItemDto.ItemDetailResponse;
import static com.example.demo.dto.ItemDto.ItemCreateRequest;
import static com.example.demo.dto.ItemDto.ItemUpdateRequest;
import static com.example.demo.dto.my.MyResponse.ItemResponse;





@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final ItemQueryRepository itemQueryRepository;
    private final ItemImageRepository itemImageRepository;
    private final ItemLikeRepository itemLikeRepository;
    private final AttributeRepository attributeRepository;// 속성 저장용
    private final CategoryRepository categoryRepository;
    private final ItemCategoryRepository itemCategoryRepository;

    private final CategoryService categoryService;
    private final ImageStorageService imageStorageService;


    //상품등록
    @Transactional
    public Long registerItem(ItemCreateRequest request, List<MultipartFile> images,Long memberId){
        Member seller=memberRepository.getReferenceById(memberId);
        Item item= Item.builder()
                .name(request.name())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .description(request.description())
                .thumbnailUrl(request.thumbnailUrl())
                .seller(seller)
                .build();

        // DB에 저장되면서 영속성 컨텍스트에 의해 item.getId()가 채워짐.
        //ItemCategory를 생성하려면
        //연결할 Item 엔티티가 필요해서 먼저 일단 저장
        itemRepository.save(item);



        ////이부분 지금 이제 해야함
//        List<ItemImage>itemImages=new ArrayList<>();
//        for(int i=0; i< images.size(); i++){
//
//            //서버 디스크에 실제 파일 저장
//            String imageUrl= imageStorageService.store(images.get(i));
//
//            itemImages.add(
//                    new ItemImage(imageUrl,i,item)
//            );
//        }
//        itemImageRepository.saveAll(itemImages);

        Category category= categoryRepository.findById(request.categoryId())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 카테고리가 포합되어 있습니다."));

        /*
         * 5. ItemCategory 생성
         *
         * 조회한 Category마다 ItemCategory를 생성한다.
         *
         * 여기서는 Item과 Category의 단방향 관계만 사용한다.
         */
//        List<ItemCategory>itemCategories=categories.stream()
//                .map(category -> new ItemCategory(item,category))
//                .toList();

        ItemCategory itemCategory= new ItemCategory(item,category);

        //상품-카테고리 연결정보 저장
        itemCategoryRepository.save(itemCategory);

        return item.getId();
    }
    //상품 정보 수정
    @Transactional
    public void updateItem(Long itemId, Long memberId, ItemUpdateRequest request) {
        Item item=itemRepository.findById(itemId).orElseThrow(()->new EntityNotFoundException("상품을 찾을수 없습니다"));

        if(!item.getSeller().getId().equals(memberId)){
            throw new AccessDeniedException("아이템 등록 회원과 일치하지 않습니다.");
        }
        //엔티티 내부 메서드를 통한 더티체킹 수정
        item.changeItem(request.name(),request.price(),request.description(),request.stockQuantity());
        //속성은 기존것 지우고 새로 등록하는 전략 취하기
        //1.단순함: 기존에 있던 데이터와 새로 들어온 데이터를 일일이 비교(ID가 같은지, 값이 바뀌었는지 등)해서 UPDATE할지 INSERT할지 분기 처리하는 로직을 짤 필요가 없음.
        //2.정확성: 데이터가 무조건 깔끔하게 최신 상태로 동기화됩니다.
        //item.updateAttributes(request.toAttributeEntries(item));
        List<String> imageUrl= new ArrayList<>();///이부분 아직 안햇음

    }

    //상품 단건 조회할때 쓰임
    public ItemDetailResponse getItemDetail(Long itemId,Long memberId){
        Item item= itemRepository.findById(itemId)
                .orElseThrow(()-> new IllegalArgumentException("상품이 존재하지 않습니다."));
        // 비로그인 사용자는 찜하지 않은 상태로 표시한다.
        // 로그인한 경우에만 해당 회원의 찜 기록을 조회한다.
        boolean liked= memberId != null && itemLikeRepository.existsByMemberIdAndItemId(memberId,itemId);

        List<String> imageUrl= itemImageRepository.findByItemIdOrderBySortOrderAsc(itemId)
                .stream()
                .map(ItemImage::getImageUrl)
                .toList();

        return new ItemDetailResponse(item.getId(),item.getName(),item.getPrice(),imageUrl,item.getDescription(),item.getLikeCount(),liked);
    }

    //상품 전체 조회 or검색 조회
    public Page<ItemListResponse> findAllItems(ItemSearchRequest condition, Pageable pageable, ItemSort sort) {

        Long categoryId  = condition.categoryId();

//         //검색 조건에 categoryId가 들어온 경우,하위 카테고리 id들 까지 전부 추출
//        if(condition !=null && condition.categoryId() != null){
//            categoryId= categoryService.getSelfAndSubCategoryIds(condition.getCategoryId());
//        }

         Page<ItemListResponse> items =itemQueryRepository.searchItems(condition,categoryId,pageable,sort);
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

    //상품삭제
    public void deleteItem(Long itemId,Long memberId) {
        Item item=itemRepository.findById(itemId).orElseThrow(()->new EntityNotFoundException("상품을 찾을수가 없습니다."));
        if(!item.getSeller().getId().equals(memberId)){
            throw new AccessDeniedException("아이템 등록 회원과 일치하지 않습니다.");
        }
        item.changeStatus(ItemStatus.DELETE);
    }


    //내가 등록한 상품목록 불러오기
    public Page<ItemResponse> getMyItems(Long memberId, Pageable pageable) {

        Page<Item>items=itemRepository.findByMemberId(memberId,pageable);
        return items.map(ItemResponse::from);

    }
}
