package com.rpa.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 将所有非 API 前端路由 fallback 到 index.html，
 * 交由 Vue Router 处理（SPA 模式）。
 */
@Controller
public class FrontendRouteController {

    /**
     * 所有未匹配的后端 API 路径之外的请求，都返回 index.html。
     * 通过 ViewController 直接渲染，不走视图解析器。
     * 注意：排除了 /robot, /user, /task, /process 等 API 路径，避免与 REST Controller 冲突
     */
    @GetMapping(value = {
        "/login", "/dashboard",
        "/user-management", "/role-management", "/resource-management",
        "/user-info",
        "/task", "/execution", "/process",
        "/data-collection", "/data-parsing", "/data-processing", "/data-query"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
