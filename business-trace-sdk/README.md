# Business Trace SDK

轻量级业务链路跟踪SDK，基于Spring Boot和AOP实现，支持自动采集方法调用链路的入参、出参、错误信息和耗时，并上报到Elasticsearch。

## 特性

- **零侵入**：只需添加`@TraceMethod`注解即可自动跟踪方法调用
- **轻量级**：无外部依赖，仅依赖Spring Boot和AspectJ
- **跨版本兼容**：支持Spring Boot 2.x和3.x，Java 1.8+
- **自动采集**：自动记录方法入参、出参、错误信息和执行耗时
- **链路追踪**：支持跨方法调用链追踪，自动生成TraceId
- **自定义上下文**：支持通过`TraceContext`添加自定义业务信息

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.github.sunhaipeng</groupId>
    <artifactId>business-trace-sdk</artifactId>
    <version>0.0.3-SNAPSHOT</version>
</dependency>
```

### 2. 配置

在`application.yml`或`application.properties`中添加配置：

```yaml
business:
  trace:
    enabled: true                    # 启用链路跟踪
    service-name: your-service-name  # 服务名称
    elasticsearch:
      uri: http://localhost:9200     # Elasticsearch地址
      index: otel-traces             # 索引名称
```

### 3. 使用注解

```java
import com.github.sunhaipeng.businesstrace.sdk.annotation.TraceMethod;

@Service
public class OrderService {
    
    @TraceMethod
    public Order createOrder(OrderRequest request) {
        // 业务逻辑
        return order;
    }
    
    @TraceMethod(operationName = "custom-operation")
    public void doSomething() {
        // 自定义操作名
    }
    
    @TraceMethod(captureParams = false, captureResult = false)
    public void skipCapture() {
        // 不捕获入参和出参
    }
}
```

## 注解说明

### @TraceMethod

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| operationName | String | "" | 自定义操作名称，为空则使用`类名.方法名` |
| captureParams | boolean | true | 是否捕获方法入参 |
| captureResult | boolean | true | 是否捕获方法返回值 |

## 高级功能

### 添加自定义上下文信息

```java
import com.github.sunhaipeng.businesstrace.sdk.util.TraceContext;

public void businessMethod() {
    TraceContext.put("userId", "12345");
    TraceContext.put("orderId", "ORD-2026-001");
    
    // 业务逻辑...
}
```

### 获取当前TraceId

```java
String traceId = TraceContext.getTraceId();
```

## 配置说明

| 配置项 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| business.trace.enabled | boolean | 否 | true | 是否启用链路跟踪 |
| business.trace.service-name | String | 否 | 类名 | 服务名称，建议使用有意义的名称 |
| business.trace.elasticsearch.uri | String | 是 | - | Elasticsearch地址 |
| business.trace.elasticsearch.index | String | 是 | - | Elasticsearch索引名称 |

## 兼容性

- **Java**: 1.8+
- **Spring Boot**: 2.x, 3.x

## 数据格式

SDK会将跟踪数据以OTLP兼容格式上报到Elasticsearch，包含以下字段：

- `trace_id`: 链路追踪ID
- `span_id`: 当前Span ID
- `parent_span_id`: 父Span ID
- `service.name`: 服务名称
- `operation.name`: 操作名称
- `start_time_unix_nano`: 开始时间（纳秒）
- `end_time_unix_nano`: 结束时间（纳秒）
- `duration_ms`: 耗时（毫秒）
- `status.code`: 状态码（0=成功，1=失败）
- `request.params`: 请求参数（JSON）
- `response.params`: 响应参数（JSON）
- `error.message`: 错误信息

## License

Apache License 2.0