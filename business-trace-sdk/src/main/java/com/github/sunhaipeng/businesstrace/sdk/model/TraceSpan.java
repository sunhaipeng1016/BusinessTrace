package com.github.sunhaipeng.businesstrace.sdk.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class TraceSpan {
    // 跟踪 ID
    private String traceId;
    // 跟踪 span ID
    private String spanId;
    // 父 span ID
    private String parentSpanId;
    // 服务名称
    private String serviceName;
    // 操作名称
    private String operationName;
    // 开始时间
    private Instant startTime;
    // 结束时间
    private Instant endTime;
    // 持续时间（毫秒）
    private Long durationMs;
    // 请求参数
    private String requestParams;
    // 响应参数
    private String responseParams;
    // 错误消息
    private String errorMessage;
    // 状态
    private String status = "OK";
    // 自定义属性
    private Map<String, Object> customAttributes;

    public TraceSpan() {
        this.spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        this.startTime = Instant.now();
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getResponseParams() {
        return responseParams;
    }

    public void setResponseParams(String responseParams) {
        this.responseParams = responseParams;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = "ERROR";
    }

    public String getStatus() {
        return status;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public void complete() {
        this.endTime = Instant.now();
        this.durationMs = this.endTime.toEpochMilli() - this.startTime.toEpochMilli();
    }

    public Map<String, Object> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(Map<String, Object> customAttributes) {
        this.customAttributes = customAttributes;
    }
}