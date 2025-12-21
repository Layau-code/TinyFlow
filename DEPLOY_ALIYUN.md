# TinyFlow 阿里云服务器部署完整指南

> 适用于阿里云 ECS 免费服务器（Ubuntu 22.04）  
> 作者：Layau  
> 更新时间：2024-12-21

---

## 📋 目录

- [前置准备](#前置准备)
- [快速部署（一键脚本）](#快速部署一键脚本)
- [手动部署（详细步骤）](#手动部署详细步骤)
- [备案说明](#备案说明)
- [域名配置](#域名配置)
- [HTTPS 配置](#https-配置)
- [性能优化](#性能优化)
- [监控与运维](#监控与运维)
- [常见问题](#常见问题)

---

## 🎯 前置准备

### 1. 阿里云服务器要求

**推荐配置**：
- **CPU**：2核（最低1核）
- **内存**：2GB（最低1GB，但会影响性能）
- **磁盘**：40GB
- **带宽**：1Mbps（建议3Mbps以上）
- **系统**：Ubuntu 22.04 LTS

**如何申请免费服务器**：
1. 访问阿里云官网：https://www.aliyun.com/
2. 搜索"云服务器 ECS 免费试用"
3. 选择"学生优惠"或"新用户免费试用"
4. 选择 Ubuntu 22.04 镜像
5. 记住你设置的 root 密码和服务器公网 IP

### 2. 本地准备

**必需软件**：
- SSH 客户端（Windows 自带 / PuTTY / MobaXterm）
- Git Bash（如果要用 Git 上传代码）
- FTP 客户端（可选，用于传输文件）

**需要记录的信息**：
- ✅ 服务器公网 IP：`___________________`
- ✅ root 密码：`___________________`
- ✅ 准备设置的 MySQL 密码：`___________________`
- ✅ 准备设置的 Redis 密码（可选）：`___________________`

---

## 🚀 快速部署（一键脚本）

### 方式一：直接在服务器上运行（推荐）

```bash
# 1. SSH 连接到服务器
ssh root@你的服务器IP
# 输入密码

# 2. 下载部署脚本
wget https://raw.githubusercontent.com/Layau-code/TinyFlow/main/deploy.sh

# 如果 wget 不可用，使用 curl
curl -O https://raw.githubusercontent.com/Layau-code/TinyFlow/main/deploy.sh

# 3. 赋予执行权限
chmod +x deploy.sh

# 4. 运行脚本
./deploy.sh
```

### 方式二：从本地上传脚本

```powershell
# 在本地 PowerShell 中（Windows）
scp d:\tiny-flow\TinyFlow\deploy.sh root@你的服务器IP:/root/

# 然后 SSH 到服务器
ssh root@你的服务器IP

# 运行脚本
chmod +x /root/deploy.sh
/root/deploy.sh
```

### 脚本运行过程

脚本会依次询问你：

1. **MySQL root 密码**（两次确认）
   ```
   请输入 MySQL root 密码（将创建新密码）: ********
   请再次输入 MySQL root 密码: ********
   ```
   建议密码格式：`MySecure@Password123`

2. **TinyFlow 数据库密码**
   ```
   请输入 TinyFlow 数据库用户密码: ********
   ```

3. **Redis 密码**（如果没有直接回车）
   ```
   请输入 Redis 密码（如果没有直接回车）: 
   ```

4. **确认服务器 IP**
   ```
   检测到服务器 IP: 47.97.110.128
   请确认或修改服务器 IP/域名 [47.97.110.128]: 
   ```
   直接回车确认即可

然后等待 **10-15 分钟**，脚本会自动完成所有部署工作。

### 部署完成标志

看到以下输出说明部署成功：

```
╔════════════════════════════════════════════════════════════╗
║                                                            ║
║              🎉 TinyFlow 部署成功！🎉                     ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝

📍 访问地址：
   前端页面: http://47.97.110.128
   健康检查: http://47.97.110.128/actuator/health
```

---

## 🔧 手动部署（详细步骤）

如果你想了解每一步在做什么，或者脚本部署失败，可以按照以下步骤手动部署。

### 第一步：连接服务器

```bash
# Windows PowerShell / Linux / Mac 终端
ssh root@你的服务器IP

# 输入密码后成功登录
```

### 第二步：更新系统

```bash
# 更新软件包列表
apt update

# 升级已安装的软件包
apt upgrade -y

# 安装基础工具
apt install -y curl wget git vim unzip htop net-tools
```

### 第三步：安装 Java 17

```bash
# 安装 OpenJDK 17
apt install -y openjdk-17-jdk

# 验证安装
java -version
# 应该显示：openjdk version "17.x.x"
```

### 第四步：安装 MySQL

```bash
# 安装 MySQL Server
apt install -y mysql-server

# 启动 MySQL
systemctl start mysql
systemctl enable mysql

# 安全配置
mysql_secure_installation
```

配置过程：
```
1. 是否设置密码验证插件？ [n]
2. 设置 root 密码：输入你的强密码
3. 删除匿名用户？ [Y]
4. 禁止 root 远程登录？ [Y]
5. 删除测试数据库？ [Y]
6. 重新加载权限表？ [Y]
```

创建数据库：
```bash
mysql -u root -p
# 输入刚才设置的密码

# 在 MySQL 中执行
CREATE DATABASE `tiny-flow` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'tinyflow'@'localhost' IDENTIFIED BY '你的数据库密码';
GRANT ALL PRIVILEGES ON `tiny-flow`.* TO 'tinyflow'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 第五步：配置 Redis

如果你的 Redis 已经在运行，跳过安装步骤：

```bash
# 检查 Redis 状态
redis-cli ping
# 如果返回 PONG，说明 Redis 已运行

# 如果 Redis 有密码
redis-cli -a 你的Redis密码 ping

# 如果需要安装 Redis
apt install -y redis-server

# 配置 Redis（可选）
vim /etc/redis/redis.conf
# 找到 # requirepass foobared
# 修改为 requirepass 你的Redis密码

# 重启 Redis
systemctl restart redis-server
systemctl enable redis-server
```

### 第六步：安装 Maven 和 Node.js

```bash
# 安装 Maven
apt install -y maven
mvn -version

# 安装 Node.js 18
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt install -y nodejs

# 验证安装
node -v  # 应该显示 v18.x.x
npm -v   # 应该显示 9.x.x
```

### 第七步：安装 Nginx

```bash
# 安装 Nginx
apt install -y nginx

# 启动并设置开机自启
systemctl start nginx
systemctl enable nginx

# 验证安装
curl http://localhost
# 应该看到 Nginx 欢迎页面
```

### 第八步：克隆项目

```bash
# 进入 /opt 目录
cd /opt

# 克隆项目
git clone https://github.com/Layau-code/TinyFlow.git

# 进入项目目录
cd TinyFlow
```

### 第九步：创建生产配置

```bash
# 创建生产环境配置文件
cat > src/main/resources/application-prod.yml << 'EOF'
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tiny-flow?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci
    username: tinyflow
    password: 你的数据库密码  # 替换为实际密码
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 100
      minimum-idle: 20
      connection-timeout: 3000

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

  data:
    redis:
      host: localhost
      port: 6379
      password: 你的Redis密码  # 如果有密码就填写，没有就删除这行
      timeout: 2s
      lettuce:
        pool:
          max-active: 200
          max-idle: 50
          max-wait: 1s
          min-idle: 10

server:
  port: 8080

app:
  domain: http://你的服务器IP  # 替换为实际IP

clicks:
  mode: redis  # 使用 Redis 模式

cache:
  caffeine:
    spec: maximumSize=50000,expireAfterWrite=30m,recordStats
  warmup:
    enabled: true
    size: 5000

jwt:
  secret: 至少32位的超长密钥请自己生成一个随机字符串  # 替换为随机字符串
  expiration: 604800000

resilience4j:
  circuitbreaker:
    instances:
      redisBreaker:
        registerHealthIndicator: true
        slidingWindowSize: 100
        minimumNumberOfCalls: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
      dbBreaker:
        registerHealthIndicator: true
        slidingWindowSize: 60
        minimumNumberOfCalls: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
EOF

# 生成 JWT 密钥
openssl rand -base64 32
# 将生成的密钥复制到上面配置中的 jwt.secret
```

**重要**：记得替换配置文件中的以下内容：
- `你的数据库密码`
- `你的Redis密码`（如果没有就删除 password 这行）
- `你的服务器IP`
- `至少32位的超长密钥请自己生成一个随机字符串`

### 第十步：构建项目

```bash
# 构建后端
cd /opt/TinyFlow
./mvnw clean package -DskipTests

# 如果 mvnw 权限问题
chmod +x mvnw
./mvnw clean package -DskipTests

# 或者使用系统 Maven
mvn clean package -DskipTests

# 构建前端
cd web
npm install
npm run build

# 返回项目根目录
cd /opt/TinyFlow
```

### 第十一步：配置 Nginx

```bash
# 创建 Nginx 站点配置
cat > /etc/nginx/sites-available/tinyflow << 'EOF'
server {
    listen 80;
    server_name 你的服务器IP;  # 替换为实际IP或域名

    # 前端静态资源
    root /opt/TinyFlow/web/dist;
    index index.html;

    # 日志
    access_log /var/log/nginx/tinyflow_access.log;
    error_log /var/log/nginx/tinyflow_error.log;

    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 短链接跳转
    location ~ ^/[a-zA-Z0-9]{4,8}$ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 健康检查（仅本地访问）
    location /actuator {
        proxy_pass http://localhost:8080;
        allow 127.0.0.1;
        deny all;
    }

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    gzip_min_length 1000;
}
EOF

# 启用站点
ln -s /etc/nginx/sites-available/tinyflow /etc/nginx/sites-enabled/

# 删除默认站点
rm -f /etc/nginx/sites-enabled/default

# 测试配置
nginx -t

# 如果测试通过，重启 Nginx
systemctl restart nginx
```

### 第十二步：创建系统服务

```bash
# 创建 Systemd 服务文件
cat > /etc/systemd/system/tinyflow.service << 'EOF'
[Unit]
Description=TinyFlow Short URL Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/TinyFlow
ExecStart=/usr/bin/java \
    -Xms512m \
    -Xmx1024m \
    -Dspring.profiles.active=prod \
    -Dfile.encoding=UTF-8 \
    -jar /opt/TinyFlow/target/tinyflow-0.0.1-SNAPSHOT.jar

StandardOutput=journal
StandardError=journal
SyslogIdentifier=tinyflow

Restart=always
RestartSec=10

LimitNOFILE=65535

[Install]
WantedBy=multi-user.target
EOF

# 重新加载 systemd
systemctl daemon-reload

# 启动服务
systemctl start tinyflow

# 设置开机自启
systemctl enable tinyflow

# 查看服务状态
systemctl status tinyflow
```

### 第十三步：配置阿里云安全组

**重要**：必须在阿里云控制台配置安全组，否则无法访问！

1. 登录阿里云控制台：https://ecs.console.aliyun.com/
2. 找到你的 ECS 实例
3. 点击右侧的"更多" → "网络和安全组" → "安全组配置"
4. 点击"配置规则"
5. 点击"添加安全组规则"
6. 配置如下：

**入方向规则**：

| 规则方向 | 授权策略 | 协议类型 | 端口范围 | 授权对象 | 描述 |
|---------|---------|---------|---------|---------|------|
| 入方向 | 允许 | TCP | 80/80 | 0.0.0.0/0 | HTTP访问 |
| 入方向 | 允许 | TCP | 443/443 | 0.0.0.0/0 | HTTPS访问 |
| 入方向 | 允许 | TCP | 22/22 | 你的IP/32 | SSH管理 |

**注意**：
- 80 端口是必须的（HTTP 访问）
- 443 端口是 HTTPS 用的（可选，但推荐）
- 22 端口建议只允许你的 IP 访问（安全）
- **不要开放** 3306（MySQL）、6379（Redis）、8080（后端）端口

### 第十四步：验证部署

```bash
# 1. 检查所有服务状态
systemctl status tinyflow
systemctl status nginx
systemctl status mysql
systemctl status redis

# 2. 查看应用日志
journalctl -u tinyflow -f

# 3. 测试健康检查
curl http://localhost:8080/actuator/health

# 4. 测试 API
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"longUrl": "https://www.baidu.com"}'
```

### 第十五步：浏览器访问

打开浏览器，访问：

```
http://你的服务器IP
```

应该能看到 TinyFlow 的前端页面！🎉

---

## 📝 备案说明

### 什么是备案？

**ICP 备案**（Internet Content Provider）是中国大陆的法律要求，所有在中国大陆提供网站服务的域名都必须进行备案。

### 是否需要备案？

根据你的使用场景：

#### ✅ 需要备案的情况

1. **使用域名访问**（如 `www.tinyflow.com`）
   - 必须备案
   - 备案周期：7-20 天
   - 备案完成前，域名无法解析到国内服务器

2. **对外提供服务**（公开访问）
   - 必须备案
   - 即使用 IP 访问，如果是商业用途也建议备案

3. **80/443 端口访问**
   - 阿里云默认拦截未备案域名的 80/443 端口
   - 需要备案后才能正常使用

#### ❌ 暂时不需要备案的情况

1. **仅用 IP 访问**（如 `http://47.97.110.128`）
   - 可以直接使用
   - 适合测试、学习、演示
   - **你目前的情况属于这种**

2. **使用非标准端口**（如 `:8888`）
   - 可以暂时访问
   - 但不推荐，用户体验差

3. **内网使用**
   - 完全不需要备案

### 备案流程（阿里云）

如果你决定使用域名，需要进行备案：

#### 第一步：购买域名

```
1. 访问阿里云域名注册：https://wanwang.aliyun.com/
2. 搜索并购买域名（.com/.cn/.net 等）
3. 费用：约 50-100 元/年
```

#### 第二步：准备备案材料

**个人备案**：
- ✅ 身份证正反面照片
- ✅ 手机号（实名认证）
- ✅ 本人照片（白色背景）
- ✅ 域名证书（购买后自动获得）

**企业备案**：
- ✅ 营业执照
- ✅ 法人身份证
- ✅ 网站负责人身份证
- ✅ 公章

#### 第三步：提交备案申请

```
1. 登录阿里云备案系统：https://beian.aliyun.com/
2. 填写主体信息（个人/企业）
3. 填写网站信息
   - 网站名称：TinyFlow 短链接服务
   - 网站内容：工具软件
   - 服务类型：其他
4. 上传材料
5. 提交初审（1-2 个工作日）
```

#### 第四步：阿里云审核

```
1. 阿里云初审（1-2 天）
2. 如果有问题，修改后重新提交
3. 初审通过后，提交管局审核
```

#### 第五步：管局审核

```
1. 各省管局审核（7-20 天）
2. 期间可能会电话核实信息
3. 审核通过后会发送备案号
```

#### 第六步：备案成功

```
1. 收到备案号（如：赣ICP备XXXXXXXX号）
2. 在网站底部添加备案号链接
3. 域名可以正式解析到服务器
```

### 备案期间如何使用？

在备案期间，你可以：

#### 方案一：使用 IP 访问（推荐）

```
直接访问：http://47.97.110.128
优点：立即可用
缺点：不好记，不专业
```

#### 方案二：使用海外服务器

```
购买香港/海外服务器（不需要备案）
优点：可以使用域名
缺点：速度慢，价格贵
```

#### 方案三：使用免费二级域名

```
使用 Vercel/Netlify 等服务的二级域名
如：tinyflow.vercel.app
优点：免费，自动 HTTPS
缺点：国内访问可能慢
```

### 不备案的风险

⚠️ **风险提示**：

1. **域名被阻断**
   - 未备案域名访问国内服务器会被阻断
   - 80/443 端口无法使用

2. **服务器被关停**
   - 如果被举报，阿里云可能关停服务器
   - 严重的会被列入黑名单

3. **法律风险**
   - 商业用途必须备案
   - 违规可能面临罚款

### 推荐方案

**对于你的学习项目**：

```
阶段一（现在）：
- 使用 IP 访问：http://47.97.110.128
- 用于学习、测试、面试展示
- 不需要备案

阶段二（如果要正式上线）：
- 购买域名
- 提交备案申请
- 备案期间继续用 IP 访问
- 备案完成后配置域名

阶段三（长期运营）：
- 启用 HTTPS
- 配置 CDN 加速
- 添加监控告警
```

---

## 🌐 域名配置

如果你已经完成备案或使用香港服务器，可以配置域名。

### 第一步：域名解析

1. 登录阿里云控制台
2. 进入"云解析 DNS"
3. 找到你的域名，点击"解析设置"
4. 添加记录：

```
记录类型：A
主机记录：@（或 www）
记录值：你的服务器IP
TTL：10分钟
```

### 第二步：修改 Nginx 配置

```bash
vim /etc/nginx/sites-available/tinyflow

# 修改 server_name
server_name yourdomain.com www.yourdomain.com;
```

### 第三步：修改应用配置

```bash
vim /opt/TinyFlow/src/main/resources/application-prod.yml

# 修改 app.domain
app:
  domain: http://yourdomain.com
```

### 第四步：重启服务

```bash
systemctl restart nginx
systemctl restart tinyflow
```

---

## 🔒 HTTPS 配置

推荐使用 Let's Encrypt 免费证书。

### 安装 Certbot

```bash
# 安装 Certbot
apt install -y certbot python3-certbot-nginx

# 申请证书（自动配置 Nginx）
certbot --nginx -d yourdomain.com -d www.yourdomain.com

# 按提示输入邮箱和同意协议
```

### 测试自动续期

```bash
# 测试续期（不会实际续期）
certbot renew --dry-run

# 证书会在到期前自动续期
```

### 强制 HTTPS

```bash
# 编辑 Nginx 配置
vim /etc/nginx/sites-available/tinyflow

# 添加重定向
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

---

## ⚡ 性能优化

### 1. MySQL 优化

```bash
# 编辑 MySQL 配置
vim /etc/mysql/mysql.conf.d/mysqld.cnf

# 添加以下配置（根据你的内存调整）
[mysqld]
# 缓冲池大小（内存的 50-70%）
innodb_buffer_pool_size = 512M
# 日志文件大小
innodb_log_file_size = 128M
# 查询缓存
query_cache_size = 64M
query_cache_type = 1
# 连接数
max_connections = 200

# 重启 MySQL
systemctl restart mysql
```

### 2. Redis 优化

```bash
# 编辑 Redis 配置
vim /etc/redis/redis.conf

# 设置最大内存（建议 256M-512M）
maxmemory 512mb
maxmemory-policy allkeys-lru

# 开启持久化
save 900 1
save 300 10
save 60 10000

# 重启 Redis
systemctl restart redis-server
```

### 3. Nginx 优化

```bash
# 编辑 Nginx 配置
vim /etc/nginx/nginx.conf

# 在 http 块中添加
http {
    # 工作进程数（等于 CPU 核心数）
    worker_processes auto;
    
    # 每个进程的最大连接数
    events {
        worker_connections 2048;
    }
    
    # 开启 Gzip
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    
    # 缓存配置
    proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=my_cache:10m max_size=1g inactive=60m;
}

# 重启 Nginx
systemctl restart nginx
```

### 4. 应用优化

```bash
# 修改 Systemd 服务，增加 JVM 参数
vim /etc/systemd/system/tinyflow.service

# 修改 ExecStart（根据内存调整）
ExecStart=/usr/bin/java \
    -Xms512m \
    -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -Dspring.profiles.active=prod \
    -jar /opt/TinyFlow/target/tinyflow-0.0.1-SNAPSHOT.jar

# 重启服务
systemctl daemon-reload
systemctl restart tinyflow
```

---

## 📊 监控与运维

### 常用管理命令

```bash
# 查看服务状态
systemctl status tinyflow
systemctl status nginx
systemctl status mysql
systemctl status redis

# 查看实时日志
journalctl -u tinyflow -f

# 查看 Nginx 日志
tail -f /var/log/nginx/tinyflow_access.log
tail -f /var/log/nginx/tinyflow_error.log

# 查看系统资源
htop
free -h
df -h

# 重启服务
systemctl restart tinyflow
systemctl restart nginx
```

### 日志管理

```bash
# 配置日志滚动
cat > /etc/logrotate.d/tinyflow << 'EOF'
/var/log/nginx/tinyflow*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    create 0644 www-data www-data
}
EOF
```

### 备份脚本

```bash
# 创建备份脚本
cat > /opt/backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR=/opt/backups

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -u root -p你的密码 tiny-flow | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# 备份 Redis
redis-cli --rdb $BACKUP_DIR/redis_$DATE.rdb

# 删除 7 天前的备份
find $BACKUP_DIR -name "*.gz" -mtime +7 -delete
find $BACKUP_DIR -name "*.rdb" -mtime +7 -delete

echo "Backup completed: $DATE"
EOF

# 赋予执行权限
chmod +x /opt/backup.sh

# 添加到 crontab（每天凌晨 2 点备份）
crontab -e
# 添加：0 2 * * * /opt/backup.sh
```

---

## ❓ 常见问题

### Q1: 无法访问服务器

**原因**：
- 安全组没有开放 80 端口
- Nginx 没有启动
- 服务器防火墙阻止

**解决**：
```bash
# 检查安全组配置（阿里云控制台）
# 检查 Nginx
systemctl status nginx
systemctl start nginx

# 检查防火墙（Ubuntu 默认未启用）
ufw status
```

### Q2: 服务启动失败

**原因**：
- 数据库连接失败
- Redis 连接失败
- 端口被占用

**解决**：
```bash
# 查看详细日志
journalctl -u tinyflow -n 100 --no-pager

# 检查端口占用
netstat -tlnp | grep 8080

# 测试数据库连接
mysql -u tinyflow -p tiny-flow

# 测试 Redis 连接
redis-cli ping
```

### Q3: 前端页面空白

**原因**：
- 前端构建失败
- Nginx 配置错误
- 文件路径不对

**解决**：
```bash
# 检查前端文件是否存在
ls -la /opt/TinyFlow/web/dist/

# 重新构建前端
cd /opt/TinyFlow/web
npm run build

# 检查 Nginx 配置
nginx -t
```

### Q4: API 请求 404

**原因**：
- 后端服务未启动
- Nginx 代理配置错误
- 路由不匹配

**解决**：
```bash
# 检查后端服务
systemctl status tinyflow

# 测试后端 API
curl http://localhost:8080/actuator/health

# 查看 Nginx 错误日志
tail -f /var/log/nginx/tinyflow_error.log
```

### Q5: 短链接无法跳转

**原因**：
- Nginx location 配置优先级问题
- 短码格式不匹配

**解决**：
```bash
# 确保短链接规则在前端路由之前
vim /etc/nginx/sites-available/tinyflow

# 顺序应该是：
# 1. location ~ ^/[a-zA-Z0-9]{4,8}$  (短链接)
# 2. location /api                    (API)
# 3. location /                       (前端)
```

### Q6: 内存不足

**原因**：
- 服务器配置太低
- JVM 堆内存设置过大

**解决**：
```bash
# 查看内存使用
free -h

# 调整 JVM 参数（1GB 服务器）
vim /etc/systemd/system/tinyflow.service
# 改为：-Xms256m -Xmx512m

# 重启服务
systemctl daemon-reload
systemctl restart tinyflow
```

### Q7: 数据库连接池耗尽

**原因**：
- 并发量太大
- 连接未释放

**解决**：
```bash
# 修改数据库连接池配置
vim /opt/TinyFlow/src/main/resources/application-prod.yml

# 调整参数
hikari:
  maximum-pool-size: 50  # 降低最大连接数
  connection-timeout: 5000  # 增加超时时间

# 重新构建并部署
```

---

## 📞 技术支持

如果遇到问题，可以：

1. **查看日志**
   ```bash
   journalctl -u tinyflow -f
   tail -f /var/log/nginx/tinyflow_error.log
   ```

2. **GitHub Issues**
   - 提交问题：https://github.com/Layau-code/TinyFlow/issues

3. **联系作者**
   - Email: 18970931397@163.com

---

## 📚 参考资源

- 阿里云 ECS 文档：https://help.aliyun.com/product/25365.html
- 阿里云备案指南：https://beian.aliyun.com/
- Let's Encrypt 官网：https://letsencrypt.org/
- Nginx 官方文档：https://nginx.org/en/docs/

---

## ✅ 部署检查清单

部署前检查：
- [ ] 服务器已准备（Ubuntu 22.04）
- [ ] SSH 可以连接
- [ ] 记录了各种密码
- [ ] 了解是否需要备案

部署中检查：
- [ ] 所有软件安装成功
- [ ] 数据库创建成功
- [ ] 项目构建成功
- [ ] Nginx 配置正确
- [ ] 安全组已配置

部署后检查：
- [ ] 服务正常启动
- [ ] 前端页面可访问
- [ ] API 接口正常
- [ ] 短链接可跳转
- [ ] 日志无错误

---

**祝你部署顺利！🎉**

如有问题，随时查阅本文档或联系技术支持。
