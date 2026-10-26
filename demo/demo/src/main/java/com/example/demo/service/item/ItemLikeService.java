package com.example.demo.service.item;


import com.example.demo.entity.Member;
import com.example.demo.entity.item.Item;
import com.example.demo.entity.item.ItemLike;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.item.ItemLikeRepository;
import com.example.demo.repository.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemLikeService {

    private final ItemLikeRepository itemLikeRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    public void addLike(Long itemId,Long memberId){
        if(itemLikeRepository.existsByMemberIdAndItemId(memberId,itemId)){
            throw new IllegalArgumentException("이미 찜을 눌렀습니다.");
        }
        Item item=itemRepository.getReferenceById(itemId);
        Member member=memberRepository.getReferenceById(memberId);

        ItemLike itemLike=ItemLike.builder()
                .member(member)
                .item(item)
                .build();

        itemLikeRepository.save(itemLike);
        itemRepository.increaseLikeCount(itemId);
    }

    public void unlike(Long itemId,Long memberId){

        ItemLike itemLike = itemLikeRepository
                .findByMember_IdAndItem_Id(memberId,itemId)
                .orElseThrow(()->new IllegalArgumentException("찜을 누르지 않았습니다"));

        itemLikeRepository.delete(itemLike);
        itemRepository.decreaseLikeCount(itemId);
    }
}
