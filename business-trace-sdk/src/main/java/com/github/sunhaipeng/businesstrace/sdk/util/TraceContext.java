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

    /**
     * 获取当前上下文的快照（副本）。
     * 供 TraceAspect 在嵌套 span 结束时恢复外层上下文使用，属于 SDK 内部 API。
     *
     * @return 当前上下文的副本；当前没有上下文时返回 null
     */
    public static Map<String, Object> snapshot() {
        Map<String, Object> context = CONTEXT_HOLDER.get();
        return context == null ? null : new HashMap<>(context);
    }

    /**
     * 将上下文恢复为指定快照，属于 SDK 内部 API。
     *
     * @param snapshot 之前通过 {@link #snapshot()} 获取的快照；传 null 表示恢复到空状态
     */
    public static void restore(Map<String, Object> snapshot) {
        if (snapshot == null) {
            CONTEXT_HOLDER.remove();
        } else {
            CONTEXT_HOLDER.set(new HashMap<>(snapshot));
        }
    }
}