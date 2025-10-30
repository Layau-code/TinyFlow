# TinyFlow - 短链接生成器

一个基于 Vue 3 和 Tailwind CSS 的现代化短链接生成器，采用极简主义设计风格。

## 功能特性

- 🎨 **极简设计** - 参考 Vercel、Linear 和 Apple 官网的设计风格
- 📱 **响应式布局** - 完美适配桌面和移动设备
- ⚡ **Vue 3 Composition API** - 使用最新的 Vue 3 技术栈
- 🎯 **实时验证** - 输入时即时验证 URL 格式
- 📋 **一键复制** - 支持复制生成的短链接
- 📱 **二维码生成** - 自动生成短链接二维码
- 🔄 **粘贴检测** - 自动检测粘贴的链接格式
- 💫 **微交互动画** - 流畅的用户交互体验

## 技术栈

- **Vue 3** - 使用 Composition API 和 `<script setup>` 语法
- **Tailwind CSS** - 通过 CDN 引入，无需构建过程
- **Axios** - HTTP 客户端用于 API 调用
- **Google Charts API** - 生成二维码

## 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd tiny-flow/web
```

### 2. 安装依赖
```bash
npm install
```

### 3. 启动开发服务器
```bash
npm run dev
```

项目将在 `http://localhost:3000` 启动并自动打开浏览器。

### 4. 直接打开 HTML 文件
由于项目使用 CDN 引入依赖，你也可以直接在浏览器中打开 `index.html` 文件。

## API 接口

项目需要后端提供以下 API 接口：

### 生成短链接
```
POST /api/v1/shorten
Content-Type: application/json

{
  "url": "https://example.com/very-long-url"
}
```

**成功响应：**
```json
{
  "shortUrl": "https://yourdomain.com/Abc123"
}
```

**错误响应：**
- `400` - 链接格式无效
- `409` - 短码已被占用
- `500` - 服务器内部错误

## 项目结构

```
web/
├── index.html          # 主页面文件
├── App.vue            # Vue 单文件组件（可选）
├── package.json       # 项目配置
└── README.md         # 项目说明
```

## 设计特色

### 配色方案
- **主色调：** 蓝色 (#3b82f6)
- **错误色：** 红色 (#ef4444)
- **文字色：** 深灰色系
- **背景色：** 浅灰色 (#f9fafb)

### 交互动画
- 按钮 hover 时轻微缩放效果
- 输入框聚焦时的阴影效果
- 加载状态的旋转动画
- 复制成功的状态反馈

### 响应式设计
- 桌面端：最大宽度 600px 居中布局
- 移动端：全宽度适配，优化触摸交互
- 按钮在小屏幕上垂直排列

## 浏览器兼容性

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## 开发说明

### 修改样式
项目使用 Tailwind CSS，可以直接在 HTML 中修改 class 来调整样式。

### 修改 API 地址
在 `index.html` 中的 `generateShortUrl` 函数中修改 API 端点。

### 自定义配色
在 `tailwind.config` 中修改颜色配置。

## 许可证

MIT License