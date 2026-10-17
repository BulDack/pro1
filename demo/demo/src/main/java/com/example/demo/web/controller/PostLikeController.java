package com.example.demo.web.controller;


import com.example.demo.config.oauth.dto.PrincipalDetails;
import com.example.demo.service.post.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("{postId}/likes")
    public ResponseEntity<Void>addLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal PrincipalDetails userDetails
            ){

        postLikeService.addLike(postId, userDetails.getId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{postId}/unlikes")
    public ResponseEntity<Void>removeLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ){
        postLikeService.unlike(postId, userDetails.getId());

        return ResponseEntity.noContent().build();
    }
}
