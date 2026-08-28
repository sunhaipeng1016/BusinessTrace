package com.github.sunhaipeng.businesstrace.sdk.reporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sunhaipeng.businesstrace.sdk.model.TraceSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Elasticsearch报告器，用于将 trace span 报告到Elasticsearch
 */
public class ElasticsearchReporter {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchReporter.class);
    /**
     * Elasticsearch 模板，用于构建文档
     */
    private final RestTemplate restTemplate;
    /**
     * JSON 序列化器，用于将 trace span 转换为 JSON 字符串
     */
    private final ObjectMapper objectMapper;
    /**
     * Elasticsearch 服务器 URI
     */
    private final String esUri;
    /**
     * Elasticsearch 索引名称
     */
    private final String index;

    public ElasticsearchReporter(RestTemplate restTemplate, ObjectMapper objectMapper, 
                                 String esUri, String index) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.esUri = esUri;
        this.index = index;
    }

    /**
     * 报告 trace span 到 Elasticsearch
     */
    public void report(TraceSpan span) {
        try {
            Map<String, Object> document = buildDocument(span);
            String json = objectMapper.writeValueAsString(document);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String url = esUri + "/" + index + "/_doc";
            HttpEntity<String> request = new HttpEntity<>(json, headers);
            
            restTemplate.postForEntity(url, request, String.class);
            
            log.info("Trace span reported successfully: traceId={}, spanId={}", 
                     span.getTraceId(), span.getSpanId());
        } catch (Exception e) {
            log.error("Failed to report trace span: traceId={}, spanId={}, error={}", 
                     span.getTraceId(), span.getSpanId(), e.getMessage(), e);
        }
    }

    /**
     * 构建 Elasticsearch 文档，包含 trace span 的所有信息
     */
    private Map<String, Object> buildDocument(TraceSpan span) {
        Map<String, Object> doc = new LinkedHashMap<>();
        
        doc.put("trace_id", span.getTraceId());
        doc.put("span_id", span.getSpanId());
        doc.put("parent_span_id", span.getParentSpanId());
        doc.put("service.name", span.getServiceName());
        doc.put("name", span.getOperationName());
        doc.put("start_time_unix_nano", span.getStartTime().toEpochMilli() * 1000000L);
        doc.put("duration", span.getDurationMs() * 1000000L);
        doc.put("status.code", span.getStatus());
        
        Map<String, Object> attributes = new LinkedHashMap<>();
        if (span.getRequestParams() != null) {
            attributes.put("request.params", span.getRequestParams());
        }
        if (span.getResponseParams() != null) {
            attributes.put("response.params", span.getResponseParams());
        }
        if (span.getErrorMessage() != null) {
            attributes.put("error.message", span.getErrorMessage());
        }
        
        if (span.getCustomAttributes() != null && !span.getCustomAttributes().isEmpty()) {
            for (Map.Entry<String, Object> entry : span.getCustomAttributes().entrySet()) {
                attributes.put("custom." + entry.getKey(), entry.getValue());
            }
        }
        
        if (!attributes.isEmpty()) {
            doc.put("attributes", attributes);
        }
        
        return doc;
    }
}