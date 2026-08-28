# Business Trace SDK

轻量级业务链路跟踪SDK，基于Spring Boot和AOP实现，支持自动采集方法调用链路的入参、出参、错误信息和耗时，并上报到Elasticsearch。

## 特性

- **零侵入**：只需添加`@TraceMethod`注解即可自动跟踪方法调用
- **轻量级**：核心功能仅依赖Spring Boot和AspectJ，Dubbo为可选依赖
- **跨版本兼容**：支持Spring Boot 2.x和3.x，Java 1.8+
- **自动采集**：自动记录方法入参、出参、错误信息和执行耗时
- **链路追踪**：支持跨方法调用链追踪，自动生成TraceId
- **分布式追踪**：支持HTTP、Dubbo RPC跨服务traceId传播
- **异步支持**：支持线程池场景的traceId传递
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
String parentSpanId = TraceContext.getParentSpanId();
```

## 分布式追踪

SDK 支持完整的分布式追踪，traceId 可以在以下场景中自动传播：

1. **HTTP 请求传播** - 通过 HTTP Header 传递 traceId
2. **Dubbo RPC 传播** - 通过 Dubbo Attachment 传递 traceId
3. **异步线程池传播** - 通过 TaskDecorator 传递 traceId
4. **同线程方法调用** - 通过 ThreadLocal 传递 traceId

### HTTP 场景（自动生效）

#### 服务 A（调用方）
```java
@Service
public class ServiceA {
    
    @Autowired
    private RestTemplate traceRestTemplate; // 使用 SDK 提供的 RestTemplate
    
    @TraceMethod
    public void callServiceB() {
        // traceId 会自动注入到 HTTP Header 中
        String response = traceRestTemplate.getForObject(
            "http://service-b/api/data", 
            String.class
        );
    }
}
```

#### 服务 B（被调用方）
```java
@RestController
public class ControllerB {
    
    @TraceMethod(operationName = "handleServiceBRequest")
    @GetMapping("/api/data")
    public String handleRequest() {
        // traceId 会自动从 HTTP Header 中提取
        // 与服务 A 的调用链路关联
        return "data";
    }
}
```

**HTTP 传播流程**：
```
服务 A (客户端)                          服务 B (服务端)
     │                                        │
     │  1. @TraceMethod 标注方法               │
     │     ↓                                   │
     │  2. 生成/获取 traceId                   │
     │     ↓                                   │
     │  3. RestTemplate 发起 HTTP 请求         │
     │     ↓                                   │
     │  4. TraceClientInterceptor 注入 Header  │
     │     - X-Trace-Id: xxx                   │
     │     - X-Parent-Span-Id: yyy             │
     │─────────────────────────────────────────>│
     │                                        │ 5. TraceServerInterceptor 提取 Header
     │                                        │    ↓
     │                                        │ 6. 设置到 ThreadLocal
     │                                        │    ↓
     │                                        │ 7. @TraceMethod 使用相同的 traceId
     │                                        │    ↓
     │                                        │ 8. 上报到 Elasticsearch
```

### Dubbo RPC 场景（自动生效）

#### 服务 A（Consumer - 调用方）
```java
@Service
public class ServiceA {
    
    @DubboReference
    private RemoteService remoteService;
    
    @TraceMethod
    public void callRemoteService() {
        // traceId 会自动注入到 Dubbo Attachment 中
        String result = remoteService.sayHello("world");
    }
}
```

#### 服务 B（Provider - 被调用方）
```java
@DubboService
public class RemoteServiceImpl implements RemoteService {
    
    @TraceMethod(operationName = "sayHello")
    @Override
    public String sayHello(String name) {
        // traceId 会自动从 Dubbo Attachment 中提取
        // 与服务 A 的调用链路关联
        return "Hello, " + name;
    }
}
```

**Dubbo 传播流程**：
```
服务 A (Consumer)                          服务 B (Provider)
     │                                        │
     │  1. @TraceMethod 标注方法               │
     │     ↓                                   │
     │  2. 生成/获取 traceId                   │
     │     ↓                                   │
     │  3. Dubbo RPC 调用                      │
     │     ↓                                   │
     │  4. DubboTraceFilter (Consumer)         │
     │     设置 Attachment:                    │
     │     - X-Trace-Id: xxx                   │
     │     - X-Parent-Span-Id: yyy             │
     │─────────────────────────────────────────>│
     │                                        │ 5. DubboTraceFilter (Provider)
     │                                        │    从 Attachment 提取 traceId
     │                                        │    ↓
     │                                        │ 6. 设置到 ThreadLocal
     │                                        │    ↓
     │                                        │ 7. @TraceMethod 使用相同的 traceId
     │                                        │    ↓
     │                                        │ 8. 上报到 Elasticsearch（关联链路）
```

**Dubbo 配置说明**：
- Dubbo Filter 已通过 SPI 自动注册，无需额外配置
- 支持 Dubbo 3.x 版本（Dubbo 2.x 需要调整 Filter 实现）
- Consumer 和 Provider 两端都需要引入 business-trace-sdk

### 异步线程池场景

#### 配置线程池
```java
@Configuration
public class AsyncConfig {
    
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        
        // 设置 TaskDecorator 传递 traceId
        executor.setTaskDecorator(new TraceTaskDecorator());
        
        executor.initialize();
        return executor;
    }
}
```

#### 使用异步调用
```java
@Service
public class AsyncService {
    
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    
    @TraceMethod
    public void asyncProcess() {
        // traceId 会自动传递到异步线程
        taskExecutor.submit(() -> {
            // 这里可以获取到相同的 traceId
            String traceId = TraceContext.getTraceId();
            System.out.println("Async traceId: " + traceId);
        });
    }
}
```

### 传播协议说明

#### HTTP Header

| Header 名称 | 说明 | 示例值 |
|------------|------|--------|
| `X-Trace-Id` | 全局追踪 ID | `4bf92f3577b34da6a3ce929d0e0e4736` |
| `X-Parent-Span-Id` | 父 Span ID | `00f067aa0ba902b7` |

#### Dubbo Attachment

| Attachment Key | 说明 | 示例值 |
|---------------|------|--------|
| `X-Trace-Id` | 全局追踪 ID | `4bf92f3577b34da6a3ce929d0e0e4736` |
| `X-Parent-Span-Id` | 父 Span ID | `00f067aa0ba902b7` |

### 与其他系统集成

#### 兼容 OpenTelemetry

如果需要与 OpenTelemetry 集成，可以使用 W3C Trace Context 标准：

```java
// traceparent header 格式
// {version}-{trace-id}-{parent-id}-{trace-flags}
String traceparent = "00-" + traceId + "-" + parentSpanId + "-01";
request.getHeaders().set("traceparent", traceparent);
```

#### 兼容 Zipkin B3

```java
request.getHeaders().set("X-B3-TraceId", traceId);
request.getHeaders().set("X-B3-SpanId", spanId);
request.getHeaders().set("X-B3-ParentSpanId", parentSpanId);
```

### 注意事项

1. **RestTemplate 使用**：必须使用 SDK 提供的 `traceRestTemplate` Bean，或者手动添加 `TraceClientInterceptor`
2. **Dubbo 版本**：支持 Dubbo 3.x 版本，使用 Dubbo 2.x 需要调整 Filter 实现
3. **线程清理**：SDK 会自动在请求完成后清理 ThreadLocal，避免内存泄漏
4. **异步线程**：必须配置 `TraceTaskDecorator` 才能在线程池中传递 traceId
5. **SPI 自动注册**：Dubbo Filter 已通过 SPI 自动注册，无需手动配置

### 故障排查

#### traceId 没有传递？

1. 检查是否使用了 `traceRestTemplate` Bean
2. 检查服务端是否引入了 SDK 并启用了自动配置
3. 查看日志中是否有 "Extracted traceId from request header" 或 "Injected traceId into request header"
4. Dubbo 场景查看日志中是否有 "[Dubbo Consumer] Injected traceId" 或 "[Dubbo Provider] Extracted traceId"

#### 异步线程中 traceId 丢失？

确保线程池配置了 `TraceTaskDecorator`：
```java
executor.setTaskDecorator(new TraceTaskDecorator());
```

#### Dubbo RPC 中 traceId 没有传递？

1. 确认 Dubbo 依赖已引入（版本 3.x）
2. 确认 SPI 配置文件存在：`META-INF/dubbo/org.apache.dubbo.rpc.Filter`
3. 检查日志中是否有 DubboTraceFilter 的相关日志
4. 确认 Consumer 和 Provider 两端都引入了 business-trace-sdk

## 配置说明

| 配置项 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| business.trace.enabled | boolean | 否 | true | 是否启用链路跟踪 |
| business.trace.service-name | String | 否 | 类名 | 服务名称，建议使用有意义的名称 |
| business.trace.elasticsearch.uri | String | 否 | http://localhost:9200 | Elasticsearch地址 |
| business.trace.elasticsearch.index | String | 否 | otel-traces | Elasticsearch索引名称 |

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