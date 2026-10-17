package com.example.demo.web;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.home.HomeResponse;
import com.example.demo.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ApiResponse<HomeResponse>getHome(){


        HomeResponse response=homeService.getHome();

        return ApiResponse.success("메인화면을 불러옵니다",response);
    }

}
