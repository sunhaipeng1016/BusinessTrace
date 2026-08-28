package com.github.sunhaipeng.businesstrace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sunhaipeng.businesstrace.model.Span;
import com.github.sunhaipeng.businesstrace.model.TraceSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Service
public class TraceService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String esUri;
    
    @Value("${business.trace.elasticsearch.index:otel-traces}")
    private String traceIndex;

    public TraceService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<TraceSummary> searchTraces(String serviceName, String operationName, 
                                           Long minDurationMs, Long maxDurationMs,
                                           Instant startTime, Instant endTime, int limit) {
        
        Map<String, Object> query = buildSearchQuery(serviceName, operationName, minDurationMs, maxDurationMs, startTime, endTime, limit);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(query, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                esUri + "/" + traceIndex + "/_search",
                request,
                String.class
            );
            
            if (response.getBody() == null) {
                return Collections.emptyList();
            }
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return parseTraceSummaries(jsonNode);
        } catch (Exception e) {
            System.err.println("Failed to search traces: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<TraceSummary> searchTracesByTraceId(String traceId) {
        Map<String, Object> query = Map.of(
            "query", Map.of("term", Map.of("trace_id", traceId)),
            "size", 1000,
            "sort", List.of(Map.of("start_time_unix_nano", "desc"))
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(query, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                esUri + "/" + traceIndex + "/_search",
                request,
                String.class
            );
            
            if (response.getBody() == null) {
                return Collections.emptyList();
            }
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return parseTraceSummaries(jsonNode);
        } catch (Exception e) {
            System.err.println("Failed to search by traceId: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Span> getTraceById(String traceId) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("query", Map.of("term", Map.of("trace_id", traceId)));
        query.put("size", 1000);
        query.put("sort", List.of(Map.of("start_time_unix_nano", "asc")));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(query, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                esUri + "/" + traceIndex + "/_search",
                request,
                String.class
            );
            
            if (response.getBody() == null) {
                return Collections.emptyList();
            }
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return parseSpans(jsonNode);
        } catch (Exception e) {
            System.err.println("Failed to get trace by id: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> getServiceNames() {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("size", 0);
        query.put("aggs", Map.of(
            "services", Map.of(
                "terms", Map.of("field", "service.name.keyword", "size", 100)
            )
        ));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(query, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                esUri + "/" + traceIndex + "/_search",
                request,
                String.class
            );
            
            if (response.getBody() == null) {
                return Collections.emptyList();
            }
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            List<String> services = new ArrayList<>();
            JsonNode buckets = jsonNode.path("aggregations").path("services").path("buckets");
            if (buckets.isArray()) {
                for (JsonNode bucket : buckets) {
                    services.add(bucket.path("key").asText());
                }
            }
            
            return services;
        } catch (Exception e) {
            System.err.println("Failed to get service names: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Map<String, Object> buildSearchQuery(String serviceName, String operationName, 
                                                  Long minDurationMs, Long maxDurationMs,
                                                  Instant startTime, Instant endTime, int limit) {
        List<Map<String, Object>> mustClauses = new ArrayList<>();
        
        if (serviceName != null && !serviceName.isEmpty()) {
            mustClauses.add(Map.of("term", Map.of("service.name.keyword", serviceName)));
        }
        
        if (operationName != null && !operationName.isEmpty()) {
            mustClauses.add(Map.of("wildcard", Map.of("name.keyword", "*" + operationName + "*")));
        }
        
        if (minDurationMs != null) {
            mustClauses.add(Map.of("range", Map.of("duration", Map.of("gte", minDurationMs * 1000000.0))));
        }
        
        if (maxDurationMs != null) {
            mustClauses.add(Map.of("range", Map.of("duration", Map.of("lte", maxDurationMs * 1000000.0))));
        }
        
        if (startTime != null) {
            mustClauses.add(Map.of("range", Map.of("start_time_unix_nano", Map.of("gte", startTime.toEpochMilli() * 1000000.0))));
        }
        
        if (endTime != null) {
            mustClauses.add(Map.of("range", Map.of("start_time_unix_nano", Map.of("lte", endTime.toEpochMilli() * 1000000.0))));
        }
        
        Map<String, Object> query;
        if (mustClauses.isEmpty()) {
            query = Map.of("match_all", Map.of());
        } else {
            query = Map.of("bool", Map.of("must", mustClauses));
        }
        
        return Map.of(
            "query", query,
            "size", limit,
            "sort", List.of(Map.of("start_time_unix_nano", "desc"))
        );
    }

    private List<TraceSummary> parseTraceSummaries(JsonNode response) {
        Map<String, TraceSummary> traceMap = new LinkedHashMap<>();
        
        JsonNode hits = response.path("hits").path("hits");
        if (!hits.isArray()) {
            return Collections.emptyList();
        }
        
        for (JsonNode hit : hits) {
            JsonNode source = hit.path("_source");
            if (source.isMissingNode()) {
                continue;
            }
            
            String traceId = source.path("trace_id").asText();
            if (traceId.isEmpty()) {
                continue;
            }
            
            if (!traceMap.containsKey(traceId)) {
                TraceSummary summary = new TraceSummary();
                summary.setTraceId(traceId);
                summary.setRootServiceName(source.path("service.name").asText());
                summary.setRootOperationName(source.path("name").asText());
                
                long startTimeNano = source.path("start_time_unix_nano").asLong();
                if (startTimeNano > 0) {
                    summary.setStartTime(Instant.ofEpochMilli(startTimeNano / 1000000));
                }
                
                long duration = source.path("duration").asLong();
                if (duration > 0) {
                    summary.setDurationMs(duration / 1000000);
                }
                
                summary.setStatus(source.path("status.code").asText("OK"));
                summary.setSpanCount(1);
                
                // 获取入参、出参、错误信息
                JsonNode attributes = source.path("attributes");
                if (attributes.isObject()) {
                    summary.setRequestParams(getNestedAttributeValue(attributes, "request.params"));
                    summary.setResponseParams(getNestedAttributeValue(attributes, "response.params"));
                    summary.setErrorMessage(getNestedAttributeValue(attributes, "error.message"));
                }
                
                traceMap.put(traceId, summary);
            } else {
                TraceSummary existing = traceMap.get(traceId);
                existing.setSpanCount(existing.getSpanCount() + 1);
            }
        }
        
        return new ArrayList<>(traceMap.values());
    }

    private List<Span> parseSpans(JsonNode response) {
        List<Span> spans = new ArrayList<>();
        
        JsonNode hits = response.path("hits").path("hits");
        if (!hits.isArray()) {
            return spans;
        }
        
        for (JsonNode hit : hits) {
            JsonNode source = hit.path("_source");
            if (source.isMissingNode()) {
                continue;
            }
            
            Span span = new Span();
            span.setTraceId(source.path("trace_id").asText());
            span.setSpanId(source.path("span_id").asText());
            span.setServiceName(source.path("service.name").asText());
            span.setMethodName(source.path("name").asText());
            
            long startTimeNano = source.path("start_time_unix_nano").asLong();
            if (startTimeNano > 0) {
                span.setStartTime(Instant.ofEpochMilli(startTimeNano / 1000000));
            }
            
            long duration = source.path("duration").asLong();
            if (duration > 0) {
                span.setDurationMs(duration / 1000000);
            }
            
            JsonNode attributes = source.path("attributes");
            if (attributes.isObject()) {
                // 处理嵌套字段名（带点号的键名）
                span.setRequestParams(getNestedAttributeValue(attributes, "request.params"));
                span.setResponseParams(getNestedAttributeValue(attributes, "response.params"));
                span.setErrorMessage(getNestedAttributeValue(attributes, "error.message"));
            }
            
            spans.add(span);
        }
        
        return spans;
    }
    
    /**
     * 获取 attributes 中的嵌套属性值（支持带点号的键名）
     */
    private String getNestedAttributeValue(JsonNode attributes, String key) {
        // 先尝试直接获取（点号作为键名的一部分）
        JsonNode node = attributes.get(key);
        if (node != null && !node.isNull()) {
            return node.asText();
        }
        
        // 如果直接获取失败，尝试按点号分割获取
        String[] parts = key.split("\\.");
        JsonNode current = attributes;
        for (String part : parts) {
            current = current.path(part);
            if (current.isMissingNode()) {
                return null;
            }
        }
        
        return current.isTextual() ? current.asText() : null;
    }
}