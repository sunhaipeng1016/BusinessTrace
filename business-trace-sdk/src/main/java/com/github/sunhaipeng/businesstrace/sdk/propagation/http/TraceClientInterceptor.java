package com.github.sunhaipeng.businesstrace.sdk.propagation.http;

import com.github.sunhaipeng.businesstrace.sdk.aspect.TraceAspect;
import com.github.sunhaipeng.businesstrace.sdk.propagation.TracePropagationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * HTTP 客户端拦截器
 * 在发起 HTTP 请求时，将 traceId 和 parentSpanId 注入到 HTTP Header 中
 * 用于跨服务的链路追踪传播
 */
public class TraceClientInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TraceClientInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                                        ClientHttpRequestExecution execution) throws IOException {
        String traceId = TraceAspect.getTraceId();
        String parentSpanId = TraceAspect.getParentSpanId();
        
        if (traceId != null && !traceId.isEmpty()) {
            request.getHeaders().set(TracePropagationConstants.HEADER_TRACE_ID, traceId);
            log.debug("Injected traceId into request header: {}", traceId);
        }
        
        if (parentSpanId != null && !parentSpanId.isEmpty()) {
            request.getHeaders().set(TracePropagationConstants.HEADER_PARENT_SPAN_ID, parentSpanId);
            log.debug("Injected parentSpanId into request header: {}", parentSpanId);
        }
        
        return execution.execute(request, body);
    }
}