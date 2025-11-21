package com.problemio.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // Image 저장, 불러오기 로직
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 브라우저에서 /uploads/** 로 요청을 보내면 -> 로컬의 C:/upload/ 폴더로 연결
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///C:/upload/");
    }
}