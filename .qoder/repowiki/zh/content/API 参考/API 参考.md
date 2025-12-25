# API 参考

<cite>
**本文档中引用的文件**
- [AuthController.java](file://src/main/java/com/layor/tinyflow/Controller/AuthController.java)
- [ShortUrlController.java](file://src/main/java/com/layor/tinyflow/Controller/ShortUrlController.java)
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java)
- [MonitorController.java](file://src/main/java/com/layor/tinyflow/Controller/MonitorController.java)
- [Result.java](file://src/main/java/com/layor/tinyflow/entity/Result.java)
- [ShortenRequest.java](file://src/main/java/com/layor/tinyflow/entity/ShortenRequest.java)
- [ShortUrlDTO.java](file://src/main/java/com/layor/tinyflow/entity/ShortUrlDTO.java)
- [UrlListResponseDTO.java](file://src/main/java/com/layor/tinyflow/entity/UrlListResponseDTO.java)
- [AuthRequest.java](file://src/main/java/com/layor/tinyflow/entity/AuthRequest.java)
- [AuthResponse.java](file://src/main/java/com/layor/tinyflow/entity/AuthResponse.java)
- [StatsQuery.java](file://src/main/java/com/layor/tinyflow/dto/StatsQuery.java)
- [application.yml](file://src/main/resources/application.yml)
</cite>

## 目录
1. [简介](#简介)
2. [认证API](#认证api)
3. [短链API](#短链api)
4. [统计API](#统计api)
5. [监控API](#监控api)
6. [通用响应结构](#通用响应结构)
7. [使用示例](#使用示例)

## 简介
本API参考文档为TinyFlow系统的所有公共API提供了详细的说明。文档涵盖了`AuthController`、`ShortUrlController`、`StatsController`和`MonitorController`中的每个端点，包括HTTP方法、URL路径、请求头、请求体JSON Schema、响应体JSON Schema和可能的HTTP状态码。所有API均基于Spring Boot框架实现，使用JWT进行身份验证，并遵循RESTful设计原则。

**Section sources**
- [AuthController.java](file://src/main/java/com/layor/tinyflow/Controller/AuthController.java#L1-L173)
- [ShortUrlController.java](file://src/main/java/com/layor/tinyflow/Controller/ShortUrlController.java#L1-L82)
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L1-L180)
- [MonitorController.java](file://src/main/java/com/layor/tinyflow/Controller/MonitorController.java#L1-L114)

## 认证API
认证API提供用户注册、登录和获取当前用户信息的功能。所有需要身份验证的端点都需要在请求头中包含Authorization令牌。

### 用户注册
创建新用户账户。

**HTTP方法**: POST  
**URL路径**: `/api/auth/register`  
**请求头**: `Content-Type: application/json`  
**请求体**:
```json
{
  "username": "string",
  "password": "string",
  "email": "string"
}
```

**请求参数**:
- `username`: 用户名，长度3-50，只能包含字母、数字、下划线和中划线
- `password`: 密码，长度6-100
- `email`: 邮箱，必须符合邮箱格式

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "token": "string",
    "type": "Bearer",
    "username": "string",
    "role": "string",
    "expiresIn": 604800
  }
}
```

**失败响应**:
- **HTTP状态码**: 400 Bad Request
- **响应体**:
```json
{
  "code": 0,
  "message": "用户名长度必须在3-50之间, 密码长度必须在6-100之间, 邮箱格式不正确",
  "success": false,
  "data": null
}
```

**可能的HTTP状态码**:
- 200: 注册成功
- 400: 参数校验失败或邮箱为空
- 500: 服务器内部错误

**curl命令示例**:
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpass123",
    "email": "test@example.com"
  }'
```

**使用场景**: 新用户首次使用系统时，通过此API注册账户并获取JWT令牌。

**Section sources**
- [AuthController.java](file://src/main/java/com/layor/tinyflow/Controller/AuthController.java#L33-L86)
- [AuthRequest.java](file://src/main/java/com/layor/tinyflow/entity/AuthRequest.java#L1-L27)
- [AuthResponse.java](file://src/main/java/com/layor/tinyflow/entity/AuthResponse.java#L1-L43)

### 用户登录
使用现有凭据登录系统。

**HTTP方法**: POST  
**URL路径**: `/api/auth/login`  
**请求头**: `Content-Type: application/json`  
**请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```

**请求参数**:
- `username`: 用户名
- `password`: 密码

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "token": "string",
    "type": "Bearer",
    "username": "string",
    "role": "string",
    "expiresIn": 604800
  }
}
```

**失败响应**:
- **HTTP状态码**: 401 Unauthorized
- **响应体**:
```json
{
  "code": 0,
  "message": "用户名或密码错误",
  "success": false,
  "data": null
}
```

**可能的HTTP状态码**:
- 200: 登录成功
- 401: 用户名或密码错误
- 500: 服务器内部错误

**curl命令示例**:
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpass123"
  }'
```

**使用场景**: 已注册用户访问系统时，通过此API验证凭据并获取新的JWT令牌。

**Section sources**
- [AuthController.java](file://src/main/java/com/layor/tinyflow/Controller/AuthController.java#L91-L135)
- [AuthRequest.java](file://src/main/java/com/layor/tinyflow/entity/AuthRequest.java#L1-L27)
- [AuthResponse.java](file://src/main/java/com/layor/tinyflow/entity/AuthResponse.java#L1-L43)

### 获取当前用户信息
获取已认证用户的详细信息。

**HTTP方法**: GET  
**URL路径**: `/api/auth/me`  
**请求头**: `Authorization: Bearer <token>`  
**请求体**: 无

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "id": 1,
    "username": "string",
    "email": "string",
    "role": "string",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

**失败响应**:
- **HTTP状态码**: 401 Unauthorized
- **响应体**:
```json
{
  "code": 0,
  "message": "未登录",
  "success": false,
  "data": null
}
```

**可能的HTTP状态码**:
- 200: 获取用户信息成功
- 401: 未登录或令牌无效
- 500: 服务器内部错误

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/auth/me" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 前端应用需要显示当前用户信息时，通过此API获取用户详细信息。

**Section sources**
- [AuthController.java](file://src/main/java/com/layor/tinyflow/Controller/AuthController.java#L140-L172)

## 短链API
短链API提供短链接的创建、管理、重定向和列表查询功能。

### 生成短链
将长URL转换为短链接，支持自定义别名。

**HTTP方法**: POST  
**URL路径**: `/api/shorten`  
**请求头**: `Content-Type: application/json`, `Authorization: Bearer <token>`  
**请求体**:
```json
{
  "longUrl": "string",
  "customAlias": "string"
}
```

**请求参数**:
- `longUrl`: 要缩短的原始长链接，必填
- `customAlias`: 自定义别名，可选，支持中文

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "shortCode": "string",
    "shortUrl": "string",
    "longUrl": "string",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

**失败响应**:
- **HTTP状态码**: 400 Bad Request
- **响应体**: 根据具体错误情况返回相应的错误信息

**可能的HTTP状态码**:
- 200: 生成短链成功
- 400: 参数校验失败
- 500: 服务器内部错误

**curl命令示例**:
```bash
curl -X POST "http://localhost:8080/api/shorten" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "longUrl": "https://www.example.com/very/long/url/path",
    "customAlias": "mylink"
  }'
```

**使用场景**: 用户需要将长链接转换为短链接以便分享时，通过此API生成短链接。

**Section sources**
- [ShortUrlController.java](file://src/main/java/com/layor/tinyflow/Controller/ShortUrlController.java#L24-L28)
- [ShortenRequest.java](file://src/main/java/com/layor/tinyflow/entity/ShortenRequest.java#L1-L17)
- [ShortUrlDTO.java](file://src/main/java/com/layor/tinyflow/entity/ShortUrlDTO.java#L1-L16)

### 获取短链列表
获取当前用户创建的所有短链接列表。

**HTTP方法**: GET  
**URL路径**: `/api/urls`  
**请求头**: `Authorization: Bearer <token>`  
**查询参数**:
- `page`: 页码，从0开始，默认0
- `size`: 每页大小，默认20
- `sort`: 排序字段，如`createdAt,desc`

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "content": [
      {
        "shortCode": "string",
        "longUrl": "string",
        "totalVisits": 100,
        "todayVisits": 5,
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0,
    "first": true,
    "last": true,
    "empty": false
  }
}
```

**可能的HTTP状态码**:
- 200: 获取列表成功
- 401: 未登录或令牌无效
- 500: 服务器内部错误

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/urls?page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户需要查看自己创建的所有短链接及其统计信息时，通过此API获取分页列表。

**Section sources**
- [ShortUrlController.java](file://src/main/java/com/layor/tinyflow/Controller/ShortUrlController.java#L46-L68)
- [UrlListResponseDTO.java](file://src/main/java/com/layor/tinyflow/entity/UrlListResponseDTO.java#L1-L17)
- [PageResponseDTO.java](file://src/main/java/com/layor/tinyflow/entity/PageResponseDTO.java#L1-L23)

### 修改短链
更新现有短链接的信息。

**HTTP方法**: PUT  
**URL路径**: `/api/{shortCode}`  
**请求头**: `Content-Type: application/json`, `Authorization: Bearer <token>`  
**路径参数**:
- `shortCode`: 要修改的短链接码

**请求体**:
```json
{
  "longUrl": "string",
  "customAlias": "string"
}
```

**请求参数**:
- `longUrl`: 新的长链接，可选
- `customAlias`: 新的自定义别名，可选

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**: 无

**失败响应**:
- **HTTP状态码**: 400 Bad Request
- **响应体**: 根据具体错误情况返回相应的错误信息

**可能的HTTP状态码**:
- 200: 修改成功
- 400: 参数校验失败
- 500: 服务器内部错误

**curl命令示例**:
```bash
curl -X PUT "http://localhost:8080/api/abc123" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "customAlias": "newalias"
  }'
```

**使用场景**: 用户需要更新短链接的目标URL或别名时，通过此API进行修改。

**Section sources**
- [ShortUrlController.java](file://src/main/java/com/layor/tinyflow/Controller/ShortUrlController.java#L35-L38)

### 删除短链
删除指定的短链接。

**HTTP方法**: DELETE  
**URL路径**: `/api/{shortCode}`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**:
- `shortCode`: 要删除的短链接码

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**: 无

**失败响应**:
- **HTTP状态码**: 400 Bad Request
- **响应体**: 根据具体错误情况返回相应的错误信息

**可能的HTTP状态码**:
- 200: 删除成功
- 400: 参数校验失败
- 500: 服务器内部错误

**curl命令示例**:
```bash
curl -X DELETE "http://localhost:8080/api/abc123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户不再需要某个短链接时，通过此API将其删除。

**Section sources**
- [ShortUrlController.java](file://src/main/java/com/layor/tinyflow/Controller/ShortUrlController.java#L39-L43)

### 短链重定向
将短链接码重定向到原始长链接。

**HTTP方法**: GET  
**URL路径**: `/{shortCode}`  
**请求头**: 无  
**路径参数**:
- `shortCode`: 短链接码

**成功响应**:
- **HTTP状态码**: 302 Found
- **响应头**: `Location: <longUrl>`

**失败响应**:
- **HTTP状态码**: 404 Not Found
- **响应体**: 无

**可能的HTTP状态码**:
- 302: 重定向到原始URL
- 404: 短链接不存在

**curl命令示例**:
```bash
curl -I "http://localhost:8080/abc123"
```

**使用场景**: 用户访问短链接时，服务器自动重定向到原始长链接。

**Section sources**
- [ShortUrlController.java](file://src/main/java/com/layor/tinyflow/Controller/ShortUrlController.java#L30-L33)

## 统计API
统计API提供短链接的访问统计、趋势分析和详细数据查询功能。

### 获取短链接概览
获取指定短链接的基本统计信息。

**HTTP方法**: GET  
**URL路径**: `/api/stats/overview/{shortCode}`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**:
- `shortCode`: 短链接码

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "shortCode": "string",
  "totalClicks": 100,
  "uniqueVisitors": 50,
  "createdAt": "2024-01-01T00:00:00",
  "lastClickedAt": "2024-01-02T00:00:00"
}
```

**可能的HTTP状态码**:
- 200: 获取成功
- 401: 未登录或令牌无效
- 404: 短链接不存在

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/stats/overview/abc123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户需要快速查看某个短链接的总体访问情况时，通过此API获取概览信息。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L20-L25)

### 获取访问趋势
获取指定短链接的每日访问趋势。

**HTTP方法**: GET  
**URL路径**: `/api/stats/trend/{shortCode}`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**:
- `shortCode`: 短链接码

**查询参数**:
- `days`: 天数，默认7天

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
[
  {
    "date": "2024-01-01",
    "clicks": 10,
    "uniqueVisitors": 5
  }
]
```

**可能的HTTP状态码**:
- 200: 获取成功
- 401: 未登录或令牌无效
- 404: 短链接不存在

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/stats/trend/abc123?days=30" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户需要分析短链接的访问趋势时，通过此API获取每日访问数据。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L26-L33)

### 比较多个短链接趋势
比较多个短链接的访问趋势。

**HTTP方法**: GET  
**URL路径**: `/api/stats/compare`  
**请求头**: `Authorization: Bearer <token>`  
**查询参数**:
- `trends`: 短链接码列表，用逗号分隔
- `days`: 天数，默认7天

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "abc123": [
    {
      "date": "2024-01-01",
      "clicks": 10,
      "uniqueVisitors": 5
    }
  ],
  "def456": [
    {
      "date": "2024-01-01",
      "clicks": 15,
      "uniqueVisitors": 8
    }
  ]
}
```

**可能的HTTP状态码**:
- 200: 获取成功
- 400: 短码列表为空
- 401: 未登录或令牌无效

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/stats/compare?trends=abc123,def456&days=14" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户需要比较多个短链接的性能时，通过此API获取并比较它们的访问趋势。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L35-L44)

### 获取访问分布
获取指定短链接的访问分布统计信息。

**HTTP方法**: POST  
**URL路径**: `/api/stats/distribution`  
**请求头**: `Content-Type: application/json`, `Authorization: Bearer <token>`  
**请求体**:
```json
{
  "code": "string",
  "start": "string",
  "end": "string",
  "source": "string",
  "device": "string",
  "city": "string",
  "page": 0,
  "size": 20
}
```

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "browserDistribution": [
    {
      "key": "Chrome",
      "count": 60
    }
  ],
  "osDistribution": [
    {
      "key": "Windows",
      "count": 70
    }
  ],
  "deviceDistribution": [
    {
      "key": "Desktop",
      "count": 80
    }
  ],
  "countryDistribution": [
    {
      "key": "China",
      "count": 90
    }
  ],
  "cityDistribution": [
    {
      "key": "Beijing",
      "count": 40
    }
  ],
  "refererDistribution": [
    {
      "key": "https://google.com",
      "count": 50
    }
  ]
}
```

**可能的HTTP状态码**:
- 200: 获取成功
- 400: 参数校验失败
- 401: 未登录或令牌无效

**curl命令示例**:
```bash
curl -X POST "http://localhost:8080/api/stats/distribution" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "code": "abc123",
    "start": "2024-01-01",
    "end": "2024-01-31"
  }'
```

**使用场景**: 用户需要深入了解短链接访问者的特征时，通过此API获取详细的分布统计。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L52-L57)
- [StatsQuery.java](file://src/main/java/com/layor/tinyflow/dto/StatsQuery.java#L1-L16)

### 获取点击事件详情
获取指定短链接的点击事件详情列表。

**HTTP方法**: POST  
**URL路径**: `/api/stats/events`  
**请求头**: `Content-Type: application/json`, `Authorization: Bearer <token>`  
**请求体**: 同`/api/stats/distribution`的请求体

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
[
  {
    "id": 1,
    "shortCode": "string",
    "ip": "string",
    "country": "string",
    "city": "string",
    "region": "string",
    "isp": "string",
    "os": "string",
    "browser": "string",
    "device": "string",
    "referer": "string",
    "userAgent": "string",
    "createdAt": "2024-01-01T00:00:00"
  }
]
```

**可能的HTTP状态码**:
- 200: 获取成功
- 400: 参数校验失败
- 401: 未登录或令牌无效

**curl命令示例**:
```bash
curl -X POST "http://localhost:8080/api/stats/events" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "code": "abc123",
    "start": "2024-01-01",
    "end": "2024-01-31",
    "page": 0,
    "size": 10
  }'
```

**使用场景**: 用户需要查看具体的点击事件时，通过此API获取详细的访问记录。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L65-L70)

### 导出统计数据
将指定短链接的统计数据导出为CSV或JSON格式。

**HTTP方法**: POST  
**URL路径**: `/api/stats/export`  
**请求头**: `Content-Type: application/json`, `Authorization: Bearer <token>`  
**请求体**: 同`/api/stats/distribution`的请求体  
**查询参数**:
- `format`: 导出格式，`csv`或`json`，默认`csv`

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应头**: 
  - `Content-Type`: `text/csv` 或 `application/json`
  - `Content-Disposition`: `attachment; filename=stats-abc123.csv`
- **响应体**: 导出的文件内容

**可能的HTTP状态码**:
- 200: 导出成功
- 400: 参数校验失败
- 401: 未登录或令牌无效

**curl命令示例**:
```bash
curl -X POST "http://localhost:8080/api/stats/export?format=csv" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "code": "abc123",
    "start": "2024-01-01",
    "end": "2024-01-31"
  }' -o stats-abc123.csv
```

**使用场景**: 用户需要将统计数据导出到本地进行进一步分析时，通过此API下载CSV或JSON文件。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L73-L83)

### 获取详细统计数据
获取指定短链接的详细统计数据（包含所有维度）。

**HTTP方法**: GET  
**URL路径**: `/api/stats/detailed/{shortCode}`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**:
- `shortCode`: 短链接码

**查询参数**:
- `start`: 开始日期，格式`yyyy-MM-dd`
- `end`: 结束日期，格式`yyyy-MM-dd`

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**: 包含所有统计维度的详细数据对象

**可能的HTTP状态码**:
- 200: 获取成功
- 401: 未登录或令牌无效
- 404: 短链接不存在

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/stats/detailed/abc123?start=2024-01-01&end=2024-01-31" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户需要全面了解某个短链接的所有统计信息时，通过此API获取详细数据。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L88-L95)

### 获取全局统计数据
获取系统的全局统计数据。

**HTTP方法**: GET  
**URL路径**: `/api/stats/global`  
**请求头**: `Authorization: Bearer <token>`  
**查询参数**:
- `start`: 开始日期
- `end`: 结束日期

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**: 包含系统级统计信息的对象

**可能的HTTP状态码**:
- 200: 获取成功
- 401: 未登录或令牌无效

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/stats/global?start=2024-01-01&end=2024-01-31" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 管理员需要了解系统整体使用情况时，通过此API获取全局统计。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L100-L106)

### 获取小时分布
获取指定短链接的小时访问分布。

**HTTP方法**: GET  
**URL路径**: `/api/stats/hour/{shortCode}`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**:
- `shortCode`: 短链接码

**查询参数**:
- `start`: 开始日期
- `end`: 结束日期

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
[
  {
    "key": "00",
    "count": 5
  },
  {
    "key": "01",
    "count": 3
  }
]
```

**可能的HTTP状态码**:
- 200: 获取成功
- 401: 未登录或令牌无效
- 404: 短链接不存在

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/stats/hour/abc123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户需要分析访问时间模式时，通过此API获取小时级别的分布数据。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L111-L118)

### 获取星期分布
获取指定短链接的星期访问分布。

**HTTP方法**: GET  
**URL路径**: `/api/stats/weekday/{shortCode}`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**:
- `shortCode`: 短链接码

**查询参数**:
- `start`: 开始日期
- `end`: 结束日期

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
[
  {
    "key": "Monday",
    "count": 20
  }
]
```

**可能的HTTP状态码**:
- 200: 获取成功
- 401: 未登录或令牌无效
- 404: 短链接不存在

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/stats/weekday/abc123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户需要了解访问的星期模式时，通过此API获取星期分布数据。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L123-L130)

### 获取浏览器分布
获取指定短链接的浏览器访问分布。

**HTTP方法**: GET  
**URL路径**: `/api/stats/browser/{shortCode}`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**:
- `shortCode`: 短链接码

**查询参数**:
- `start`: 开始日期
- `end`: 结束日期

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
[
  {
    "key": "Chrome",
    "count": 60
  }
]
```

**可能的HTTP状态码**:
- 200: 获取成功
- 401: 未登录或令牌无效
- 404: 短链接不存在

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/stats/browser/abc123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户需要了解访问者的浏览器使用情况时，通过此API获取浏览器分布数据。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L135-L142)

### 获取国家分布
获取指定短链接的国家访问分布。

**HTTP方法**: GET  
**URL路径**: `/api/stats/country/{shortCode}`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**:
- `shortCode`: 短链接码

**查询参数**:
- `start`: 开始日期
- `end`: 结束日期

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
[
  {
    "key": "China",
    "count": 90
  }
]
```

**可能的HTTP状态码**:
- 200: 获取成功
- 401: 未登录或令牌无效
- 404: 短链接不存在

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/stats/country/abc123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户需要了解访问者的地理分布时，通过此API获取国家分布数据。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L147-L154)

### 获取Referer分布
获取指定短链接的Referer详细分布。

**HTTP方法**: GET  
**URL路径**: `/api/stats/referer/{shortCode}`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**:
- `shortCode`: 短链接码

**查询参数**:
- `start`: 开始日期
- `end`: 结束日期

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
[
  {
    "key": "https://google.com",
    "count": 50
  }
]
```

**可能的HTTP状态码**:
- 200: 获取成功
- 401: 未登录或令牌无效
- 404: 短链接不存在

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/stats/referer/abc123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户需要了解流量来源时，通过此API获取Referer分布数据。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L159-L166)

### 获取PV/UV数据
获取指定短链接的PV（页面浏览量）和UV（独立访客）数据。

**HTTP方法**: GET  
**URL路径**: `/api/stats/pvuv/{shortCode}`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**:
- `shortCode`: 短链接码

**查询参数**:
- `start`: 开始日期
- `end`: 结束日期

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "pv": 100,
  "uv": 50
}
```

**可能的HTTP状态码**:
- 200: 获取成功
- 401: 未登录或令牌无效
- 404: 短链接不存在

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/stats/pvuv/abc123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**使用场景**: 用户需要精确了解访问量和独立访客数时，通过此API获取PV/UV数据。

**Section sources**
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L171-L178)

## 监控API
监控API提供系统健康状态、缓存统计和管理功能。

### 获取系统健康状态
获取系统的健康状态信息，包括熔断器、限流器和缓存状态。

**HTTP方法**: GET  
**URL路径**: `/api/monitor/health`  
**请求头**: 无  
**请求体**: 无

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "status": "UP",
  "timestamp": 1700000000000,
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
    "hitCount": 1000,
    "missCount": 100,
    "hitRate": "90.91%",
    "evictionCount": 50,
    "size": 5000
  }
}
```

**可能的HTTP状态码**:
- 200: 获取成功

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/monitor/health"
```

**使用场景**: 运维人员需要检查系统健康状况时，通过此API获取全面的健康信息。

**Section sources**
- [MonitorController.java](file://src/main/java/com/layor/tinyflow/Controller/MonitorController.java#L37-L75)

### 获取缓存统计
获取本地缓存的详细统计信息。

**HTTP方法**: GET  
**URL路径**: `/api/monitor/cache/stats`  
**请求头**: 无  
**请求体**: 无

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "hitCount": 1000,
  "missCount": 100,
  "loadSuccessCount": 100,
  "loadFailureCount": 0,
  "totalLoadTime": 1000000,
  "evictionCount": 50,
  "evictionWeight": 50,
  "hitRate": 0.9090909090909091,
  "missRate": 0.09090909090909091,
  "averageLoadPenalty": 10000,
  "estimatedSize": 5000
}
```

**可能的HTTP状态码**:
- 200: 获取成功

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/monitor/cache/stats"
```

**使用场景**: 开发人员需要分析缓存性能时，通过此API获取详细的缓存统计。

**Section sources**
- [MonitorController.java](file://src/main/java/com/layor/tinyflow/Controller/MonitorController.java#L80-L96)

### 清空本地缓存
清空本地缓存中的所有条目。

**HTTP方法**: GET  
**URL路径**: `/api/monitor/cache/clear`  
**请求头**: 无  
**请求体**: 无

**成功响应**:
- **HTTP状态码**: 200 OK
- **响应体**:
```json
{
  "success": true,
  "removedEntries": 5000,
  "message": "Cache cleared successfully"
}
```

**可能的HTTP状态码**:
- 200: 清空成功

**curl命令示例**:
```bash
curl -X GET "http://localhost:8080/api/monitor/cache/clear"
```

**使用场景**: 系统维护或调试时，需要清空缓存以确保数据一致性。

**Section sources**
- [MonitorController.java](file://src/main/java/com/layor/tinyflow/Controller/MonitorController.java#L101-L112)

## 通用响应结构
所有API响应都遵循统一的响应结构，便于客户端处理。

### Result 响应包装器
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {}
}
```

**字段说明**:
- `code`: 响应码，0表示成功
- `message`: 响应消息
- `success`: 是否成功
- `data`: 响应数据，具体结构根据API而定

**Section sources**
- [Result.java](file://src/main/java/com/layor/tinyflow/entity/Result.java#L1-L34)

## 使用示例
以下是一些常见的使用场景示例：

### 创建短链接并获取统计
```bash
# 1. 登录获取令牌
TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}' \
  | jq -r '.data.token')

# 2. 创建短链接
SHORT_URL=$(curl -s -X POST "http://localhost:8080/api/shorten" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"longUrl":"https://www.example.com","customAlias":"mylink"}' \
  | jq -r '.data.shortUrl')

# 3. 获取统计信息
curl -s -X GET "http://localhost:8080/api/stats/overview/mylink" \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.'
```

### 批量比较短链接性能
```bash
# 比较多个短链接的7天趋势
curl -s -X GET "http://localhost:8080/api/stats/compare?trends=link1,link2,link3&days=7" \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.'
```

### 导出统计数据进行分析
```bash
# 导出特定时间段的统计数据
curl -s -X POST "http://localhost:8080/api/stats/export?format=csv" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"code":"mylink","start":"2024-01-01","end":"2024-01-31"}' \
  -o mylink_stats.csv
```

**Section sources**
- [AuthController.java](file://src/main/java/com/layor/tinyflow/Controller/AuthController.java#L91-L135)
- [ShortUrlController.java](file://src/main/java/com/layor/tinyflow/Controller/ShortUrlController.java#L24-L28)
- [StatsController.java](file://src/main/java/com/layor/tinyflow/Controller/StatsController.java#L35-L44)