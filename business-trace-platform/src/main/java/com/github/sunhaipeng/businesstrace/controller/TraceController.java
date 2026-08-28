package com.github.sunhaipeng.businesstrace.controller;

import com.github.sunhaipeng.businesstrace.model.Span;
import com.github.sunhaipeng.businesstrace.model.TraceSummary;
import com.github.sunhaipeng.businesstrace.service.TraceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Controller
@RequestMapping("/traces")
public class TraceController {

    private final TraceService traceService;

    public TraceController(TraceService traceService) {
        this.traceService = traceService;
    }

    @GetMapping
    public String listTraces(@RequestParam(required = false) String traceId,
                            @RequestParam(required = false) String serviceName,
                            @RequestParam(required = false) String operationName,
                            @RequestParam(required = false) Integer lastHours,
                            @RequestParam(defaultValue = "50") int limit,
                            Model model) throws IOException {
        
        Instant endTime = Instant.now();
        Instant startTime = lastHours != null ? endTime.minusSeconds(lastHours * 3600L) : endTime.minusSeconds(3600L);
        
        List<TraceSummary> traces;
        if (traceId != null && !traceId.isEmpty()) {
            traces = traceService.searchTracesByTraceId(traceId);
        } else {
            traces = traceService.searchTraces(
                serviceName, operationName, null, null,
                startTime, endTime, limit
            );
        }
        
        List<String> serviceNames = traceService.getServiceNames();
        
        model.addAttribute("traces", traces);
        model.addAttribute("serviceNames", serviceNames);
        model.addAttribute("traceId", traceId);
        model.addAttribute("serviceName", serviceName);
        model.addAttribute("operationName", operationName);
        model.addAttribute("lastHours", lastHours != null ? lastHours : 1);
        model.addAttribute("limit", limit);
        model.addAttribute("currentTime", System.currentTimeMillis());
        
        return "trace-list";
    }
}