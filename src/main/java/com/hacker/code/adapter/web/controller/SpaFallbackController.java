package com.hacker.code.adapter.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA 回退控制器：将非 API 请求转发到前端 index.html，支持 Vue Router history 模式。
 */
@Controller
public class SpaFallbackController {

    @RequestMapping(value = {
            "/",
            "/dashboard",
            "/rank",
            "/strategy/**",
            "/fund/**",
            "/backtest/**",
            "/report/**",
            "/system/**"
    })
    public String fallback() {
        return "forward:/index.html";
    }
}
