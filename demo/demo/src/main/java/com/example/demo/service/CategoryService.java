package com.example.demo.service;

import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import jakarta.persistence.Cacheable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

//1. 사용자(Customer) 영역에서의 활용
// ① 메인 네비게이션 헤더 (카테고리 드롭다운 메뉴)상황: 사용자가 쇼핑몰에 접속했을 때 상단 메인 메뉴에 대/중/소분류 카테고리 목록이 펼쳐지는 화면입니다.Service 역할: 캐싱된 카테고리 트리 데이터(getCategoryTree())를 빠르게 반환합니다.호출: CategoryController -> CategoryService
// ② 브레드크럼(Breadcrumb) / 현재 위치 표시상황: 상품 상세 페이지 상단에 Home > 패션의류 > 상의 > 맨투맨 과 같이 현재 위치를 보여줄 때입니다.Service 역할: 특정 카테고리 ID를 받아 부모를 역추적(Self 참조)하여 최상위 카테고리까지의 경로 리스트를 조합해 줍니다.
// ③ 카테고리별 상품 목록 조회 (ItemService와의 연동)상황: 사용자가 상의(중분류) 카테고리를 클릭했습니다. 이때 상의 아래에 있는 맨투맨(소분류), 후드티(소분류)에 속한 모든 상품이 나와야 합니다.Service 역할: CategoryService가 상의 카테고리와 그 하위 카테고리 ID들을 전부 추출([10, 11, 12])해서 ItemService에 넘겨주면, ItemService는 DB에서 IN (10, 11, 12) 조건으로 상품들을 조회합니다.

//2. 관리자(Admin) 영역에서의 활용
//① 상품 등록/수정 시 카테고리 검증
//상황: 관리자가 새로운 상품을 등록할 때 카테고리를 선택합니다.
//Service 역할: ItemService가 상품을 저장하기 전 CategoryService.validateCategory(categoryId)를 호출하여 존재하는 카테고리인지, 삭제되거나 비활성화된 카테고리는 아닌지 검증합니다.
//② 카테고리 구조 변경 (생성/수정/이동)
//상황: 관리자가 카테고리 이름을 바꾸거나, 특정 카테고리를 다른 부모 카테고리 밑으로 이동시킵니다.
//Service 역할:
//순환 참조 방지: "A가 B의 부모인데, B를 A의 부모로 지정하는 오류"를 예방하는 로직 수행.
//캐시 갱신: 카테고리 구조가 변경되었으므로 기존에 저장된 카테고리 트리 캐시(Redis/Caffeine)를 초기화(@CacheEvict)합니다.
//③ 카테고리 안전 삭제
//상황: 관리자가 특정 카테고리를 삭제하려고 합니다.
//Service 역할:
//삭제하려는 카테고리에 하위 카테고리가 남아있는지 확인
//삭제하려는 카테고리에 연결된 상품(ItemCategory)이 남아있는지 확인
//조건 불충분 시 CategoryDeleteException 예외를 발생시켜 데이터 유실을 방지합니다.


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    //상위-하위 카테고리를 연관관계로 묶어둔 경우, 하위 카테고리(children)를 조회할 때 JPA의 지연 로딩이 동작합니다.
    //지연 로딩이 작동하려면 DB 세션(EntityManager)과 트랜잭션이 유지되는 상태여야 합니다.

    //[사용자 요청: "상의 카테고리 상품 보여줘"]
    //   ↓
    //[ItemController]
    //   ↓ (1. 하위 카테고리 ID 목록 조회 요청)
    //[CategoryService] ──> "상의" 및 자식들 ID 추출 (예: [10, 11, 12])
    //   ↓ (2. ID 목록 전달)
    //[ItemService] ──────> IN (10, 11, 12) 조건으로 QueryDSL/JPA 검색
    //   ↓
    //[사용자에게 상품 목록 응답]

    private final CategoryRepository categoryRepository;


    /**
     * 특정 카테고리 ID를 받아 자기 자신 + 모든 하위 카테고리 ID 목록을 반환
     */
    public List<Long>getSelfAndSubCategoryIds(Long categoryId){
        if(categoryId==null){
            return Collections.emptyList();
        }

        //1.대상 카테고리 조회(fetch조인으로 자식 카테고리 까지 한번에 가져왓음)
        Category category=categoryRepository.findByIdWithChildren(categoryId).orElseThrow(
                ()->new EntityNotFoundException("존재하지 않는 카데고리 입니다.")
        );

        Set<Long>allCategoryIds=new HashSet<>();

        //2.재귀함수를 통해 모든 자식 id수집
        traverseCategoryIds(category,allCategoryIds);

        return new ArrayList<>(allCategoryIds);

    }

//이거 아직안함
//    // 카테고리 트리는 캐싱을 적용하기 딱 좋은 대상!
//    @Cacheable(value = "categoryTree", key = "'all'")
//    public List<CategoryResponseDto> getCategoryTree() {
//        // ... DB 조회 및 트리 변환 로직
//    }


    // 재귀적으로 자식 카테고리 ID들을 수집하는 헬퍼 메서드
    private void traverseCategoryIds(Category category, Set<Long> idSet) {

        idSet.add(category.getId()); //현재 카테고리 id추가

        for (Category child: category.getChildren()){
            traverseCategoryIds(child,idSet); //자식노드로 이동
        }

    }


}
