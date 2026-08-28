package com.github.sunhaipeng.businesstrace.model;

import java.time.Instant;

public class TraceSummary {
    private String traceId;
    private String rootServiceName;
    private String rootOperationName;
    private Instant startTime;
    private Long durationMs;
    private Integer spanCount;
    private String status;
    private String requestParams;
    private String responseParams;
    private String errorMessage;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRootServiceName() {
        return rootServiceName;
    }

    public void setRootServiceName(String rootServiceName) {
        this.rootServiceName = rootServiceName;
    }

    public String getRootOperationName() {
        return rootOperationName;
    }

    public void setRootOperationName(String rootOperationName) {
        this.rootOperationName = rootOperationName;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Integer getSpanCount() {
        return spanCount;
    }

    public void setSpanCount(Integer spanCount) {
        this.spanCount = spanCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
    }
}