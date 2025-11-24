# 统计模块接口文档（Enterprise）

本文档面向企业级使用场景，覆盖当前前端统计模块所需的后端接口，强调简约、现代、专业、克制的设计原则下的数据与交互需求。所有示例均与前端实现保持一致。

## 总览
- 接口数量：5 个
- 基础路径：`/api`
- 鉴权：推荐使用 `Authorization: Bearer <token>`（企业环境默认需要鉴权）
- 响应风格：直接返回业务数据对象或数组（无额外 envelope），错误通过 HTTP 状态码体现
- 时间格式：`ISO 8601` 或 `YYYY-MM-DD`（趋势按天）

常见状态码：
- `200 OK`：成功
- `400 Bad Request`：参数错误
- `401 Unauthorized`：未鉴权或令牌失效
- `403 Forbidden`：无权限访问该短码数据
- `404 Not Found`：资源不存在（短码不存在）
- `500 Internal Server Error`：服务器异常

---

## 1) 获取我的短链列表
GET `/api/urls`

用途：看板页展示当前用户所有短链数据，用于列表、搜索与选择对比。前端现阶段做客户端分页；企业场景推荐后端分页（预留参数）。

请求参数（Query，可选）：
- `q`：搜索关键字（支持按短码或长链接模糊匹配）
- `page`：页码（默认 `1`）
- `pageSize`：每页条数（默认 `10`）
- `orderBy`：排序字段（如 `totalVisits`、`createdAt`）
- `order`：排序方向（`asc`/`desc`）

响应 200（JSON 数组）：
```
[
  {
    "shortCode": "demo123",
    "longUrl": "https://example.com/landing",
    "totalVisits": 1324,
    "todayVisits": 27,
    "createdAt": "2025-10-30T08:12:34.000Z"
  },
  ...
]
```

说明：
- 列表字段与前端 `DashboardPage` 使用一致。
- 如启用后端分页，可返回 `{ items: [...], page, pageSize, total }`，前端后续可调整为服务端分页。

---

## 2) 获取短码概览
GET `/api/stats/overview/{shortCode}`

用途：统计详情页顶部卡片展示的核心指标。

路径参数：
- `shortCode`：短码标识（URL 编码）

响应 200（JSON 对象）：
```
{
  "totalVisits": 1324,
  "todayVisits": 27,
  "createdAt": "2025-10-30T08:12:34.000Z"
}
```

错误：
- `404`：短码不存在
- `403`：当前用户无权查看该短码统计

---

## 3) 获取短码趋势（按日）
GET `/api/stats/trend/{shortCode}`

用途：统计详情页折线图，默认最近 7 天。

路径参数：
- `shortCode`：短码标识（URL 编码）

请求参数（Query）：
- `days`：天数（默认 `7`，建议支持 `7/14/30/90`）

响应 200（JSON 数组）：
```
[
  { "date": "2025-10-24", "visits": 121 },
  { "date": "2025-10-25", "visits": 98 },
  ...
]
```

说明：
- `date` 采用 `YYYY-MM-DD`，便于前端横轴渲染与对齐。

---

## 4) 获取短码分布数据
GET `/api/stats/distribution/{shortCode}`

用途：设备、地域、来源等维度分布（柱状/水平条/简化饼图）。

路径参数：
- `shortCode`：短码标识（URL 编码）

响应 200（JSON 对象）：
```
{
  "device": [
    { "label": "Mobile", "value": 860 },
    { "label": "Desktop", "value": 412 }
  ],
  "city": [
    { "label": "上海", "value": 320 },
    { "label": "北京", "value": 280 },
    { "label": "杭州", "value": 210 }
  ],
  "referer": [
    { "label": "wechat", "value": 540 },
    { "label": "direct", "value": 410 },
    { "label": "weibo", "value": 220 }
  ]
}
```

说明：
- 统一使用 `{ label, value }`，便于前端组件直接消费。
- 地域分布前端默认取 Top5；后端可返回完整列表。

---

## 5) 多短码趋势对比
GET `/api/stats/compare`

用途：看板页左侧多折线对比，前端通过勾选多短码调用此接口。

请求参数（Query）：
- `trends`：短码列表（逗号分隔），如 `demo123,abC9,xYz01`
- `days`：天数（默认 `7`）

响应 200（JSON 对象）：
```
{
  "demo123": [
    { "date": "2025-10-24", "visits": 121 },
    { "date": "2025-10-25", "visits": 98 }
  ],
  "abC9": [
    { "date": "2025-10-24", "visits": 55 },
    { "date": "2025-10-25", "visits": 67 }
  ]
}
```

说明：
- 返回对象以短码为 key，value 为其趋势数组，前端将自动按日期聚合标签。

---

## 安全与性能建议
- 鉴权：所有统计接口建议要求登录态；对资源进行所有权校验。
- 速率限制：按 IP/用户维度做 rate limit，防止批量拉取导致压力。
- 缓存：趋势与分布数据可按短码+天数维度缓存 1–5 分钟，降低查询成本。
- 分页：`/api/urls` 建议启用服务端分页与排序；返回总数以便前端渲染。

## 示例（curl）
```
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/urls

curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/stats/overview/demo123

curl -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/stats/trend/demo123?days=7"

curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/stats/distribution/demo123

curl -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/stats/compare?trends=demo123,abC9&days=7"
```