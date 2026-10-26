package com.example.demo.web.controller;


import com.example.demo.config.oauth.dto.PrincipalDetails;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PostDto;
import com.example.demo.ennum.CommentSort;
import com.example.demo.ennum.PostSort;
import com.example.demo.service.post.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.dto.CommentDto.commentRequestDto;
import static com.example.demo.dto.CommentDto.commentResponseDto;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //댓글 조회
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<Page<commentResponseDto>> getComment(
        @PathVariable Long postId,
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @PageableDefault(size = 20) Pageable pageable,
        @RequestParam(defaultValue = "LATEST") CommentSort sort
    ){

        Long memberId = principalDetails != null
                ? principalDetails.getId()
                : null;

        Page<commentResponseDto> result=
               commentService.getComments(postId,memberId,pageable,sort);

        return ApiResponse.success("댓글을 불러왔습니다",result);
    }

    //댓글 생성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Long>createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody commentRequestDto request
            ){
        Long commentId=commentService.createComment(postId,request,principalDetails.getId());
        return ResponseEntity.ok(commentId);
    }

    //댓글 삭제
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void>deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ){
        commentService.deleteComment(postId,commentId, principalDetails.getId());
        return ResponseEntity.ok().build();
    }
}
