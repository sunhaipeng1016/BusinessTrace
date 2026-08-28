package com.github.sunhaipeng.businesstrace.sdk.propagation;

/**
 * 链路追踪传播常量
 * 定义 HTTP Header 和 Dubbo Attachment 中使用的键名
 */
public final class TracePropagationConstants {

    /**
     * Trace ID 的键名
     */
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * 父 Span ID 的键名
     */
    public static final String HEADER_PARENT_SPAN_ID = "X-Parent-Span-Id";

    /**
     * Span ID 的键名
     */
    public static final String HEADER_SPAN_ID = "X-Span-Id";
    
    private TracePropagationConstants() {
    }
}