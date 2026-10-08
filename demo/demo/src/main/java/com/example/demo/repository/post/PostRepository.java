package com.example.demo.repository.post;

import com.example.demo.entity.Post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long> {


    public Page<Post> findAllByMemberId(Long memberId, Pageable pageable);
}
