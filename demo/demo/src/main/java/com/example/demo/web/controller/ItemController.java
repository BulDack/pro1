package com.example.demo.web.controller;


import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ItemDto;
import com.example.demo.config.oauth.dto.PrincipalDetails;
import com.example.demo.ennum.ItemSort;
import com.example.demo.service.item.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


//import static com.example.demo.dto.ItemDto.Response;
import java.util.List;

import static com.example.demo.dto.ItemDto.ItemUpdateRequest;
import static com.example.demo.dto.ItemDto.ItemListResponse;
import static com.example.demo.dto.ItemDto.ItemSearchRequest;
import static com.example.demo.dto.ItemDto.ItemDetailResponse;
import static com.example.demo.dto.ItemDto.ItemCreateRequest;


@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;


    //상품등록
    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Long>createItem(
            @Valid @RequestPart("request") ItemCreateRequest request,
            @RequestPart("images") List<MultipartFile>images,
            @AuthenticationPrincipal PrincipalDetails userDetails // 필요시 등록한 관리자 정보 추출 가능
            ){
        Long itemId=itemService.registerItem(request,images,userDetails.getId());

        return ApiResponse.success("상품등록에 성공했습니다",itemId);
    }

    //상품전체조회(권한 없음 - 비회원도 가능)
    //* SecurityConfig에서 /api/items는 permitAll()로 열어두기!!!!
    @GetMapping
    public ApiResponse<Page<ItemListResponse>>getItems(
            @ModelAttribute ItemSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(defaultValue="LATEST") ItemSort sort
    ){
        System.out.println("Controller categoryIds = " + request.categoryId());
        Page<ItemListResponse> result=itemService.findAllItems(request,pageable,sort);
        return ApiResponse.success("게시물을 불러왔습니다",result);
    }

    //만약 condition 객체 자체가 null로 넘어온다면, where 절 내부에서 condition.getSearchKeyword() 등을 호출할 때 NullPointerException (NPE)이 발생하게 됩니다.
    //스프링 컨트롤러에서 @ModelAttribute 등으로 받을 때는 쿼리 파라미터가 비어있어도 빈 객체(new ItemSearchCondition())를 자동으로 생성해 주기 때문에 보통은 null이 아니지만,
    // 외부에서 이 메서드를 직접 호출하거나 테스트 코드를 작성할 때는 null이 들어올 수 있으므로 방어 코드(Null-safe)를 작성하는 것이 안전합니다.
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    //상품 단건 상세조회
    //* SecurityConfig에서 /api/items는 permitAll()로 열어두기!!!!
    @GetMapping("/{itemId}")
    public ApiResponse<ItemDetailResponse>getItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal PrincipalDetails userDetails){

        Long memberId=userDetails != null ? userDetails.getId() :null;
        ItemDetailResponse item=itemService.getItemDetail(itemId,memberId);
        return ApiResponse.success("상품을 조회합니다.",item);
    }


    @PatchMapping("/{itemId}/update")
//    @PreAuthorize("hasRole('ADMIN')") //상품수정(관리자만 가능)
    public ApiResponse<Void>updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemUpdateRequest requestDto,  //다음에 할때 valid 검색해서 정리하기!!!!
            @AuthenticationPrincipal PrincipalDetails userDetails
    ){
//        ItemDetailResponse updateItem=itemService.updateItem(itemId,userDetails.getId(),requestDto);
        itemService.updateItem(itemId,userDetails.getId(),requestDto);
        return ApiResponse.success("상품 업데이트에 성공햇습니다.");
    }


    @DeleteMapping("/{itemId}")
    //@PreAuthorize("hasRole('ADMIN')") //상품삭제(관리자만 가능)
    public ApiResponse<Void>deleteItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal PrincipalDetails userDetails){

        itemService.deleteItem(itemId,userDetails.getId());
        return ApiResponse.success("상품 삭제에 성공햇습니다.");
    }

//    @GetMapping(value="/items/new")
//    public String createForm(Model model){
//        model.addAttribute("form",new ItemForm());
//        return "items/createItemForm";
//    }
//
//    //상품등록
//    @PostMapping(value="/items/new")
//    public String craete(ItemForm form){
//
//        return "redirect:/items";
//
//        }
//
//    //상품목록 조회
//    @GetMapping(value="/items")
//    public String list(Model model){
//
//        List<Item>items=itemService.findItems();
//        model.addAttribute("items",items);
//
//        return "items/itemList";
//    }
//
//    @GetMapping(value="/items/{itemId}/edit")
//    public String updateItemForm(@PathVariable("itemId")Long itemId,Model model){
//        Item item= itemService.findById(itemId).get();
//
//        //item폼에 넣기 이부분
//        ItemForm form= ItemForm.builder()
//                .id(item.getId())
//                .name(item.getName())
//                .price(item.getPrice())
//                .stockQuantity(item.getStockQuantity())
//                .author(item.getAuthor())
//              .isbn(item.getIsbn())
//                .build();
//
//        model.addAttribute("form",form);
//        return "items/updateItemForm";
//    }
//
//    //상품수정
//    @PostMapping(value="/items/{itemId}/edit")
//    public String updateItem(@PathVariable Long itemId,@ModelAttribute("form") ItemDto.UpdateRequest request){
//
//        itemService.updateItem(itemId,request);
//
//        return "redirect:/items";
//    }

}
