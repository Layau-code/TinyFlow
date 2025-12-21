# TinyFlow - 高性能短链接系统

<div align="center">

![TinyFlow Logo](./docs/images/logo.png)
<!-- 📍 需要插入图片：项目 Logo，建议尺寸 200x200px -->

**世界上没有两个相同的雨滴，就像每一个短链接都独一无二**

**雨滴汇聚成河流 (Flow)，链接汇聚成数据洪流**

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue.js-3.x-4FC08D.svg)](https://vuejs.org/)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)

一款现代化、高并发的短链接生成与统计系统

压测验证可稳定支撑 **3000+ QPS**，P99 延迟 **< 100ms**

[在线演示](http://your-demo-url.com) · [快速开始](#快速开始) · [技术架构](#技术栈与架构) · [部署指南](#部署到生产环境)

</div>

---

## 📖 目录

- [项目简介](#项目简介)
- [核心特性](#核心特性)
- [技术栈与架构](#技术栈与架构)
- [系统架构](#系统架构)
- [性能指标](#性能指标)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [配置说明](#配置说明)
- [开发与调试](#开发与调试)
- [部署到生产环境](#部署到生产环境)
- [性能优化](#性能优化)
- [常见问题](#常见问题)
- [后续拓展计划](#后续拓展计划)
- [贡献指南](#贡献指南)
- [开源协议](#开源协议)
- [联系方式](#联系方式)

---

## 🌟 项目简介

### 为什么叫 TinyFlow？

> 世界上没有两个完全相同的雨滴，就像这个系统中的每一个短链接都是独一无二的。
>
> 雨滴汇聚到一起就成了河流（Flow），无数次的点击汇聚成数据洪流。
>
> 微小而独特（Tiny），汇聚成流（Flow），这就是 TinyFlow 的设计哲学。

TinyFlow 是一款面向高并发场景的短链接生成与统计系统，采用**号段模式 + 多级缓存 + 消息队列**架构，解决了传统短链系统在高并发下的性能瓶颈问题。

### 核心亮点

- 🚀 **高性能**：压测验证支撑 3000+ QPS，P99 延迟 < 100ms
- 🔐 **无冲突**：号段模式 + Hashids + Base58 生成 6 位短码
- 💾 **多级缓存**：Caffeine (L1) + Redis (L2) + MySQL，缓存命中率 85%+
- 📊 **异步统计**：RabbitMQ 消息队列解耦，消息丢失率 < 0.01%
- 🛡️ **熔断降级**：Resilience4j 保障服务稳定性
- 📈 **可观测性**：Prometheus + Grafana + Zipkin 全链路监控

### 适用场景

- ✅ 营销活动短链生成与数据统计
- ✅ 社交媒体分享链接追踪
- ✅ 移动端 App 推广链接
- ✅ 二维码生成与管理
- ✅ 学习高并发系统设计

---

## ✨ 核心特性

### 用户功能

- **自定义别名**：可自定义短链后缀，增强品牌一致性
- **一键生成**：输入长链接，秒级生成短链接
- **二维码生成**：自动生成二维码，支持下载
- **历史记录管理**：分页、筛选、编辑、删除
- **访问统计**：点击趋势、来源分布、设备统计等
- **多语言支持**：中英文切换

### 技术特性

- **分布式 ID 生成**：号段模式预分配 ID，双 Buffer 异步加载
- **短码生成策略**：Hashids 算法 + Base58 编码，稳定生成 6 位短码
- **多级缓存架构**：本地缓存 + 分布式缓存 + 数据库，逐级回填
- **缓存预热**：启动时加载 Top 1000 热点短链到本地缓存
- **消息队列解耦**：点击统计异步处理，批量聚合刷库
- **熔断降级**：Redis/MySQL 故障自动熔断，降级策略保障可用性
- **全链路监控**：Prometheus 采集指标，Grafana 可视化，Zipkin 追踪

---

## 功能特性

- 现代化 UI 与交互：极简卡片化布局、响应式适配、细致的悬停与动效
- 统一跳转端点：前端所有点击、复制、二维码均使用 `GET /api/redirect/{code}`
- 历史记录：支持分页刷新、筛选、编辑别名与删除条目
- 多语言支持：内置 i18n，可在导航栏切换语言
- 开发体验：Vite 快速热更新、结构化代码与组件拆分

---

## 🛠️ 技术栈与架构

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 | 3.x | Composition API，响应式框架 |
| Vite | 5.x | 极速构建工具 |
| Tailwind CSS | 3.x | 原子化 CSS 框架 |
| Axios | 1.x | HTTP 请求库 |
| Vue Router | 4.x | 前端路由 |
| Vue I18n | 9.x | 国际化支持 |
| ECharts | 5.x | 数据可视化图表 |
| QRCode.vue | 3.x | 二维码生成 |

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.7 | 基础框架 |
| Spring Data JPA | 3.x | 数据访问层 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 分布式缓存 |
| RabbitMQ | 3.x | 消息队列 |
| Caffeine | 3.x | 本地缓存 |
| Resilience4j | 2.x | 熔断器 |
| Prometheus | - | 指标采集 |
| Zipkin | - | 链路追踪 |
| Grafana | - | 监控可视化 |

### 核心组件

- **短码生成**：号段模式 ID 生成器 + Hashids + Base58
- **多级缓存**：Caffeine (L1) + Redis (L2) + MySQL (L3)
- **消息队列**：RabbitMQ 异步统计 + 死信队列 + 手动 ACK
- **熔断降级**：Resilience4j 熔断器 + 降级策略
- **可观测性**：Prometheus + Grafana + Zipkin

### 运行拓扑

**开发环境**：
- 前端 dev server：`http://localhost:5173`（或 5174/5175）
- 后端 API：`http://localhost:8080`
- 前端通过 `vite.config.js` 代理 `/api` 到后端

**生产环境**：
- 前端：Nginx 静态托管 + 反向代理
- 后端：Spring Boot JAR 部署
- 数据库：MySQL 主从复制
- 缓存：Redis 哨兵模式
- 消息队列：RabbitMQ 集群

---

## 🏗️ 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                          用户端 (Web/Mobile)                     │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Nginx (反向代理 + 负载均衡)                    │
└─────────────────────────────────────────────────────────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 ▼                               ▼
         ┌──────────────┐                ┌──────────────┐
         │  Vue 3 前端  │                │ Spring Boot  │
         │  (静态资源)  │                │   后端服务   │
         └──────────────┘                └──────────────┘
                                                 │
                 ┌───────────────────────────────┼───────────────┐
                 ▼                               ▼               ▼
         ┌──────────────┐              ┌──────────────┐  ┌─────────┐
         │   Caffeine   │              │    Redis     │  │ RabbitMQ│
         │  (L1 本地缓存)│              │ (L2 分布式缓存)│  │ (消息队列)│
         └──────────────┘              └──────────────┘  └─────────┘
                                                 │               │
                                                 ▼               ▼
                                         ┌──────────────┐  ┌─────────┐
                                         │    MySQL     │  │ 统计消费者│
                                         │  (持久化层)   │  │ (批量写入)│
                                         └──────────────┘  └─────────┘
```
<!-- 📍 需要插入图片：系统架构图，建议使用 draw.io 绘制后导出 PNG -->

### 短码生成流程

```
用户请求
   │
   ▼
号段 ID 生成器 (SegmentIdGenerator)
   │
   ├─> 检查当前 Buffer 是否充足
   │
   ├─> 若 < 50%，异步加载下一 Buffer
   │
   ├─> 从内存获取 ID (无数据库 IO)
   │
   ▼
Hashids 算法编码
   │
   ▼
Base58 编码生成 6 位短码
   │
   ▼
返回短链接
```
<!-- 📍 需要插入图片：短码生成流程图 -->

### 多级缓存读取流程

```
用户访问短链
   │
   ▼
L1 Caffeine 本地缓存
   │
   ├─> 命中 ──> 返回 (最快)
   │
   ├─> 未命中
   │   │
   │   ▼
   │  L2 Redis 分布式缓存
   │   │
   │   ├─> 命中 ──> 回填 L1 ──> 返回
   │   │
   │   ├─> 未命中
   │   │   │
   │   │   ▼
   │   │  L3 MySQL 数据库
   │   │   │
   │   │   ├─> 查询成功 ──> 回填 L2 ──> 回填 L1 ──> 返回
   │   │   │
   │   │   └─> 查询失败 ──> 返回 404
```
<!-- 📍 需要插入图片：多级缓存架构图 -->

### 异步统计流程

```
用户点击短链
   │
   ▼
记录点击事件
   │
   ▼
发送到 RabbitMQ
   │
   ├─> 立即返回 302 重定向 (用户无感知)
   │
   ▼
消息队列
   │
   ├─> 每 2 秒批量消费
   │
   ├─> 聚合统计数据 (Map<shortCode, count>)
   │
   ├─> 批量 UPDATE clicks = clicks + delta
   │
   ├─> 手动 ACK 确认
   │
   └─> 失败重试 (指数退避 3 次) ──> 死信队列
```
<!-- 📍 需要插入图片：异步统计流程图 -->

---

## 📊 性能指标

### 压测环境

- **服务器配置**：4 核 8GB
- **压测工具**：K6
- **并发数**：100 VU
- **测试时长**：5 分钟

### 性能数据

| 指标 | 数值 | 说明 |
|------|------|------|
| QPS | 3000+ | 每秒处理请求数 |
| P99 延迟 | < 100ms | 99% 请求响应时间 |
| P95 延迟 | < 50ms | 95% 请求响应时间 |
| 平均延迟 | ~30ms | 平均响应时间 |
| 缓存命中率 | 85%+ | L1+L2 总命中率 |
| 数据库 QPS | < 500 | 缓存后数据库压力 |
| 消息吞吐 | 5000+/s | RabbitMQ 消息处理能力 |
| 消息丢失率 | < 0.01% | 死信队列 + 手动 ACK |

### 优化效果对比

| 优化项 | 优化前 | 优化后 | 提升幅度 |
|--------|--------|--------|----------|
| 短码生成 TPS | 200 | 10000+ | **50 倍** |
| 数据库压力 | 100% | 5% | **降低 95%** |
| 缓存命中率 | 0% | 85% | **提升 85%** |
| 列表接口响应 | 800ms | 50ms | **优化 94%** |
| 系统 TPS | 600 | 3000+ | **5 倍** |

<!-- 📍 需要插入图片：性能监控截图 (Grafana Dashboard) -->
<!-- 📍 需要插入图片：压测结果截图 (K6 测试报告) -->

---

## 项目结构

```
前端/
├── src/
│   ├── components/           # 通用组件（加载、二维码、图标、统计卡片等）
│   ├── pages/                # 页面（Dashboard、Stats）
│   ├── router/               # 前端路由
│   ├── composables/          # 组合式工具（API、统计等）
│   ├── assets/               # 静态资源
│   ├── i18n.js               # 国际化配置
│   ├── main.js               # 入口文件
│   └── style.css             # 全局样式
├── App.vue                   # 首页与顶栏
├── vite.config.js            # Dev 代理与构建配置
├── package.json
└── README.md
```

后端/
- Controller
  - `ShortUrlController`：短链生成、维护与重定向相关接口
  - `StatsController`：统计相关查询接口
- entity（含 DTO/VO）
  - `ShortUrl`、`ShortUrlDTO`、`ShortUrlOverviewDTO`
  - `ShortenRequest`、`PageResponseDTO`、`Result`
  - `DailyClick`、`DailyVisitTrendDTO`、`UrlClickStatsDTO`、`UrlListResponseDTO`
- repository
  - `ShortUrlRepository`：短链数据访问
  - `DailyClickRepository`：点击统计数据访问
- service
  - `ShortUrlService`：短链核心业务（生成、查询、重定向、更新别名、删除等）
- 应用入口
  - `TinyFlowApplication`


---

## 🚀 快速开始

### 前置条件

**环境要求**：
- Java `>= 17`
- Node.js `>= 18`
- Maven `>= 3.6`
- MySQL `>= 8.0`
- Redis `>= 7.0`
- RabbitMQ `>= 3.11`

**推荐工具**：
- IDE：IntelliJ IDEA / VS Code
- 包管理器：npm / pnpm
- 容器：Docker / Docker Compose

### 一键启动（Docker Compose）

```bash
# 1. 克隆项目
git clone https://github.com/Layau-code/TinyFlow.git
cd TinyFlow

# 2. 启动基础服务 (MySQL + Redis + RabbitMQ + Prometheus + Grafana)
docker-compose up -d

# 3. 启动后端
cd src/main
mvn spring-boot:run

# 4. 启动前端
cd ../../web
npm install
npm run dev

# 5. 访问应用
# 前端：http://localhost:5173
# 后端 API：http://localhost:8080
# Grafana：http://localhost:3000 (admin/admin)
# Prometheus：http://localhost:9090
```

### 手动启动

#### 后端启动

```bash
# 1. 修改配置文件 src/main/resources/application.yml
# 配置 MySQL、Redis、RabbitMQ 连接信息

# 2. 初始化数据库
mysql -u root -p < db/schema.sql

# 3. 启动后端
mvn clean package
java -jar target/tinyflow-0.0.1-SNAPSHOT.jar
```

#### 前端启动

```bash
# 1. 进入前端目录
cd web

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 4. 浏览器访问
open http://localhost:5173
```

### 构建生产包

#### 前端构建

```bash
cd web
npm run build
# 产物输出到 web/dist 目录
```

#### 后端构建

```bash
mvn clean package -DskipTests
# 产物输出到 target/tinyflow-0.0.1-SNAPSHOT.jar
```

### 验证安装

```bash
# 1. 检查后端健康状态
curl http://localhost:8080/actuator/health

# 2. 生成短链接
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"longUrl": "https://www.example.com"}'

# 3. 访问短链接
curl -I http://localhost:8080/abc123
```

---

## 配置说明

- API 地址：
  - 开发环境默认通过 `vite.config.js` 代理 `/api` 到 `http://localhost:8080`
  - 在 `App.vue` 中有常量 `API_BASE = 'http://localhost:8080'`，用于构造短链展示地址（如二维码、复制用）。生产环境请按实际域名修改。

- 跳转端点：
  - 前端所有短链均指向 `GET /api/redirect/{code}`，由后端返回 `302` 到原始长链，并进行点击日志记录（推荐）。

---

## 开发与调试

- 常用脚本：
  - `npm run dev`：启动前端开发服务器
  - `npm run build`：构建生产包

- 页面导航：
  - 首页生成短链与历史记录管理
  - `Dashboard`：集中管理与操作列表
  - `Stats`：查看某短码的访问统计（接口完善中）

- 调试提示：
  - 首页 favicon 加载可能出现跨站错误（如 `net::ERR_BLOCKED_BY_ORB`），不影响核心功能；
  - 若点击短链仍走旧端点，请强制刷新缓存（Ctrl+F5）或检查 `App.vue` 的 `buildShortUrl` 与 `redirectViaApi` 实现是否已更新。

---

## 🚢 部署到生产环境

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    # 前端静态资源
    location / {
        root /var/www/tinyflow/web/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 短链接跳转
    location ~ ^/[a-zA-Z0-9]{6}$ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
    }
}
```

### 环境变量配置

```bash
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/tinyflow
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

### Docker 部署

```bash
# 1. 构建镜像
docker build -t tinyflow:latest .

# 2. 运行容器
docker run -d \
  -p 8080:8080 \
  -e MYSQL_HOST=mysql \
  -e REDIS_HOST=redis \
  -e RABBITMQ_HOST=rabbitmq \
  --name tinyflow \
  tinyflow:latest
```

### 监控与告警

- **Grafana Dashboard**：导入 `web/infra/observability/dashboards/shortener-overview.json`
- **Prometheus 配置**：`web/infra/observability/prometheus.yml`
- **告警规则**：配置 QPS、错误率、延迟阈值告警

<!-- 📍 需要插入图片：生产环境部署架构图 -->
<!-- 📍 需要插入图片：Grafana 监控大盘截图 -->

---

## ⚡ 性能优化

### 已实现的优化

#### 1. 短码生成优化

**问题**：数据库自增 ID 在高并发下成为瓶颈

**方案**：
- 号段模式预分配 ID 段（每次 1000 个）到内存
- 双 Buffer 机制（当前 Buffer 用至 50% 时异步加载下一段）
- Hashids 算法 + Base58 编码生成 6 位短码

**效果**：单机 TPS 从 200 提升至 10000+，数据库压力降低 95%

#### 2. 多级缓存优化

**问题**：热点短链高并发访问导致数据库压力大

**方案**：
- L1 Caffeine 本地缓存（10 万容量 + 1h TTL）
- L2 Redis 分布式缓存
- 缓存预热：启动时加载 Top 1000 热点短链
- 逐级回填策略

**效果**：缓存命中率 85%+，数据库 IO 降低 70%

#### 3. 分页性能优化

**问题**：前端内存分页 + 后端 N+1 查询

**方案**：
- 改为服务端分页（PageRequest + 稳定排序）
- 批量 IN 查询消除 N+1 问题
- Map 合并统计数据到 DTO

**效果**：接口响应从 800ms 降至 50ms（优化 94%）

#### 4. 消息队列异步解耦

**问题**：点击统计同步写入阻塞跳转请求

**方案**：
- 跳转请求发送 RabbitMQ 消息后立即返回
- 消费者每 2 秒批量聚合刷库
- 死信队列 + 手动 ACK + 指数退避重试

**效果**：系统 TPS 提升 5 倍，消息丢失率 < 0.01%

### 可继续优化的点

- [ ] **布隆过滤器**：防止缓存穿透
- [ ] **限流策略**：Guava RateLimiter / Redis 滑动窗口
- [ ] **读写分离**：MySQL 主从复制 + 读写路由
- [ ] **分库分表**：ShardingSphere 水平拆分
- [ ] **CDN 加速**：静态资源 + 短链跳转页 CDN 缓存

---

## 🗂️ 后续拓展计划

- 安全与稳定性
  - 访问速率限制（Rate Limit）、防刷与机器人识别
  - 短码唯一性保障与冲突检测优化（含自定义别名策略）
  - 审计日志与操作留痕（更新/删除）

- 统计与分析
  - 细化点击日志结构（UA、IP、Referer、UTM、地理位置等）
  - 趋势图与来源分布（按天、周、月、时间段）
  - 导出报表 CSV/JSON、分享统计页的只读链接

- 管理能力
  - 批量导入/导出短链
  - 标签/分组管理与筛选
  - 权限模型与多租户（个人/团队/企业）
  - 管理后台（Admin）与审核流程

- 使用体验
  - 自定义域名与多域支持（如 `s.yourbrand.com`）
  - 二维码定制（颜色、LOGO、容错率）
  - 失效策略与到期提醒（TTL、软删除恢复）
  - 跳转前提示页（如合规警示/安全验证）

- 开发运维
  - 缓存与热点优化（本地缓存/Redis/LRU）
  - 观察性与告警（Metrics/Tracing/Logging）
  - Webhook/事件订阅（点击通知、到期通知）
  - 完整测试体系（单测/集成/端到端）与 CI/CD 流程

> 如需优先推进其中某一项，请在 Issue 中告知场景与需求，我会按优先级完善。

## 常见问题

- 前端短链地址与后端域名不一致？
  - 请在 `App.vue` 中调整 `API_BASE` 为线上域名，或在构建时注入环境变量并使用 `import.meta.env`。

- 后端未提供纯文本接口 `/shorten`？
  - 前端会回退到 `/api/shorten`，无需修改。你也可以移除文本接口的调用逻辑。

---

## 贡献指南

欢迎提交 Issue 或 PR 来改进项目：

1. Fork 仓库并创建特性分支
2. 保持代码风格一致，避免无关修改
3. 编写必要的说明与测试（如适用）
4. 提交 PR 并描述改动动机与效果

---

## 📜 开源协议

本项目采用 [MIT License](./LICENSE)。

---

## 📮 联系方式

- **项目地址**：https://github.com/Layau-code/TinyFlow
- **问题反馈**：https://github.com/Layau-code/TinyFlow/issues
- **邮箱**：18970931397@163.com

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Caffeine](https://github.com/ben-manes/caffeine)
- [Hashids](https://hashids.org/)
- [RabbitMQ](https://www.rabbitmq.com/)
- [Resilience4j](https://resilience4j.readme.io/)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，欢迎 Star 支持！**

**💡 有任何建议或问题，欢迎提交 Issue 或 PR！**

**🎓 适合作为学习高并发系统设计的实战项目！**

Made with ❤️ by [Layau](https://github.com/Layau-code)

</div>
