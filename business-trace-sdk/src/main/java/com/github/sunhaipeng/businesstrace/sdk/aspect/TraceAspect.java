package com.github.sunhaipeng.businesstrace.sdk.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sunhaipeng.businesstrace.sdk.annotation.TraceMethod;
import com.github.sunhaipeng.businesstrace.sdk.config.TraceProperties;
import com.github.sunhaipeng.businesstrace.sdk.model.TraceSpan;
import com.github.sunhaipeng.businesstrace.sdk.reporter.ElasticsearchReporter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import com.github.sunhaipeng.businesstrace.sdk.util.TraceContext;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.UUID;

@Aspect
@Order(1)
public class TraceAspect {

    private static final Logger log = LoggerFactory.getLogger(TraceAspect.class);
    static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();
    static final ThreadLocal<String> PARENT_SPAN_ID_HOLDER = new ThreadLocal<>();
    
    public static void setTraceId(String traceId) {
        TRACE_ID_HOLDER.set(traceId);
    }
    
    public static String getTraceId() {
        return TRACE_ID_HOLDER.get();
    }
    
    public static void setParentSpanId(String parentSpanId) {
        PARENT_SPAN_ID_HOLDER.set(parentSpanId);
    }
    
    public static String getParentSpanId() {
        return PARENT_SPAN_ID_HOLDER.get();
    }
    
    public static void clearTraceContext() {
        TRACE_ID_HOLDER.remove();
        PARENT_SPAN_ID_HOLDER.remove();
    }

    private final TraceProperties properties;
    private final ElasticsearchReporter reporter;
    private final ObjectMapper objectMapper;

    public TraceAspect(TraceProperties properties, ElasticsearchReporter reporter, ObjectMapper objectMapper) {
        this.properties = properties;
        this.reporter = reporter;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(traceMethod)")
    public Object traceMethod(ProceedingJoinPoint joinPoint, TraceMethod traceMethod) throws Throwable {
        // 检查是否开启了 trace 功能
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        String serviceName = getServiceName(joinPoint);
        String operationName = getOperationName(traceMethod, joinPoint);

        // 创建 span
        TraceSpan span = new TraceSpan();
        span.setServiceName(serviceName);
        span.setOperationName(operationName);

        // 从 ThreadLocal 中获取 traceId
        String traceId = TRACE_ID_HOLDER.get();
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().replace("-", "");
            TRACE_ID_HOLDER.set(traceId);
        }
        span.setTraceId(traceId);

        // 从 ThreadLocal 中获取 parentSpanId
        String parentSpanId = PARENT_SPAN_ID_HOLDER.get();
        if (parentSpanId != null) {
            span.setParentSpanId(parentSpanId);
        }

        // 捕获请求参数
        if (traceMethod.captureParams()) {
            try {
                Object[] args = joinPoint.getArgs();
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                String[] parameterNames = signature.getParameterNames();
                
                if (args != null && args.length > 0) {
                    if (parameterNames != null && parameterNames.length > 0) {
                        Map<String, Object> paramsMap = new java.util.LinkedHashMap<>();
                        for (int i = 0; i < args.length; i++) {
                            String paramName = i < parameterNames.length ? parameterNames[i] : "arg" + i;
                            paramsMap.put(paramName, args[i]);
                        }
                        span.setRequestParams(objectMapper.writeValueAsString(paramsMap));
                    } else {
                        span.setRequestParams(objectMapper.writeValueAsString(args));
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to capture request params: {}", e.getMessage());
            }
        }
        
        // 设置 spanId 到 ThreadLocal
        PARENT_SPAN_ID_HOLDER.set(span.getSpanId());
        
        try {
            Object result = joinPoint.proceed();
            
            if (traceMethod.captureResult() && result != null) {
                try {
                    // 捕获响应参数
                    span.setResponseParams(objectMapper.writeValueAsString(result));
                } catch (Exception e) {
                    log.error("Failed to capture response params: {}", e.getMessage());
                }
            }

            // 完成 span
            span.complete();
            
            // 捕获自定义属性
            Map<String, Object> customAttributes = TraceContext.getAll();
            span.setCustomAttributes(customAttributes);

            // 报告 span
            reporter.report(span);
            TraceContext.clear();
            
            log.info("Trace completed: service={}, operation={}, duration={}ms",
                     serviceName, operationName, span.getDurationMs());
            
            return result;
        } catch (Throwable throwable) {
            // 设置错误信息
            span.setErrorMessage(throwable.getMessage());
            span.complete();
            
            // 捕获自定义属性
            Map<String, Object> customAttributes = TraceContext.getAll();
            span.setCustomAttributes(customAttributes);
            
            // 报告 span
            reporter.report(span);
            TraceContext.clear();
            
            log.error("Trace error: service={}, operation={}, error={}", 
                     serviceName, operationName, throwable.getMessage());
            
            throw throwable;
        } finally {
            // 清除 ThreadLocal 中的 spanId
            PARENT_SPAN_ID_HOLDER.remove();
            if (parentSpanId == null) {
                TRACE_ID_HOLDER.remove();
            }
        }
    }

    private String getServiceName(ProceedingJoinPoint joinPoint) {
        if (properties.getServiceName() != null && !properties.getServiceName().isEmpty()) {
            return properties.getServiceName();
        }
        return joinPoint.getTarget().getClass().getSimpleName();
    }

    private String getOperationName(TraceMethod traceMethod, ProceedingJoinPoint joinPoint) {
        if (!traceMethod.operationName().isEmpty()) {
            return traceMethod.operationName();
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }
}