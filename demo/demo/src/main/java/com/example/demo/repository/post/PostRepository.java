package com.example.demo.repository.post;

import com.example.demo.entity.Post.Post;
import org.hibernate.sql.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post,Long> {


    public Page<Post> findAllByMemberId(Long memberId, Pageable pageable);

    @Modifying
    @Query("""
    UPDATE Post p
    SET p.viewCount = p.viewCount +1
    where p.id = :postId
    """)
    int increaseViewCount(@Param("postId")Long postId);
}
