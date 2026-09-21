package com.example.demo.repository.post;

import com.example.demo.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike,Long> {
    public boolean existsByMemberIdAndPostId(Long memberId,Long postId);

    Optional<PostLike> findByMemberIdAndPostId(Long memberId, Long postId);
}
