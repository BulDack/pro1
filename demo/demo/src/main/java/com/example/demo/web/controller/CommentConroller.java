package com.example.demo.web.controller;


import com.example.demo.dto.security.PrincipalDetails;
import com.example.demo.service.post.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.dto.PostDto.commentDto;

@RestController("/api")
@RequiredArgsConstructor
public class CommentConroller {

    private final CommentService commentService;


    //댓글 생성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Long>createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody commentDto request
            ){
        Long commentId=commentService.createComment(postId,request,principalDetails.getId());
        return ResponseEntity.ok(commentId);
    }

    //댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void>deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ){
        commentService.deleteComment(commentId, principalDetails.getId());
        return ResponseEntity.ok().build();
    }
}
