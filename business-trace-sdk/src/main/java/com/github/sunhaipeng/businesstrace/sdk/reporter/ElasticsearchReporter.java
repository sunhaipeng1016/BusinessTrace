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

public class ElasticsearchReporter {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchReporter.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String esUri;
    private final String index;

    public ElasticsearchReporter(RestTemplate restTemplate, ObjectMapper objectMapper, 
                                 String esUri, String index) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.esUri = esUri;
        this.index = index;
    }

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