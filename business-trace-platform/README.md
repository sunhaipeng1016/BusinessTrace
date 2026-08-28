# Business Trace Platform

轻量级业务链路跟踪可视化平台，用于查看和分析通过Business Trace SDK收集的方法调用链路数据。

## 特性

- **轻量级**：基于Spring Boot + Thymeleaf，无需前后端分离
- **实时刷新**：支持自动刷新（30秒），实时查看最新链路数据
- **多维度筛选**：支持按服务名称、方法名、Trace ID、时间范围筛选
- **完整展示**：展示链路的时间、服务、方法、入参、出参、错误信息、耗时
- **开箱即用**：直接运行即可使用，无需复杂配置

## 快速开始

### 1. 构建

```bash
mvn clean package -DskipTests
```

### 2. 运行

```bash
java -jar business-trace-platform-0.0.3-SNAPSHOT.jar
```

### 3. 访问

打开浏览器访问：http://localhost:8088

## 配置

在`application.properties`中配置：

```properties
# Elasticsearch配置
spring.elasticsearch.uris=http://localhost:9200

# 索引名称（需与SDK配置一致，默认：otel-traces）
business.trace.elasticsearch.index=otel-traces

# 服务端口（默认8088）
server.port=8088
```

## 功能说明

### 链路列表

- 展示所有链路记录，包含Trace ID、时间、服务、方法、入参、出参、错误信息、耗时、状态
- 支持按服务名称、方法名、Trace ID筛选
- 支持按时间范围筛选（最近N小时）
- 支持自动刷新（30秒）

### 数据展示

| 字段 | 说明 |
|------|------|
| Trace ID | 链路追踪唯一标识 |
| 时间 | 链路开始时间 |
| 服务 | 服务名称 |
| 方法 | 方法名称（类名.方法名） |
| 入参 | 方法调用参数（JSON格式） |
| 出参 | 方法返回结果（JSON格式） |
| 错误信息 | 异常信息（如有） |
| 耗时(ms) | 方法执行耗时（毫秒） |
| 状态 | 成功/失败 |

## 技术栈

- **后端**: Spring Boot 2.5.15, Java 1.8
- **前端**: Thymeleaf, Bootstrap 5
- **存储**: Elasticsearch
- **构建**: Maven

## 与SDK配合使用

1. 在业务项目中引入`business-trace-sdk`
2. 配置SDK的Elasticsearch地址和索引
3. 运行`business-trace-platform`，配置相同的Elasticsearch地址和索引
4. 在业务项目中使用`@TraceMethod`注解需要跟踪的方法
5. 访问Platform页面查看链路数据

## License

Apache License 2.0