package com.github.sunhaipeng.businesstrace.sdk.util;

import com.github.sunhaipeng.businesstrace.sdk.aspect.TraceAspect;

import java.util.HashMap;
import java.util.Map;

public class TraceContext {

    // 线程本地上下文存储
    private static final ThreadLocal<Map<String, Object>> CONTEXT_HOLDER = new ThreadLocal<>();

    public static String getTraceId() {
        return TraceAspect.getTraceId();
    }

    public static String getParentSpanId() {
        return TraceAspect.getParentSpanId();
    }

    // 存储上下文信息
    public static void put(String key, Object value) {
        Map<String, Object> context = CONTEXT_HOLDER.get();
        if (context == null) {
            context = new HashMap<>();
            CONTEXT_HOLDER.set(context);
        }
        context.put(key, value);
    }

    public static Object get(String key) {
        Map<String, Object> context = CONTEXT_HOLDER.get();
        if (context == null) {
            return null;
        }
        return context.get(key);
    }

    // 获取所有上下文信息
    public static Map<String, Object> getAll() {
        Map<String, Object> context = CONTEXT_HOLDER.get();
        if (context == null) {
            return new HashMap<>();
        }
        return new HashMap<>(context);
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}