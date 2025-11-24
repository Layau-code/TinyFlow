# 短链核心服务（设计与接口文档）

本文档描述“短链核心服务”的最小可用功能（MVP）、接口规范、缓存与限流策略、数据模型与演进方向，便于面试展示与团队协作。

## 目标
- 面向高并发的短链创建与解析（重定向）。
- 提供稳定、可观测、可扩展的基础能力：缓存、限流、过期控制与错误模型。
- 前端可直接对接，或通过 OpenAPI 导入调试。

## 术语
- 长链（Long URL）：原始目标链接。
- 短码（Code）：短链标识，通常为 6–8 位 Base62 字符。
- 短链（Short URL）：`/r/{code}`，访问时 302 重定向到长链。

---

## API 概览
- 创建短链：`POST /api/links`
- 查询短链：`GET /api/links/{code}`
- 解析重定向：`GET /r/{code}`（302 跳转）
- 健康检查：`GET /health`（可选）

### 1) 创建短链
- 方法：`POST /api/links`
- 请求体（JSON）：
```json
{
  "longUrl": "https://www.example.com/landing?ref=abc",
  "alias": "mycode",          // 可选，自定义短码；若冲突返回 409
  "ttlDays": 7                  // 可选，到期天数（自然日）；不填表示不过期
}
```
- 成功响应 200：
```json
{
  "code": "mycode",
  "longUrl": "https://www.example.com/landing?ref=abc",
  "shortUrl": "/r/mycode"
}
```
- 失败响应：
  - 400：参数非法（空 URL、URL 非法、ttlDays 非法等）
  - 409：`alias` 冲突（短码已存在）
  - 500：服务器内部错误

建议校验
- `longUrl` 必填，协议需为 `http/https`；最大长度 2048。
- `alias` 仅允许 `[A-Za-z0-9_-]`，长度 3–64；区分场景需要时设为大小写敏感。
- `ttlDays` 为正整数；到期后解析应返回 404。

可选增强
- 幂等：客户端可携带 `Idempotency-Key` 请求头，服务端以（Key + Body Hash）保证幂等创建。

### 2) 查询短链
- 方法：`GET /api/links/{code}`
- 成功响应 200：
```json
{ "code": "abc123", "longUrl": "https://..." }
```
- 失败响应：
  - 404：短码不存在或已过期

### 3) 解析重定向
- 方法：`GET /r/{code}`
- 行为：返回 302（或 301），`Location` 指向长链。
- 失败：404（不存在/过期），429（超过限流）。

---

## 错误模型（建议）
```json
{
  "error": "ALIAS_CONFLICT",
  "message": "Alias already exists",
  "traceId": "..."  // 可选，便于排障
}
```
常见错误码：`VALIDATION_FAILED`、`ALIAS_CONFLICT`、`NOT_FOUND`、`RATE_LIMITED`、`INTERNAL_ERROR`。

---

## 缓存策略
- 读缓存：
  - 本地内存缓存（例如 Caffeine）：`code -> longUrl`，提升 P95 延迟并抗抖动。
  - 分布式缓存（Redis，可选）：同样缓存 `code -> longUrl`，与 TTL 对齐；穿透时回源数据库。
- 写路径：
  - 创建成功后写数据库；同时刷新/写入缓存（双写或延时双删选其一）。
- 过期：
  - 若配置 `ttlDays`，过期后应从缓存与解析中剔除；查询返回 404。

---

## 限流策略
- 解析接口 `/r/{code}` 建议对来源 IP 做令牌桶限流（如每分钟 60 次）。
- 过载保护：返回 429，配合 `Retry-After` 响应头（可选）。
- 管理端可查看限流命中统计（可观测性）。

---

## 数据模型
- 表：`links`
  - `id`（自增或雪花）
  - `code`（唯一索引）
  - `long_url`（文本，最大 2048）
  - `created_at`（UTC）
  - `expires_at`（UTC，可空）

短码生成
- 自定义别名优先；否则生成 6–7 位 Base62，遇冲突随机重试再加长。

---

## 可观测性（建议）
- 指标：请求 QPS、P95 延迟、缓存命中率、Redis 失败数、限流命中、DB 慢查询。
- 日志：结构化 JSON，包含 `traceId` 与关键字段（code、status、costMs）。
- 追踪：OpenTelemetry（HTTP 入站与数据库、Redis 调用链）。

---

## 安全与风控（基础建议）
- 输入校验：严格校验 URL，限制私网地址（SSRF 风险）。
- 速率限制：按 IP/Token；黑白名单；对异常 UA 与爬虫做规则过滤。
- HTTPS：生产强制 HTTPS 与 HSTS。

---

## 本地联调（前端）
- 开发代理建议：
  - `/api` → `http://localhost:8080`
  - `/r` → `http://localhost:8080`
- 跨域：若不用代理，则需在后端开启 CORS（仅开发）。

---

## Postgres 是否更好？
- 结论：是的，推荐 Postgres 作为 OLTP 主库。
- 理由：
  - 更强的标准兼容性与约束能力（唯一索引、事务一致性、`jsonb` 类型）。
  - 优秀的查询优化器，复杂查询与索引策略更成熟。
  - 社区活跃、云服务完善，配套生态丰富。
- 对统计大吞吐场景：建议后续引入 ClickHouse 存储与查询聚合；OLTP 与 OLAP 分离。

---

## 后续演进
- 幂等创建（Idempotency-Key）
- 密码保护、一次性短链、到期策略（精确到分钟/秒）
- A/B 与定向策略（设备/地区/时段）
- 事件采集 → 消息队列 → 列式存储（ClickHouse） → 维度统计与趋势对比
- 开放平台：API Key、配额与统计报表导出

---

## 附：OpenAPI 规范
- 见 `docs/openapi-shortener.yaml`，可导入 Swagger UI / Postman / Apifox 调试。