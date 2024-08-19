package edu.hit.fmpmm.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class FileUploadConfig {
    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;
    @Value("${spring.servlet.multipart.max-request-size}")
    private DataSize maxRequestSize;
    @Value("${spring.servlet.multipart.location}")
    private String location;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 设置最大文件大小
        factory.setMaxFileSize(maxFileSize);
        // 设置最大请求大小
        factory.setMaxRequestSize(maxRequestSize);  // DataSize.ofMegabytes(10)
        // 设置临时文件存储位置
        factory.setLocation(location);
        return factory.createMultipartConfig();
    }
}
