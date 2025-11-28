# 可观测性优化说明

## 📊 优化内容总结

### 1. 熔断器配置增强

#### Redis 熔断器
- **滑动窗口**：100次请求
- **失败率阈值**：50%
- **慢调用阈值**：80%（>1秒算慢调用）
- **熔断等待时间**：30秒

#### 数据库熔断器
- **滑动窗口**：60秒（时间窗口）
- **失败率阈值**：50%
- **慢调用阈值**:70%（>2秒算慢调用）
- **熔断等待时间**：60秒

#### 重试机制
- **最大重试次数**：3次
- **等待时间**：100ms
- **退避策略**：指数退避（2倍）

---

### 2. 链路追踪

#### TraceId 自动生成
每个请求自动生成唯一 TraceId，支持：
- 日志中显示：`[traceId]`
- 跨服务传递：`X-Trace-Id` 请求头

#### 日志格式
```
2025-11-28 10:30:45.123 [async-1] [a1b2c3d4e5f6] INFO  c.l.t.service.ShortUrlService - [L1 HIT] shortCode=abc, duration=0ms
```

---

### 3. 性能监控

#### 慢请求监控
- **阈值**：100ms
- **日志文件**：`logs/performance.log`
- **监控范围**：Controller + Service 层

#### 监控端点

| 端点 | 说明 |
|------|------|
| `/api/monitor/health` | 系统健康状态 |
| `/api/monitor/cache/stats` | 缓存详细统计 |
| `/api/monitor/cache/clear` | 清空本地缓存 |
| `/actuator/health` | Spring Boot健康检查 |
| `/actuator/metrics` | 所有指标 |
| `/actuator/prometheus` | Prometheus格式指标 |
| `/actuator/circuitbreakers` | 熔断器状态 |
| `/actuator/ratelimiters` | 限流器状态 |

---

### 4. 日志配置

#### 日志文件
- **应用日志**：`logs/tinyflow.log`（按天滚动，保留30天）
- **错误日志**：`logs/error.log`（单独记录，保留90天）
- **性能日志**：`logs/performance.log`（慢请求，保留7天）

#### 日志级别
- **开发环境**：DEBUG（仅控制台）
- **生产环境**：INFO（文件输出）

---

## 🚀 使用示例

### 1. 查看系统健康状态

```bash
curl http://localhost:8080/api/monitor/health
```

**响应示例**：
```json
{
  "status": "UP",
  "timestamp": 1732766400000,
  "circuitBreakers": {
    "redisBreaker": "CLOSED",
    "dbBreaker": "CLOSED"
  },
  "rateLimiters": {
    "redirectLimit": {
      "availablePermissions": 3500,
      "waitingThreads": 0
    }
  },
  "caffeineCache": {
    "hitCount": 125430,
    "missCount": 1250,
    "hitRate": "99.01%",
    "evictionCount": 50,
    "size": 1603
  }
}
```

---

### 2. 查看缓存统计

```bash
curl http://localhost:8080/api/monitor/cache/stats
```

**响应示例**：
```json
{
  "hitCount": 125430,
  "missCount": 1250,
  "hitRate": 0.9901,
  "missRate": 0.0099,
  "evictionCount": 50,
  "estimatedSize": 1603,
  "averageLoadPenalty": 15000000
}
```

---

### 3. 查看熔断器状态

```bash
curl http://localhost:8080/actuator/circuitbreakers
```

---

### 4. 查看 Prometheus 指标

```bash
curl http://localhost:8080/actuator/prometheus
```

**关键指标**：
```
# 缓存命中率
caffeine_cache_hit_total{cache="localUrlCache"} 125430
caffeine_cache_miss_total{cache="localUrlCache"} 1250

# 熔断器状态
resilience4j_circuitbreaker_state{name="redisBreaker",state="closed"} 1

# 限流器
resilience4j_ratelimiter_available_permissions{name="redirectLimit"} 3500

# HTTP 请求延迟
http_server_requests_seconds{uri="/api/redirect/{code}",status="302",quantile="0.95"} 0.008
```

---

## 📈 Grafana 监控配置

### 导入 Dashboard

1. 访问 Grafana：`http://localhost:3000`
2. 导入 Dashboard：`web/infra/observability/dashboards/shortener-overview.json`

### 关键面板

- **请求吞吐量**：实时 QPS
- **响应延迟**：P50/P95/P99
- **熔断器状态**：实时监控熔断打开/关闭
- **缓存命中率**：L1/L2 缓存效率
- **线程池监控**：异步线程池队列长度

---

## 🔍 日志查询示例

### 查看慢请求
```bash
tail -f logs/performance.log
```

### 追踪单个请求
```bash
grep "a1b2c3d4e5f6" logs/tinyflow.log
```

### 查看错误日志
```bash
tail -f logs/error.log
```

### 查看熔断器事件
```bash
grep "CircuitBreaker" logs/tinyflow.log
```

**输出示例**：
```
2025-11-28 10:35:20.456 ⚡ CircuitBreaker [redisBreaker] state changed: CLOSED → OPEN
2025-11-28 10:35:50.789 🟢 CircuitBreaker [redisBreaker] is now CLOSED. System recovered.
```

---

## 🛠️ 告警配置建议

### 关键告警指标

1. **熔断器打开**：`resilience4j_circuitbreaker_state{state="open"} == 1`
2. **错误率 >1%**：`rate(http_server_requests_seconds_count{status=~"5.."}[1m]) > 0.01`
3. **P95 延迟 >100ms**：`http_server_requests_seconds{quantile="0.95"} > 0.1`
4. **缓存命中率 <90%**：`caffeine_cache_hit_total / (caffeine_cache_hit_total + caffeine_cache_miss_total) < 0.9`

---

## 📝 优化效果

### 1. 可观测性提升
- ✅ 链路追踪（TraceId）
- ✅ 慢请求自动记录
- ✅ 熔断器状态实时监控
- ✅ 缓存命中率统计

### 2. 稳定性提升
- ✅ Redis 故障自动降级
- ✅ 数据库慢查询保护
- ✅ 重试机制避免瞬时故障
- ✅ 舱壁隔离防止资源耗尽

### 3. 故障定位效率
- ✅ 日志结构化（TraceId 关联）
- ✅ 性能日志独立存储
- ✅ 错误日志单独记录
- ✅ 监控端点实时查询

---

## 🎯 下一步优化建议

1. **接入告警平台**：钉钉/企业微信/PagerDuty
2. **分布式追踪**：集成 Zipkin/Jaeger
3. **日志中心化**：ELK/Loki
4. **自定义指标**：业务相关的监控指标

---

## 📚 相关文档

- [Resilience4j 官方文档](https://resilience4j.readme.io/)
- [Micrometer 指标体系](https://micrometer.io/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
