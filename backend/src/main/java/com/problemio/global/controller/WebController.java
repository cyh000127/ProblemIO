package com.problemio.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // API 요청이나 정적 파일(확장자가 있는 파일)이 아닌 모든 경로를 index.html로 포워딩
    // 이렇게 해야 Vue Router가 작동함 (새로고침 시 404 방지)
    @GetMapping(value = "/{path:[^\\.]*}")
    public String forward() {
        return "forward:/index.html";
    }
}
