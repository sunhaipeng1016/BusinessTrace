package com.github.sunhaipeng.businesstrace.sdk.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sunhaipeng.businesstrace.sdk.aspect.TraceAspect;
import com.github.sunhaipeng.businesstrace.sdk.config.TraceProperties;
import com.github.sunhaipeng.businesstrace.sdk.propagation.http.TraceClientInterceptor;
import com.github.sunhaipeng.businesstrace.sdk.reporter.ElasticsearchReporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
@EnableConfigurationProperties(TraceProperties.class)
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = "business.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TraceAutoConfiguration {

    // 配置 RestTemplate 以支持 traceId 传递
    @Bean
    public RestTemplate traceRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // 添加 traceId 传递拦截器
        restTemplate.setInterceptors(Collections.singletonList(new TraceClientInterceptor()));
        return restTemplate;
    }

    @Bean
    public ElasticsearchReporter elasticsearchReporter(TraceProperties properties, 
                                                       ObjectMapper objectMapper) {
        return new ElasticsearchReporter(
            new RestTemplate(),
            objectMapper,
            properties.getElasticsearch().getUri(),
            properties.getElasticsearch().getIndex()
        );
    }

    @Bean
    public TraceAspect traceAspect(TraceProperties properties, 
                                   ElasticsearchReporter reporter, 
                                   ObjectMapper objectMapper) {
        return new TraceAspect(properties, reporter, objectMapper);
    }
}