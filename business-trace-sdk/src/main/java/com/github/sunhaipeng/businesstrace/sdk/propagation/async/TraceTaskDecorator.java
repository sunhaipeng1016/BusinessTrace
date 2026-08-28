package com.github.sunhaipeng.businesstrace.sdk.propagation.async;

import com.github.sunhaipeng.businesstrace.sdk.aspect.TraceAspect;
import org.springframework.core.task.TaskDecorator;

/**
 * 异步线程池 TaskDecorator
 * 用于在线程池任务间传递 traceId 和 parentSpanId
 * 解决异步场景下 ThreadLocal 数据丢失的问题
 */
public class TraceTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        String traceId = TraceAspect.getTraceId();
        String parentSpanId = TraceAspect.getParentSpanId();
        
        return () -> {
            try {
                if (traceId != null) {
                    TraceAspect.setTraceId(traceId);
                }
                if (parentSpanId != null) {
                    TraceAspect.setParentSpanId(parentSpanId);
                }
                runnable.run();
            } finally {
                TraceAspect.clearTraceContext();
            }
        };
    }
}