package com.example.demo.service.post;


import com.example.demo.entity.Member;
import com.example.demo.entity.Post.Post;
import com.example.demo.entity.Post.PostLike;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.post.PostLikeRepository;
import com.example.demo.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final MemberRepository memberRepository;

    public void addLike(Long postId,Long memberId){

        if(postLikeRepository.existsByMemberIdAndPostId(memberId,postId)){
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }

        Post post=postRepository.getReferenceById(postId);
        Member member=memberRepository.getReferenceById(memberId);

        PostLike postLike= PostLike.builder()
                .member(member)
                .post(post)
                .build();

        postLikeRepository.save(postLike);
        postRepository.increaseLikeCount(postId);
    }

    public void unlike(Long postId,Long memberId){

        PostLike postLike=postLikeRepository
                .findByMember_IdAndPost_Id(memberId,postId)
                        .orElseThrow(()->new IllegalArgumentException("좋아요를 누르지 않았습니다."));



        postLikeRepository.delete(postLike);
        postRepository.decreaseLikeCount(postId);
    }
}//postLikeRepository.deleteLike(postId,memberId);
