package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        /*
         * application.yml에 설정한 실제 업로드 폴더를 가져온다.
         *
         * 현재 설정:
         * C:/Users/bfbas/IdeaProjects/pro1/demo/demo/uploads
         *
         * 상대경로를 사용하지 않으므로
         * IntelliJ의 실행 작업 디렉터리가 달라져도
         * 기존 이미지 위치가 바뀌지 않는다.
         */
        Path uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();


        /*
         * /uploads/** 경로로 들어오는 요청을
         * 실제 서버 파일 시스템의 uploads 폴더로 연결한다.
         *
         * 예:
         * /uploads/products/abc.jpg
         *  → uploads/products/abc.jpg
         */
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath.toUri().toString());

//        registry
//                .addResourceHandler("/uploads/**")
//                .addResourceLocations("file:///C:/Users/bfbas/IdeaProjects/pro1/demo/demo/uploads");
    }
}
