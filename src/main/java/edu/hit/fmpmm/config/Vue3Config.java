package edu.hit.fmpmm.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class Vue3Config {
    @Value("${vue3.url}")
    public String url;
}
