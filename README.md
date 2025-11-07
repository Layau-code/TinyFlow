# TinyFlow

一款现代化的短链接生成与统计系统，前端基于 Vue 3 + Vite + Tailwind CSS，后端推荐使用 Spring Boot（可替换为任意语言栈）。UI 参考多款优秀开源项目与主流网站的交互与视觉风格，提供极简流畅的使用体验。

> 当前前端已统一使用后端重定向接口：`GET /api/redirect/{code}` 进行跳转与二维码生成。

---

## 目录

- 概览
- 功能特性
- 技术栈与架构
- 项目结构
- 快速开始
- 配置说明
- 开发与调试
- 常见问题
- 后续拓展计划
- 贡献指南
- 许可证

---

## 概览

TinyFlow 旨在提供一个轻量、可靠的短链接平台：

- 生成可自定义别名的短链接
- 一键复制与二维码生成
- 历史记录管理（分页、筛选、编辑、删除）
- 数据统计页面（点击趋势、来源分布等，接口规划中）

---

## 功能特性

- 现代化 UI 与交互：极简卡片化布局、响应式适配、细致的悬停与动效
- 统一跳转端点：前端所有点击、复制、二维码均使用 `GET /api/redirect/{code}`
- 历史记录：支持分页刷新、筛选、编辑别名与删除条目
- 多语言支持：内置 i18n，可在导航栏切换语言
- 开发体验：Vite 快速热更新、结构化代码与组件拆分

---

## 技术栈与架构

- 前端：
  - `Vue 3`（Composition API）
  - `Vite 5`
  - `Tailwind CSS`
  - `Axios`

- 后端（推荐方案，亦可替换）：
  - `Spring Boot`
  - `Spring Data JPA`
  - MySQL，Redis 

- 运行拓扑（开发环境）：
  - 前端 dev server：`http://localhost:5173`（或 5174/5175，取决于端口占用）
  - 后端 API：`http://localhost:8080`
  - 前端通过 `vite.config.js` 代理 `/api` 到后端

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

## 快速开始

### 前置条件

- Node.js `>= 18`
- 推荐安装包管理器：`npm` 或 `pnpm`
- 后端服务（示例：Spring Boot）运行在 `http://localhost:8080`

### 安装与启动（前端）

```bash
git clone <your-repo-url>
cd TinyFlow/web
npm install
npm run dev
```

浏览器打开提示的本地地址（如 `http://localhost:5173/`）。

### 构建生产包

```bash
npm run build
```

产物输出到 `web/dist` 目录。

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
## 后续拓展计划（Roadmap）

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

## 许可证

本项目采用 MIT License。
