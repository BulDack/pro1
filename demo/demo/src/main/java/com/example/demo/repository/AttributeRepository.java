package com.example.demo.repository;

import com.example.demo.entity.item.ItemSearchAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttributeRepository extends JpaRepository<ItemSearchAttribute,Long> {

    @Modifying(clearAutomatically = true)
    //일반적인 JPA 로직은 영속성 컨텍스트를 거쳐서 DB로 가는데,
    // @Modifying이 붙은 벌크 쿼리(한 번에 대량으로 지우거나 수정하는 쿼리)는 영속성 컨텍스트를 완전히 무시하고 DB로 곧장 감.
    //
    //여기서 심각한 데이터 불일치 문제가 생길 수 있음.
    //clearAutomatically = true: 벌크 쿼리를 실행한 직후에 영속성 컨텍스트(1차 캐시)를 강제로 깨끗하게 비워버리는(clear) 옵션.
    // 이렇게 비워두면, 이후에 데이터를 다시 조회할 때 1차 캐시가 비어있으므로 DB에서 최신 데이터를 새로 깔끔하게 읽어오게 됨.
    // 데이터 불일치가 해결되죠!
    @Query("delete from ItemSearchAttribute a where a.item.id= :itemId")
    public void deleteByItemId(@Param("itemId") Long itemId);
}
