<div align="center">

<div style="display: inline-flex; align-items: center; justify-content: center; gap: 15px; margin-bottom: 20px;">
  <img src="web/public/logo.png" width="60" height="60" style="object-fit: contain; vertical-align: middle; position: relative; top: 18px;">
  <h1 style="margin: 0; display: inline; vertical-align: middle; line-height: 60px;">TinyFlow</h1>
</div>

<div style="margin-bottom: 20px;">
  <h2 style="margin: 0; color: #666; font-weight: normal;">- 高性能短链接服务 -</h2>
</div>

<div style="margin-bottom: 20px; text-align: center; line-height: 1.5;">
  <span style="display: inline-block; margin: 0 8px;">
    <a href="https://opensource.org/licenses/MIT" target="_blank">
      <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License: MIT">
    </a>
  </span>
  <span style="display: inline-block; margin: 0 8px;">
    <a href="https://www.oracle.com/java/" target="_blank">
      <img src="https://img.shields.io/badge/Java-17-orange.svg" alt="Java 17">
    </a>
  </span>
  <span style="display: inline-block; margin: 0 8px;">
    <a href="https://vuejs.org/" target="_blank">
      <img src="https://img.shields.io/badge/Vue-3-green.svg" alt="Vue 3">
    </a>
  </span>
  <span style="display: inline-block; margin: 0 8px;">
    <a href="https://spring.io/projects/spring-boot" target="_blank">
      <img src="https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg" alt="Spring Boot 3.5.7">
    </a>
  </span>
</div>

</div>

## 简介

TinyFlow 是一个现代化的高性能短链接生成与统计系统，支持快速生成短链接并提供详细的访问数据分析功能。

## 特性

### 核心功能
- 🚀 **极速生成**：毫秒级短链生成
- 📊 **实时统计**：详细的访问数据分析
- 🔗 **自定义短码**：支持自定义短链接后缀
- 📱 **二维码生成**：自动生成短链接二维码
- 🌐 **多语言支持**：完整的中英文国际化

### 技术特点
- 基于号段模式的ID生成算法
- 多级缓存架构（本地 + Redis）
- 异步消息队列处理
- 响应式前端设计

## 快速开始

### 环境要求
- Java 17 或更高版本
- Node.js 18 或更高版本
- MySQL 8.0
- Redis 7.0

### 本地开发

1. **克隆项目**
```bash
git clone https://github.com/Layau-code/TinyFlow.git
cd TinyFlow
```

2. **启动后端服务**
```bash
mvn spring-boot:run
```

3. **启动前端服务**
```bash
cd web
npm install
npm run dev
```

4. **访问应用**
- 前端界面：http://localhost:5173
- 后端API：http://localhost:8080

## 技术栈

### 后端
- **框架**：Spring Boot 3.5.7
- **数据库**：MySQL + JPA
- **缓存**：Redis + Caffeine
- **消息队列**：RabbitMQ
- **监控**：Spring Boot Actuator

### 前端
- **框架**：Vue 3 + Vite
- **样式**：Tailwind CSS
- **图表**：ECharts
- **国际化**：Vue I18n

## 项目结构

```
src/
├── main/java/com/layor/tinyflow/   # 后端源代码
│   ├── controller/                 # API控制器
│   ├── service/                    # 业务逻辑
│   ├── repository/                 # 数据访问
│   └── entity/                     # 数据实体
web/
├── src/                            # 前端源代码
│   ├── components/                 # Vue组件
│   ├── pages/                      # 页面组件
│   └── composables/               # 组合式函数
```

## 部署

### 生产环境构建

```bash
# 构建后端
mvn clean package -DskipTests

# 构建前端
cd web
npm install
npm run build
```

### Nginx配置示例

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    location / {
        root /var/www/tinyflow/web/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
    }
}
```

## API使用示例

### 生成短链接
```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"longUrl": "https://example.com"}'
```

### 访问短链接
```bash
curl -I http://localhost:8080/abc123
```

## 贡献

欢迎提交 Issue 和 Pull Request！请遵循以下流程：

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系我们

- 项目地址：https://github.com/Layau-code/TinyFlow
- 问题反馈：https://github.com/Layau-code/TinyFlow/issues

---

<div align="center">
如果这个项目对您有帮助，请给个 ⭐️ 支持一下！
</div>