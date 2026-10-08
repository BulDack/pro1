package com.example.demo.service;


import com.example.demo.dto.home.HomeResponse;
import com.example.demo.dto.home.ItemSummaryResponse;
import com.example.demo.dto.home.PostSummaryResponse;
import com.example.demo.repository.ItemQueryRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.post.PostQueryRepository;
import com.example.demo.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final ItemQueryRepository itemQueryRepository;
    private final PostQueryRepository postQueryRepository;

    //메인화면에서 보여줄 데이터만 각각 조회
    public HomeResponse getHome() {

        List<ItemSummaryResponse>popularItems=itemQueryRepository.findPopularItems(4);
        List<ItemSummaryResponse>newItems=itemQueryRepository.findNewItems(4);
        List<PostSummaryResponse>popularPosts=postQueryRepository.findPopularPosts(5);
        List<PostSummaryResponse>recentPosts=postQueryRepository.findNewPosts(5);


        return new HomeResponse(
                popularItems,
                newItems,
                popularPosts,
                recentPosts
        );
    }
}
