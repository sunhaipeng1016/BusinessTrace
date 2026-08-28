# Business Trace

轻量级业务链路跟踪解决方案，包含SDK和可视化平台。

## 项目介绍

Business Trace 是一个轻量级的业务链路跟踪工具，旨在帮助开发者快速定位和排查分布式系统中的方法调用问题。通过简单的注解配置，即可自动采集方法调用的入参、出参、错误信息和耗时，并提供可视化平台进行查看和分析。

## 项目结构

```
BusinessTrace/
├── business-trace-sdk/          # SDK - 链路采集与上报
│   └── README.md
├── business-trace-platform/     # Platform - 可视化平台
│   └── README.md
└── pom.xml
```

## 快速开始

### 1. 在业务项目中引入SDK

```xml
<dependency>
    <groupId>com.github.sunhaipeng</groupId>
    <artifactId>business-trace-sdk</artifactId>
    <version>0.0.3-SNAPSHOT</version>
</dependency>
```

### 2. 配置

```yaml
business:
  trace:
    enabled: true
    service-name: your-service-name
    elasticsearch:
      uri: http://localhost:9200
      index: otel-traces
```

### 3. 使用注解

```java
@TraceMethod
public Order createOrder(OrderRequest request) {
    // 业务逻辑
    return order;
}
```

### 4. 运行可视化平台

```bash
cd business-trace-platform
mvn clean package -DskipTests
java -jar target/business-trace-platform-0.0.3-SNAPSHOT.jar
```

访问 http://localhost:8080 查看链路数据。

## 特性

### SDK
- **零侵入**：只需添加`@TraceMethod`注解即可自动跟踪方法调用
- **轻量级**：无外部依赖，仅依赖Spring Boot和AspectJ
- **跨版本兼容**：支持Spring Boot 2.x和3.x，Java 1.8+
- **自动采集**：自动记录方法入参、出参、错误信息和执行耗时
- **链路追踪**：支持跨方法调用链追踪，自动生成TraceId
- **自定义上下文**：支持通过`TraceContext`添加自定义业务信息

### Platform
- **轻量级**：基于Spring Boot + Thymeleaf，无需前后端分离
- **实时刷新**：支持自动刷新（30秒），实时查看最新链路数据
- **多维度筛选**：支持按服务名称、方法名、Trace ID、时间范围筛选
- **完整展示**：展示链路的时间、服务、方法、入参、出参、错误信息、耗时
- **开箱即用**：直接运行即可使用，无需复杂配置

## 技术栈

- **SDK**: Spring Boot 2.x/3.x, AspectJ, Java 1.8+
- **Platform**: Spring Boot 2.5.15, Thymeleaf, Bootstrap 5, Java 1.8
- **存储**: Elasticsearch

## 文档

- [SDK文档](business-trace-sdk/README.md)
- [Platform文档](business-trace-platform/README.md)

## License

Apache License 2.0