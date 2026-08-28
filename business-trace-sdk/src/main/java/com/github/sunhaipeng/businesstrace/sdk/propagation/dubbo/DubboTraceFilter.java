package com.github.sunhaipeng.businesstrace.sdk.propagation.dubbo;

import com.github.sunhaipeng.businesstrace.sdk.aspect.TraceAspect;
import com.github.sunhaipeng.businesstrace.sdk.propagation.TracePropagationConstants;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dubbo RPC 过滤器
 * 支持 Consumer 和 Provider 两端的 traceId 传播
 * 通过 Dubbo Attachment 机制传递链路追踪上下文
 */
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class DubboTraceFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(DubboTraceFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext context = RpcContext.getContext();
        
        boolean isProvider = context.isProviderSide();
        
        if (isProvider) {
            return handleProvider(invoker, invocation, context);
        } else {
            return handleConsumer(invoker, invocation, context);
        }
    }

    /**
     * 处理 Provider 端（服务端）逻辑
     * 从 Attachment 中提取 traceId 并设置到 ThreadLocal
     */
    private Result handleProvider(Invoker<?> invoker, Invocation invocation, RpcContext context) {
        String traceId = context.getAttachment(TracePropagationConstants.HEADER_TRACE_ID);
        String parentSpanId = context.getAttachment(TracePropagationConstants.HEADER_PARENT_SPAN_ID);
        
        if (traceId != null && !traceId.isEmpty()) {
            TraceAspect.setTraceId(traceId);
            log.debug("[Dubbo Provider] Extracted traceId from attachment: {}", traceId);
        }
        
        if (parentSpanId != null && !parentSpanId.isEmpty()) {
            TraceAspect.setParentSpanId(parentSpanId);
            log.debug("[Dubbo Provider] Extracted parentSpanId from attachment: {}", parentSpanId);
        }
        
        try {
            return invoker.invoke(invocation);
        } finally {
            TraceAspect.clearTraceContext();
        }
    }

    /**
     * 处理 Consumer 端（客户端）逻辑
     * 将 traceId 设置到 Attachment 中以便传递给 Provider
     */
    private Result handleConsumer(Invoker<?> invoker, Invocation invocation, RpcContext context) {
        String traceId = TraceAspect.getTraceId();
        String parentSpanId = TraceAspect.getParentSpanId();
        
        if (traceId != null && !traceId.isEmpty()) {
            context.setAttachment(TracePropagationConstants.HEADER_TRACE_ID, traceId);
            log.debug("[Dubbo Consumer] Injected traceId into attachment: {}", traceId);
        }
        
        if (parentSpanId != null && !parentSpanId.isEmpty()) {
            context.setAttachment(TracePropagationConstants.HEADER_PARENT_SPAN_ID, parentSpanId);
            log.debug("[Dubbo Consumer] Injected parentSpanId into attachment: {}", parentSpanId);
        }
        
        return invoker.invoke(invocation);
    }
}