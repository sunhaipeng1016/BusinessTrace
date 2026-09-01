package com.github.sunhaipeng.businesstrace.sdk.propagation.http;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 拦截器配置
 * 自动注册 TraceServerInterceptor 到所有 HTTP 请求路径
 */
@Configuration
@ConditionalOnClass(WebMvcConfigurer.class)
public class TraceWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(new TraceServerInterceptor())
                .addPathPatterns("/**");
    }
}