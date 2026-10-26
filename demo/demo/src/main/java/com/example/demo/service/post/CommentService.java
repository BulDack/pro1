package com.example.demo.service.post;


import com.example.demo.dto.ApiResponse;
import com.example.demo.ennum.CommentSort;
import com.example.demo.entity.Post.Comment;
import com.example.demo.entity.Member;
import com.example.demo.entity.Post.Post;
import com.example.demo.repository.post.CommentQueryRepository;
import com.example.demo.repository.post.CommentRepository;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.demo.dto.CommentDto.commentRequestDto;
import static com.example.demo.dto.CommentDto.commentResponseDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final CommentQueryRepository commentQueryRepository;


    //댓글 조회
    public Page<commentResponseDto>getComments( Long postId,Long currentMemberId,Pageable pageable, CommentSort sort){

        return commentQueryRepository.getComments(postId,currentMemberId,pageable,sort);
    };


    //댓글생성
    @Transactional
    public Long createComment(Long postId,commentRequestDto request,Long memberId){

        Member member= memberRepository.getReferenceById(memberId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        Comment comment= Comment.builder()
                .post(post)
                .content(request.content())
                .member(member)
                .build();

        return commentRepository.save(comment).getId();
    }

    //댓글 삭제

    @Transactional
    public void deleteComment(Long postId,Long commentId,Long memberId){

        Comment comment=commentRepository.findById(commentId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        if(!comment.getMember().getId().equals(memberId)){
            throw new IllegalArgumentException("삭제권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }
}
