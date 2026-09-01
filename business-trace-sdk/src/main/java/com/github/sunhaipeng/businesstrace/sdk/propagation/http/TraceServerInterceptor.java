package com.github.sunhaipeng.businesstrace.sdk.propagation.http;

import com.github.sunhaipeng.businesstrace.sdk.aspect.TraceAspect;
import com.github.sunhaipeng.businesstrace.sdk.propagation.TracePropagationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

/**
 * HTTP 服务端拦截器
 * 在接收 HTTP 请求时，从 HTTP Header 中提取 traceId 和 parentSpanId
 * 并设置到 ThreadLocal 中，用于链路追踪的上下文传递
 *
 * 使用 Spring {@link WebRequestInterceptor} 接口，不依赖具体的 Servlet API
 * （javax.servlet 或 jakarta.servlet），天然兼容 Spring Boot 2.x 和 3.x
 */
public class TraceServerInterceptor implements WebRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TraceServerInterceptor.class);

    @Override
    public void preHandle(WebRequest request) {
        String traceId = request.getHeader(TracePropagationConstants.HEADER_TRACE_ID);
        String parentSpanId = request.getHeader(TracePropagationConstants.HEADER_PARENT_SPAN_ID);
        
        if (traceId != null && !traceId.isEmpty()) {
            TraceAspect.setTraceId(traceId);
            log.debug("Extracted traceId from request header: {}", traceId);
        }
        
        if (parentSpanId != null && !parentSpanId.isEmpty()) {
            TraceAspect.setParentSpanId(parentSpanId);
            log.debug("Extracted parentSpanId from request header: {}", parentSpanId);
        }
    }

    @Override
    public void postHandle(WebRequest request, ModelMap model) {
        // 不需要后处理
    }

    @Override
    public void afterCompletion(WebRequest request, Exception ex) {
        TraceAspect.clearTraceContext();
    }
}