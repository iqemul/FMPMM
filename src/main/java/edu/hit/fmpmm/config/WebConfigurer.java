package edu.hit.fmpmm.config;

import edu.hit.fmpmm.service.web.UserService;
import edu.hit.fmpmm.util.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器
 */
@Configuration
public class WebConfigurer implements WebMvcConfigurer {
    private final UserService userService;

    @Autowired
    public WebConfigurer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(userService))
                .excludePathPatterns(
                        "/user/login",
                        "/user/register"
                );
    }
}
