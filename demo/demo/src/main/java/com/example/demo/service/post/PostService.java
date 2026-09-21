package com.example.demo.service.post;

import com.example.demo.ennum.PostSort;
import com.example.demo.entity.Post;
import com.example.demo.entity.Member;
import com.example.demo.repository.post.PostQueryRepository;
import com.example.demo.repository.post.PostRepository;
import com.example.demo.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.demo.dto.PostDto.createRequestDto;
import static com.example.demo.dto.PostDto.updateRequestDto;
import static com.example.demo.dto.PostDto.searchRequestDto;
import static com.example.demo.dto.PostDto.postListResponse;
import static com.example.demo.dto.PostDto.postDetailResponse;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final PostQueryRepository postQueryRepository;


    //게시글 작성
    @Transactional
    public Long createPost(createRequestDto request, Long memberId){
        Member member=memberJpaRepository.getReferenceById(memberId);
        Post post= Post.builder().
                content(request.content())
                .title(request.title())
                .member(member)
                .build();

        return postRepository.save(post).getId();
    }

    //게시글 업데이트
    @Transactional
    public void updatePost(Long postId,updateRequestDto request,Long memberId){
        Post post=postRepository.findById(postId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if(!post.getMember().getId().equals(memberId)){
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        post.update(request.title(), request.content());

    }


    //게시글 삭제
    @Transactional
    public void deletePost(Long postId,Long memberId){
        Post post=postRepository.findById(postId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시글입니다."));
        if(!post.getMember().getId().equals(memberId)){
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        postRepository.delete(post);
    }


    //해당 글만 찾기(게시글을 클릭하면 상세조회)
    //게시글 상세 조회 / 수정 / 삭제처럼 `Post 엔티티 자체가 필요한 경우에 쓰기 !!!
    public postDetailResponse findPost(Long postId) {
        Post post= this.postRepository.findById(postId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시글입니다."));

        return postQueryRepository.findDetailPost(postId);
    }

    //내 모든 게시글 보이기
    public Page<postListResponse>getMyPost(Long memberId,Pageable pageable){

        Page<Post> posts=postRepository.findAllByMemberId(memberId,pageable);

        // Page 내부의 map 메서드로 Record DTO 변환
        return posts.map(postListResponse::from);

    }

    //게시글 제목 검색해서 불러오기
    public Page<postListResponse>searchPosts(searchRequestDto request, Pageable pageable,PostSort sort){
        return postQueryRepository.searchPosts(request,pageable,sort);
    }


    //모든 게시글 불러오기
    public Page<postListResponse>findAll(Pageable pageable, PostSort sort){

        return postQueryRepository.findAllPost(pageable,sort);
    }
}
