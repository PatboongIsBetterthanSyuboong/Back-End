package com.example.bitcomputer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로젝트 루트 찾기
        Path currentPath = Paths.get("").toAbsolutePath();
        
        // BitComputer 폴더 찾기
        Path bitComputerPath = currentPath.resolve("BitComputer");
        if (!Files.exists(bitComputerPath) || !Files.isDirectory(bitComputerPath)) {
            Path parent = currentPath.getParent();
            if (parent != null) {
                bitComputerPath = parent.resolve("BitComputer");
            }
        }
        
        // images 폴더 경로
        Path imagesPath = bitComputerPath.resolve("images");
        String imagesPathStr = imagesPath.toAbsolutePath().toString().replace("\\", "/");
        
        // 정적 리소스 핸들러 등록
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imagesPathStr + "/");
    }
}

