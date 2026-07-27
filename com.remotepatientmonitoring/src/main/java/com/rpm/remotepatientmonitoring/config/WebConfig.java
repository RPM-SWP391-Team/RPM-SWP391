package com.rpm.remotepatientmonitoring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectPath = System.getProperty("user.dir");
        
        // Xác định thư mục uploads tùy thuộc vào vị trí chạy lệnh
        File uploadDir = new File(projectPath, "uploads");
        if (projectPath.endsWith("RPM-SWP391") && !projectPath.contains("com.remotepatientmonitoring")) {
            uploadDir = new File(projectPath + "/com.remotepatientmonitoring", "uploads");
        }

        String uploadPath = uploadDir.getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");

        registry.addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/static/docs/", "file:" + projectPath + "/docs/", "file:" + projectPath + "/data/");
    }
}
