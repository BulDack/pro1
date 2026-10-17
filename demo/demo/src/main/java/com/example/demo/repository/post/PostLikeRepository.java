package com.example.demo.repository.post;

import com.example.demo.entity.Post.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike,Long> {

    /*
     * 해당 회원이 게시글에 좋아요를 눌렀는지 확인
     *
     * true  : 이미 좋아요를 누른 상태
     * false : 좋아요를 누르지 않은 상태
     */
    public boolean existsByMemberIdAndPostId(Long memberId,Long postId);

    /*
     * 좋아요 취소
     *
     * 게시글 ID와 회원 ID가 일치하는
     * 좋아요 데이터를 삭제한다.
     *
     * 반환값:
     * 1 -> 삭제 성공
     * 0 -> 삭제할 좋아요가 없음
     */
    @Modifying
    @Query("""
        DELETE FROM PostLike pl
        WHERE pl.post.id = :postId
          AND pl.member.id = :memberId
    """)
    void deleteLike(
            @Param("postId") Long postId,
            @Param("memberId") Long memberId
    );

    Optional<PostLike> findByMember_IdAndPost_Id(Long memberId, Long postId);

    long countByPostId(Long postId);
}
