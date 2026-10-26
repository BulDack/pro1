package com.example.demo.dto.my;

import com.example.demo.entity.Post.Post;
import com.example.demo.entity.item.Item;

import java.time.LocalDateTime;

public class MyResponse {


    public record MemberResponse(
            String nickname
    ){
    }
    public record PostResponse(
            String title
    ){
        public static PostResponse from(Post post) {
            return new PostResponse(
                    post.getTitle()
            );
        }
    }

    public record ItemResponse(
            String name,
            int price,
            int stockQuantity,
            String thumbnailUrl
    ){
        //팩터리 메서드
        public static ItemResponse from(Item item) {
            return new ItemResponse(
                    item.getName(),
                    item.getPrice(),
                    item.getStockQuantity(),
                    item.getThumbnailUrl()
            );
        }
    }

    public record OrderResponse(){

    }
}
