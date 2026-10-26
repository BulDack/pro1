package com.example.demo.web.controller;


import com.example.demo.config.oauth.dto.PrincipalDetails;
import com.example.demo.service.item.ItemLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/items")
public class ItemLikeController {

    private final ItemLikeService itemLikeService;

    @PostMapping("{itemId}/likes")
    public ResponseEntity<Void>addLike(
            @PathVariable Long itemId,
            @AuthenticationPrincipal PrincipalDetails userDetails

            ){
        itemLikeService.addLike(itemId,userDetails.getId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{itemId}/unlikes")
    public ResponseEntity<Void>removeLike(
            @PathVariable Long itemId,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ){
        itemLikeService.unlike(itemId, userDetails.getId());

        return ResponseEntity.noContent().build();
    }

}
