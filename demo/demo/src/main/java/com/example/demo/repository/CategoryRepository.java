package com.example.demo.repository;

import com.example.demo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    //1.최상위 카테고리만 조회(parent_id가 null인 항목)
    List<Category> findByParentIsNull();

    //2.특정 부모 카테고리의 직속 하위 카테고리 목록 조회
    List<Category> findByParentId(Long parentId);

    //3.카테고리 이름 중복 확인(생성/수정시 검증용)
    boolean existsByName(String name);

    //n+1문제 해결 (findByParentIsNull()로 최상위 카테고리를 가져온 뒤 DTO로 변환하면, 자식 카테고리를 로딩할 때마다 추가 쿼리가 나가는 N+1 문제가 발생,
    //이를 해결하기 위해 한번의 쿼리로전체 카테고리와 자식 연관관계를 한꺼번에 끌어옴)
    @Query("select distinct c from Category c " + "left join fetch c.child " + "where c.parent is null " + "order by c.id asc")
    List<Category> findAllWithChildren();

    //여기도 n+1문제 해결위해 jpql써서 한번에 가져옴
    @Query("select distinct c from Category c " +
            "left join fetch c.children " +
            "where c.id = :categoryId")
    Optional<Category> findByIdWithChildren(@Param("categoryId") Long categoryId);

    //이런식으로 querydsl을 사용해서 할수 있음
    //// 1. Custom 인터페이스
    //public interface CategoryRepositoryCustom {
    //    List<Category> findAllCategoryTree();
    //}
    //
    //// 2. Custom 구현체
    //@RequiredArgsConstructor
    //public class CategoryRepositoryImpl implements CategoryRepositoryCustom {
    //
    //    private final JPAQueryFactory queryFactory;
    //
    //    @Override
    //    public List<Category> findAllCategoryTree() {
    //        return queryFactory
    //                .selectFrom(category)
    //                .distinct()
    //                .leftJoin(category.children, QCategory.category).fetchJoin() // 자식 노드 Fetch Join
    //                .where(category.parent.isNull())
    //                .orderBy(category.id.asc())
    //                .fetch();
    //    }
    //}
    //
    //// 3. 메인 Repository에 상속
    //public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryRepositoryCustom {
    //}
}
