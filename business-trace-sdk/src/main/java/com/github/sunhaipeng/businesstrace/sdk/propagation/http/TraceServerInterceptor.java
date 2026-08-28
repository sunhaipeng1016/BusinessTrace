package com.github.sunhaipeng.businesstrace.sdk.propagation.http;

import com.github.sunhaipeng.businesstrace.sdk.aspect.TraceAspect;
import com.github.sunhaipeng.businesstrace.sdk.propagation.TracePropagationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP 服务端拦截器
 * 在接收 HTTP 请求时，从 HTTP Header 中提取 traceId 和 parentSpanId
 * 并设置到 ThreadLocal 中，用于链路追踪的上下文传递
 */
public class TraceServerInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TraceServerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
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
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        TraceAspect.clearTraceContext();
    }
}