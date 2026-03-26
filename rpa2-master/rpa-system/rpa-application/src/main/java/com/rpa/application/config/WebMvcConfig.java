package com.rpa.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }


    public void addViewControllers(ViewControllerRegistry registry) {
        // 将所有非API请求转发到index.html，支持前端路由
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/dashboard").setViewName("forward:/index.html");
        registry.addViewController("/task").setViewName("forward:/index.html");
        registry.addViewController("/execution").setViewName("forward:/index.html");
        registry.addViewController("/robot").setViewName("forward:/index.html");
        registry.addViewController("/process").setViewName("forward:/index.html");
        registry.addViewController("/data-collection").setViewName("forward:/index.html");
        registry.addViewController("/data-parsing").setViewName("forward:/index.html");
        registry.addViewController("/data-processing").setViewName("forward:/index.html");
        registry.addViewController("/data-query").setViewName("forward:/index.html");
        registry.addViewController("/user-info").setViewName("forward:/index.html");
        registry.addViewController("/user-management").setViewName("forward:/index.html");
        registry.addViewController("/role-management").setViewName("forward:/index.html");
        registry.addViewController("/resource-management").setViewName("forward:/index.html");
    }

    // 降级为AntPathMatcher（兼容旧的路径模式写法）
    @Bean
    public PathMatcher pathMatcher() {
        return new AntPathMatcher();
    }

    // Spring Boot 2.6+ 还需要配置
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {

            public void configurePathMatch(PathMatchConfigurer configurer) {
                configurer.setPathMatcher(new AntPathMatcher());
            }
        };
    }
}
