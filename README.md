# TinyFlow - 高性能短链接服务

一个面向高并发场景的短链接生成与管理平台，支持长链接压缩为6位短码或自定义别名，提供链接收藏夹与数据统计分析功能，便于用户集中管理常用网址。

## 技术栈

**后端**：Spring Boot 3、MySQL 8、Redis、RabbitMQ、Caffeine、Resilience4j、Micrometer + Prometheus

**前端**：Vue 3、Vite、Tailwind CSS

**运维**：Docker Compose、Grafana、K6 压测

---

## 目录

- [核心特性](#核心特性)
- [系统架构](#系统架构)
- [技术亮点详解](#技术亮点详解)
  - [三级缓存架构与防穿透策略](#1-三级缓存架构与防穿透策略)
  - [高并发分布式ID生成](#2-高并发分布式id生成)
  - [异步统计与消息解耦](#3-异步统计与消息解耦)
  - [熔断限流与服务降级](#4-熔断限流与服务降级)
- [性能指标](#性能指标)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [压测与监控](#压测与监控)
- [API 文档](#api-文档)
- [许可证](#许可证)

---

## 核心特性

| 特性 | 描述 |
|------|------|
| **短码生成** | 支持6位短码自动生成（Hashids编码）或用户自定义别名 |
| **三级缓存** | Caffeine（L1）→ Redis（L2）→ MySQL（L3），热点链接毫秒级响应 |
| **缓存防护** | 布隆过滤器拦截无效短码请求，防止缓存穿透 |
| **分布式ID** | 参考美团Leaf号段模式，双Buffer异步预加载，避免ID段切换阻塞 |
| **异步统计** | 跳转成功后立即返回302，点击日志通过RabbitMQ异步写入，核心链路不阻塞 |
| **流量防护** | Resilience4j实现限流（滑动窗口3500 QPS）+ Redis/MySQL独立熔断器 |
| **可观测性** | Prometheus指标暴露 + Grafana可视化 + 结构化日志 |
| **数据统计** | 点击趋势、来源分布、设备占比、地域分析，支持CSV/JSON导出 |

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Client Request                                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Spring Boot Application                              │
│  ┌─────────────┐    ┌─────────────────────────────────────────────────────┐ │
│  │ Rate Limiter│───▶│                   Controller Layer                   │ │
│  │  (3500 QPS) │    │         ShortUrlController / StatsController         │ │
│  └─────────────┘    └─────────────────────────────────────────────────────┘ │
│                                      │                                       │
│                                      ▼                                       │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                           Service Layer                                  │ │
│  │  ┌───────────────────┐  ┌───────────────────┐  ┌─────────────────────┐  │ │
│  │  │  ShortUrlService  │  │ SegmentIdGenerator│  │ ClickRecorderService│  │ │
│  │  │  (Core Business)  │  │   (Leaf-Style ID) │  │  (Async Statistics) │  │ │
│  │  └───────────────────┘  └───────────────────┘  └─────────────────────┘  │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
│                                      │                                       │
└──────────────────────────────────────┼───────────────────────────────────────┘
                                       │
          ┌────────────────────────────┼────────────────────────────┐
          │                            │                            │
          ▼                            ▼                            ▼
┌─────────────────┐        ┌─────────────────┐          ┌─────────────────┐
│   L1: Caffeine  │        │   L2: Redis     │          │   L3: MySQL     │
│   (Local Cache) │◀──────▶│ (Distributed)   │◀────────▶│  (Persistence)  │
│   50,000 entries│        │ + BloomFilter   │          │                 │
│   TTL: 30min    │        │ + CircuitBreaker│          │ + CircuitBreaker│
└─────────────────┘        └─────────────────┘          └─────────────────┘
                                   │
                                   ▼
                          ┌─────────────────┐
                          │    RabbitMQ     │
                          │ (Click Events)  │
                          └─────────────────┘
                                   │
                                   ▼
                          ┌─────────────────┐
                          │ Click Analytics │
                          │  (Batch Write)  │
                          └─────────────────┘
```

---

## 技术亮点详解

### 1. 三级缓存架构与防穿透策略

**问题背景**：短链跳转是典型的读多写少场景，热点链接可能承受极高QPS。同时需要防止恶意请求不存在的短码导致缓存穿透打穿数据库。

**解决方案**：

```
请求 → BloomFilter判断 → L1 Caffeine → L2 Redis → L3 MySQL
         ↓ 不存在                ↓ 未命中      ↓ 未命中     ↓ 查询
      快速返回404           查L2并回填L1   查L3并回填L1/L2  返回结果
```

- **L1 本地缓存（Caffeine）**：单机50,000条热点短链，30分钟过期，命中率>95%时跳过网络开销
- **L2 分布式缓存（Redis）**：全量短链缓存，支持集群部署数据共享，24小时TTL
- **L3 持久存储（MySQL）**：兜底数据源，回源时自动回填L1/L2
- **布隆过滤器**：基于Redisson的分布式布隆过滤器，预期1000万短码，1%误判率，拦截无效请求

**缓存一致性策略**：
- 写操作：先更新DB → 删除Redis缓存 → 失效本地缓存（Cache Aside模式）
- 热点预热：应用启动时加载Top 5000热点短链到L1，减少冷启动延迟

### 2. 高并发分布式ID生成

**问题背景**：传统自增ID在分布式部署下存在锁竞争和短码可遍历问题；UUID太长且无序。

**解决方案**：参考美团Leaf号段模式设计

```
┌────────────────────────────────────────────────────────────────┐
│                    SegmentIdGenerator                          │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Buffer (Per bizTag)                        │   │
│  │   ┌───────────────┐      ┌───────────────┐              │   │
│  │   │ currentRange  │      │  nextRange    │              │   │
│  │   │ [10001-110000]│      │[110001-210000]│ ← 异步预加载 │   │
│  │   └───────────────┘      └───────────────┘              │   │
│  └─────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │   id_segment  │  (MySQL)
                    │ biz_tag | max_id | step
                    │ shorturl| 210000 | 100000
                    └───────────────┘
```

**核心机制**：
- **号段预取**：每次从DB申请10万个ID，减少DB访问频率
- **双Buffer异步加载**：当currentRange使用率>70%时，异步线程提前加载nextRange
- **数据库行锁**：`UPDATE id_segment SET max_id = max_id + step WHERE biz_tag = ?`，原子操作保证多实例不冲突
- **Hashids编码**：将长整型ID编码为6位短字符串（a-zA-Z0-9），支持自定义盐值防止枚举

**性能**：本地号段模式，ID获取O(1)时间复杂度，**单机支撑百万级QPS无压力**

### 3. 异步统计与消息解耦

**问题背景**：跳转接口需要记录点击日志（IP、UA、来源、设备类型等），同步写入会严重拖慢响应时间。

**解决方案**：跳转与统计链路分离

```
┌──────────────┐     302跳转      ┌──────────────┐
│   用户请求   │ ──────────────▶  │    客户端    │
└──────────────┘                  └──────────────┘
       │
       │  异步发送MQ消息
       ▼
┌──────────────┐    消费&批量写入   ┌──────────────┐
│   RabbitMQ   │ ────────────────▶ │ ClickEvent表 │
│ (click.queue)│                   │   (MySQL)    │
└──────────────┘                   └──────────────┘
```

**技术实现**：
- **即时响应**：跳转接口返回302后，异步发送点击事件到RabbitMQ
- **消息格式**：`{shortCode, ip, ua, referer, deviceType, timestamp}`
- **批量消费**：消费者每2秒批量拉取消息，聚合后批量INSERT（减少DB压力）
- **采样控制**：高流量场景可配置采样率（如10%），平衡统计精度与存储成本
- **死信队列**：消费失败的消息进入DLQ，支持人工排查与重试

**效果**：跳转接口P99延迟降低60%+，统计与核心链路完全解耦

### 4. 熔断限流与服务降级

**问题背景**：Redis/MySQL故障时需要快速失败，避免雪崩；突发流量需要限流保护。

**解决方案**：基于Resilience4j的多维度防护

```yaml
resilience4j:
  ratelimiter:
    instances:
      redirectLimit:
        limitForPeriod: 3500      # 每周期允许请求数
        limitRefreshPeriod: 1s    # 滑动窗口周期
        timeoutDuration: 0        # 超限立即拒绝

  circuitbreaker:
    instances:
      redisBreaker:
        slidingWindowSize: 10          # 滑动窗口大小
        failureRateThreshold: 50       # 失败率阈值50%
        waitDurationInOpenState: 5s    # 熔断后等待时间
        permittedNumberOfCallsInHalfOpenState: 3  # 半开状态探测请求数
        
      mysqlBreaker:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

**降级策略**：
| 故障场景 | 降级行为 |
|---------|---------|
| Redis熔断 | 跳过L2，直接查询L3 MySQL |
| MySQL熔断 | 返回L1/L2缓存数据，无缓存则返回503 |
| 限流触发 | 返回429 Too Many Requests |
| RabbitMQ故障 | 本地内存队列暂存，定时重试 |

---

## 性能指标

基于K6压测，双实例部署（4C8G × 2），MySQL 8.0，Redis 7.0

| 接口 | QPS | P50 | P95 | P99 | 错误率 |
|------|-----|-----|-----|-----|--------|
| 跳转（缓存命中） | 5,200+ | 12ms | 28ms | 45ms | <0.01% |
| 跳转（缓存穿透） | 2,800+ | 35ms | 85ms | 120ms | <0.1% |
| 短链创建 | 1,500+ | 25ms | 65ms | 95ms | <0.05% |

**调优关键点**：
1. HikariCP连接池：`maximumPoolSize=100`，`connectionTimeout=3s`
2. Caffeine本地缓存50K条目，命中率>92%
3. Redis Lettuce连接池：`maxActive=600`，`maxIdle=200`
4. Tomcat线程池：`maxThreads=600`，`acceptCount=2000`

---

## 项目结构

```
TinyFlow/
├── src/main/java/com/layor/tinyflow/
│   ├── config/                    # 配置类
│   │   ├── CacheConfig.java       # Caffeine + Redis缓存配置
│   │   ├── RabbitConfig.java      # RabbitMQ队列配置
│   │   ├── BloomFilterConfig.java # 布隆过滤器配置
│   │   └── HashidsConfig.java     # 短码编码配置
│   ├── controller/
│   │   ├── ShortUrlController.java    # 短链CRUD + 跳转
│   │   └── StatsController.java       # 统计查询
│   ├── service/
│   │   ├── ShortUrlService.java       # 核心业务逻辑
│   │   ├── SegmentIdGenerator.java    # 号段模式ID生成器
│   │   ├── ClickRecorderService.java  # 点击统计服务
│   │   └── ClickEventConsumer.java    # MQ消费者
│   ├── strategy/
│   │   └── HashidsStrategy.java       # 短码编码策略
│   ├── entity/                    # JPA实体
│   ├── dto/                       # 数据传输对象
│   └── repository/                # 数据访问层
├── src/main/resources/
│   └── application.yml            # 应用配置
├── web/                           # Vue 3 前端
│   ├── src/
│   │   ├── components/            # 通用组件
│   │   ├── pages/                 # 页面
│   │   └── composables/           # 组合式API
│   └── infra/
│       ├── load/k6/               # K6压测脚本
│       └── observability/         # Prometheus + Grafana配置
├── docker-compose.yml             # 本地开发环境
└── pom.xml
```

---

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.12+（可选，不配置则使用本地异步模式）
- Node.js 18+（前端）

### 1. 启动基础设施

```bash
docker compose up -d mysql redis rabbitmq
```

### 2. 初始化数据库

```sql
CREATE DATABASE `tiny_flow` DEFAULT CHARACTER SET utf8mb4;

-- 号段表
CREATE TABLE `id_segment` (
  `biz_tag` VARCHAR(64) PRIMARY KEY,
  `max_id` BIGINT NOT NULL DEFAULT 1,
  `step` INT NOT NULL DEFAULT 100000,
  `version` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO `id_segment` (`biz_tag`, `max_id`, `step`) VALUES ('shorturl', 1, 100000);
```

### 3. 启动后端

```bash
cd TinyFlow
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd web
npm install
npm run dev
```

访问 `http://localhost:5173`

---

## 配置说明

核心配置项（`application.yml`）：

```yaml
# 缓存配置
cache:
  caffeine:
    spec: maximumSize=50000,expireAfterWrite=30m,recordStats
  warmup:
    enabled: true
    size: 5000  # 启动时预热Top N热点链接

# 布隆过滤器
bloom:
  expected-insertions: 10000000  # 预期数据量
  false-positive-rate: 0.01     # 误判率

# 点击统计
clicks:
  mode: rabbitmq  # 可选: local（本地异步）, rabbitmq（消息队列）
events:
  sampleRate: 1.0  # 采样率，1.0=100%记录

# 熔断限流
resilience4j:
  ratelimiter:
    instances:
      redirectLimit:
        limitForPeriod: 3500
        limitRefreshPeriod: 1s
```

---

## 压测与监控

### 运行压测

```bash
# 启动观测栈
docker compose -f web/infra/observability/docker-compose.yml up -d

# 运行K6压测
k6 run web/infra/load/k6/shortener.js
```

### Grafana Dashboard

导入 `web/infra/observability/dashboards/shortener-overview.json`，可视化：
- 跳转接口 QPS / P95 / P99
- 缓存命中率（L1/L2）
- 熔断器状态
- RabbitMQ队列深度

---

## API 文档

| Method | Endpoint | 描述 |
|--------|----------|------|
| POST | `/api/shorten` | 创建短链（支持自定义别名） |
| GET | `/{shortCode}` | 短链跳转（302重定向） |
| GET | `/api/urls` | 分页查询短链列表 |
| PUT | `/api/{shortCode}` | 更新短链别名 |
| DELETE | `/api/{shortCode}` | 删除短链 |
| GET | `/api/stats/{shortCode}/overview` | 统计概览 |
| GET | `/api/stats/{shortCode}/trend` | 点击趋势 |
| GET | `/api/stats/{shortCode}/distribution` | 来源/设备分布 |

---

## 许可证

MIT License
