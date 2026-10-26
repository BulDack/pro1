package com.example.demo.repository.item;

import com.example.demo.entity.Post.PostLike;
import com.example.demo.entity.item.ItemLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemLikeRepository extends JpaRepository<ItemLike,Long> {

    public boolean existsByMemberIdAndItemId(Long memberId,Long itemId);


    @Modifying
    @Query("""
        DELETE FROM ItemLike il
        WHERE il.item.id = :itemId
          AND il.member.id = :memberId
    """)
    void deleteLike(
            @Param("itemId") Long itemId,
            @Param("memberId") Long memberId
    );

    @Modifying
    @Query("DELETE FROM ItemLike il WHERE il.item.id = :itemId")
    void deleteAllByItemId(@Param("itemId") Long itemId);

    Optional<ItemLike> findByMember_IdAndItem_Id(Long memberId, Long itemId);
}
