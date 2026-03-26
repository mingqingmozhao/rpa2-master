package com.rpa.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，用于访问上传的头像
        String userDir = System.getProperty("user.dir");
        String uploadDir = userDir + File.separator + "uploads" + File.separator + "avatars";
        
        // 添加日志输出路径
        System.out.println("头像上传目录：" + uploadDir);
        System.out.println("头像访问 URL: /uploads/avatars/**");
        
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + uploadDir + File.separator);
    }
}
