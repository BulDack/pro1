package com.example.demo.web.controller;

import com.example.demo.dto.PostDto;
import com.example.demo.dto.security.PrincipalDetails;
import com.example.demo.ennum.PostSort;
import com.example.demo.entity.Post;
import com.example.demo.service.post.PostLikeService;
import com.example.demo.service.post.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.dto.PostDto.createRequestDto;
import static com.example.demo.dto.PostDto.updateRequestDto;
import static com.example.demo.dto.PostDto.postListResponse;
import static com.example.demo.dto.PostDto.searchRequestDto;
import static com.example.demo.dto.PostDto.postDetailResponse;

import java.util.List;

//실무에서는 보통 이렇게 나눔
//상황	            방식
//게시글 1개 조회	   findById()
//게시글 수정	       findById()
//게시글 삭제	       findById()
//게시글 상세 페이지	상황에 따라 findById() 또는 DTO
//게시글 목록	       QueryDSL + DTO
//검색 결과 목록	   QueryDSL + DTO
//페이징 게시글 목록	QueryDSL + DTO
//좋아요 수 등 집계 데이터 포함	QueryDSL + DTO
//즉, "엔티티가 필요한가?" 이걸 기준으로 생각하기!!
//목록은 엔티티를 다 가져올필요 없이 필요한 컬럼만 가져오는게 효율적
@RestController //@ResponseBody(메서드의 반환값을 페이지 이름으로 해석말고 http응답본문에 그대로 데이터를 넣어 보냄)+controller
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;

    //게시글 리스트 불러오기
    @GetMapping
    public Page<postListResponse>findPosts(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(defaultValue = "latest") PostSort sort
    ){
        return postService.findAll(pageable,sort);

    }

    //게시글 검색한후 리스트 불러오기
    //(GET /posts?sort=LATEST&page=0&size=10 최신순)이런식으로 조건을 달리해 불러올수있음
    @GetMapping("/search")
    public Page<postListResponse>searchFindAll(
            searchRequestDto condition,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(defaultValue = "latest") PostSort sort
    )
    {
        return postService.searchPosts(condition,pageable,sort);

    }

    //게시글 작성
    @PostMapping
    public ResponseEntity<Long>createPost(
            @Valid @RequestBody createRequestDto request,
            @AuthenticationPrincipal PrincipalDetails principalDetails){

        Long postId=postService.createPost(request, principalDetails.getId());
        return ResponseEntity.ok(postId);
    }

    //게시물 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void>updatePost(
            @PathVariable Long id,
            @Valid @RequestBody updateRequestDto request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ){

        postService.updatePost(id,request, principalDetails.getId());
        return ResponseEntity.ok().build();
    }

    //게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ){
        postService.deletePost(id, principalDetails.getId());
        return ResponseEntity.ok().build();
    }

    //게시글 상세조회(수정버튼 활성화할 방법만 찾기)
    @GetMapping("/{id}")
    public postDetailResponse getPost(@PathVariable Long id, @AuthenticationPrincipal PrincipalDetails userDetails
                                              //Model model
    ){
        //postDetailResponse post = postService.findPost(id);
        //model.addAttribute("article", post);

        // 로그인한 상태라면, 이 글의 주인인지 확인하여 '수정' 버튼 노출 여부 결정
        if(userDetails!=null){
            boolean isOwner = id.equals(userDetails.getId());
            //model.addAttribute("isOwner", isOwner); //이부분을 새로 대체할 로직 작성(그게 프론트엔드에서 처리하려나)
        }

        return postService.findPost(id);
    }
    //로그인한 사용자 글 리스트 가져오기(얘도 고쳐야됨 다 옛날 방식,리액트방식으로 다시 고치기)
    @GetMapping("/my")
    public Page<postListResponse> mypost(@AuthenticationPrincipal PrincipalDetails userDetails,
                                         @PageableDefault(size = 20) Pageable pageable
                                         //Model model
    ){

        Page<postListResponse> myPosts = postService.getMyPost(userDetails.getId(),pageable);
        //model.addAttribute("myposts", myPosts);
        //return "articles/myList";
        return myPosts;


        // 만약 성능이 걱정된다면? (Fetch Join)
        //나중에 게시글이 많아지고, 게시글 목록 화면에서 작성자의 이름까지 출력해야 한다면 N+1 문제가 발생할 수 있음
        // 그때는 리포지토리에 @Query를 사용해 한 번에 긁어오는 것이 좋음.
        // fetch join을 사용해 멤버 정보까지 한 번의 쿼리로 가져오기
        //@Query("select a from Article a join fetch a.member where a.member.id = :id")
        //List<Article> findAllByMemberId(@Param("id") Long id);
    }

    //좋아요 누르기 api
    @PostMapping("/{id}/likes")
    public void likePost(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails userDetails
    )
    {
        postLikeService.like(id,userDetails.getId());
    }

    //좋아요 취소(좋아요를 한번 더눌러서)
    @DeleteMapping("/{id}/likes")
    public void unlikePost(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails userDetails
    )
    {
        postLikeService.unlike(id,userDetails.getId());
    }


}
