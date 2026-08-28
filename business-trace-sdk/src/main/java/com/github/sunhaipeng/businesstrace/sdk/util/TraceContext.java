package com.github.sunhaipeng.businesstrace.sdk.util;

import java.util.HashMap;
import java.util.Map;

public class TraceContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT_HOLDER = new ThreadLocal<>();

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