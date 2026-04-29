package com.rpa.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源处理器，排除 API 请求
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600)
                .resourceChain(false);
        
        // 配置上传文件访问路径（使用绝对路径）
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
        
        // 同时支持相对路径访问（兼容不同部署方式）
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 将所有非 API 路径映射到 index.html，由 Vue Router 处理
        // 这是为了解决 SPA 应用刷新后 404 的问题
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/robot/**").setViewName("forward:/index.html");
        registry.addViewController("/task/**").setViewName("forward:/index.html");
        registry.addViewController("/execution/**").setViewName("forward:/index.html");
        registry.addViewController("/process/**").setViewName("forward:/index.html");
        registry.addViewController("/system/**").setViewName("forward:/index.html");
        registry.addViewController("/data/**").setViewName("forward:/index.html");
    }
}
