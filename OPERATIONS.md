# TinyFlow 运维与扩展指南

> 阿里云服务器运维手册 - 常用命令、故障排查、性能优化

---

## 📋 目录

- [一、常用运维命令](#一常用运维命令)
- [二、服务管理](#二服务管理)
- [三、日志查看](#三日志查看)
- [四、故障排查](#四故障排查)
- [五、性能监控](#五性能监控)
- [六、数据备份](#六数据备份)
- [七、后续扩展](#七后续扩展)
- [八、安全加固](#八安全加固)

---

## 一、常用运维命令

### 1.1 服务器登录

```bash
# Windows PowerShell
ssh -p 22222 -i C:\Users\你的用户名\.ssh\aliyun_id_rsa root@47.97.110.128
```

### 1.2 检查服务器状态

```bash
# 查看系统资源使用情况
htop  # 按 q 退出

# 查看磁盘使用
df -h

# 查看内存使用
free -h

# 查看网络连接
netstat -tlnp
```

---

## 二、服务管理

### 2.1 TinyFlow 后端服务

```bash
# 启动服务
systemctl start tinyflow

# 停止服务
systemctl stop tinyflow

# 重启服务
systemctl restart tinyflow

# 查看状态
systemctl status tinyflow

# 开机自启（已设置）
systemctl enable tinyflow
```

### 2.2 Nginx 服务

```bash
# 启动
systemctl start nginx

# 重启
systemctl restart nginx

# 测试配置是否正确
nginx -t

# 查看状态
systemctl status nginx
```

### 2.3 MySQL 服务

```bash
# 重启
systemctl restart mysql

# 查看状态
systemctl status mysql

# 登录数据库
mysql -u root -p
# 输入密码：123456

# 登录应用数据库
mysql -u tinyflow -p tiny-flow
# 输入密码：123456
```

### 2.4 Redis 服务

```bash
# 重启
systemctl restart redis

# 查看状态
systemctl status redis

# 测试连接
redis-cli -a 123456 ping
# 应该返回 PONG

# 进入 Redis 客户端
redis-cli -a 123456
# 常用命令：
# keys *           # 查看所有key
# get key名        # 获取值
# flushall         # 清空所有数据（危险！）
```

---

## 三、日志查看

### 3.1 后端应用日志

```bash
# 实时查看日志（Ctrl+C 退出）
journalctl -u tinyflow -f

# 查看最近100行日志
journalctl -u tinyflow -n 100 --no-pager

# 查看最近1小时的日志
journalctl -u tinyflow --since "1 hour ago"

# 查看错误日志
journalctl -u tinyflow --since today | grep -i error

# 查看某个功能的日志（例如注册）
journalctl -u tinyflow --since "10 minutes ago" | grep "用户注册"
```

### 3.2 Nginx 日志

```bash
# 访问日志
tail -f /var/log/nginx/access.log

# 错误日志
tail -f /var/log/nginx/error.log

# 查看最近50条访问记录
tail -n 50 /var/log/nginx/access.log
```

---

## 四、故障排查

### 4.1 服务无法启动

```bash
# 1. 查看详细错误
journalctl -u tinyflow -n 50 --no-pager

# 2. 检查端口占用
netstat -tlnp | grep 8080

# 3. 检查配置文件
cat /etc/systemd/system/tinyflow.service

# 4. 测试jar包能否直接运行
cd /opt/tinyflow
java -jar app.jar

# 5. 查看系统资源
free -h  # 内存
df -h    # 磁盘
```

### 4.2 数据库连接失败

```bash
# 1. 检查 MySQL 是否运行
systemctl status mysql

# 2. 测试数据库连接
mysql -u tinyflow -p tiny-flow
# 密码：123456

# 3. 查看数据库日志
tail -f /var/log/mysql/error.log

# 4. 检查数据库用户权限
mysql -u root -p
SHOW GRANTS FOR 'tinyflow'@'localhost';
```

### 4.3 Redis 连接失败

```bash
# 1. 检查 Redis 是否运行
systemctl status redis

# 2. 测试连接
redis-cli -a 123456 ping

# 3. 查看 Redis 日志
journalctl -u redis -n 50

# 4. 检查 Redis 配置
grep -E "requirepass|bind" /etc/redis/redis.conf
```

### 4.4 前端访问失败

```bash
# 1. 检查 Nginx 状态
systemctl status nginx

# 2. 测试 Nginx 配置
nginx -t

# 3. 检查前端文件是否存在
ls -lh /opt/TinyFlow/web/dist/

# 4. 查看 Nginx 错误日志
tail -f /var/log/nginx/error.log

# 5. 测试后端API
curl http://localhost:8080/api/auth/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

---

## 五、性能监控

### 5.1 实时监控命令

```bash
# 综合监控（推荐）
htop

# CPU使用率
top

# 内存使用
free -h

# 磁盘IO
iostat -x 1

# 网络流量
iftop

# 查看进程
ps aux | grep java
```

### 5.2 性能指标

```bash
# 查看 TinyFlow 进程资源使用
ps aux | grep tinyflow

# 查看端口连接数
netstat -an | grep :8080 | wc -l

# 查看 MySQL 连接数
mysql -u root -p -e "SHOW PROCESSLIST;"

# 查看 Redis 信息
redis-cli -a 123456 info stats
```

---

## 六、数据备份

### 6.1 数据库备份

```bash
# 手动备份
mysqldump -u root -p tiny-flow > /root/backup/tinyflow_$(date +%Y%m%d).sql

# 创建备份目录
mkdir -p /root/backup

# 定时备份（每天凌晨2点）
crontab -e
# 添加以下行：
0 2 * * * mysqldump -u root -p123456 tiny-flow > /root/backup/tinyflow_$(date +\%Y\%m\%d).sql

# 恢复数据
mysql -u root -p tiny-flow < /root/backup/tinyflow_20251221.sql
```

### 6.2 Redis 数据备份

```bash
# Redis 自动持久化到
/var/lib/redis/dump.rdb

# 手动保存
redis-cli -a 123456 save

# 备份文件
cp /var/lib/redis/dump.rdb /root/backup/redis_$(date +%Y%m%d).rdb
```

### 6.3 代码备份

```bash
# 备份配置文件
cp /etc/systemd/system/tinyflow.service /root/backup/
cp /etc/nginx/sites-available/tinyflow /root/backup/

# 备份应用文件
cp /opt/tinyflow/app.jar /root/backup/app_$(date +%Y%m%d).jar
```

---

## 七、后续扩展

### 7.1 升级应用版本

#### 本地修改代码后部署新版本

```bash
# 1. 在本地推送代码到 GitHub
git add .
git commit -m "feat: 新功能"
git push origin main

# 2. 在服务器上更新代码
cd /opt/TinyFlow
git pull origin main

# 3. 重新构建后端
mvn clean package -DskipTests

# 4. 停止服务
systemctl stop tinyflow

# 5. 备份旧版本
cp /opt/tinyflow/app.jar /opt/tinyflow/app.jar.backup

# 6. 替换新版本
cp target/TinyFlow-*.jar /opt/tinyflow/app.jar

# 7. 启动服务
systemctl start tinyflow

# 8. 检查启动状态
sleep 30
systemctl status tinyflow
journalctl -u tinyflow -n 20

# 9. 如果有前端修改，重新构建前端
cd /opt/TinyFlow/web
npm install
npm run build

# 10. 重启 Nginx
systemctl restart nginx
```

### 7.2 修改配置参数

#### 修改数据库密码

```bash
# 1. 修改 MySQL 密码
mysql -u root -p
ALTER USER 'tinyflow'@'localhost' IDENTIFIED BY '新密码';
FLUSH PRIVILEGES;
exit;

# 2. 修改服务配置
nano /etc/systemd/system/tinyflow.service
# 找到 --spring.datasource.password=123456
# 改成 --spring.datasource.password=新密码

# 3. 重启服务
systemctl daemon-reload
systemctl restart tinyflow
```

#### 修改 Redis 密码

```bash
# 1. 修改 Redis 配置
nano /etc/redis/redis.conf
# 找到 requirepass 123456
# 改成 requirepass 新密码

# 2. 重启 Redis
systemctl restart redis

# 3. 修改应用配置
nano /etc/systemd/system/tinyflow.service
# 找到 --spring.redis.password=123456
# 改成 --spring.redis.password=新密码

# 4. 重启应用
systemctl daemon-reload
systemctl restart tinyflow
```

#### 修改 JWT 密钥

```bash
# 1. 生成新密钥（64位十六进制）
openssl rand -hex 32

# 2. 修改服务配置
nano /etc/systemd/system/tinyflow.service
# 找到 --jwt.secret=xxx
# 替换成新生成的密钥

# 3. 重启服务
systemctl daemon-reload
systemctl restart tinyflow

# 注意：修改后旧的token会失效，用户需要重新登录
```

### 7.3 域名配置（如果购买了域名）

```bash
# 1. 在阿里云控制台添加域名解析
# A 记录：yourdomain.com -> 47.97.110.128

# 2. 修改 Nginx 配置
nano /etc/nginx/sites-available/tinyflow
# 找到 server_name _;
# 改成 server_name yourdomain.com;

# 3. 修改后端域名配置
nano /etc/systemd/system/tinyflow.service
# 找到 --app.domain=http://47.97.110.128
# 改成 --app.domain=http://yourdomain.com

# 4. 重启服务
nginx -t
systemctl restart nginx
systemctl daemon-reload
systemctl restart tinyflow

# 5. 配置 HTTPS（可选，推荐）
# 安装 certbot
apt install certbot python3-certbot-nginx

# 自动配置 SSL
certbot --nginx -d yourdomain.com

# 自动续期
certbot renew --dry-run
```

### 7.4 性能优化

#### 增加 JVM 内存

```bash
# 编辑服务配置
nano /etc/systemd/system/tinyflow.service

# 修改 ExecStart，在 java 和 -jar 之间添加内存参数
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar /opt/tinyflow/app.jar ...

# 重启服务
systemctl daemon-reload
systemctl restart tinyflow
```

#### 数据库优化

```bash
# 编辑 MySQL 配置
nano /etc/mysql/mysql.conf.d/mysqld.cnf

# 添加优化参数
[mysqld]
max_connections = 500
innodb_buffer_pool_size = 512M
query_cache_size = 64M

# 重启 MySQL
systemctl restart mysql
```

### 7.5 添加新功能模块

#### 示例：添加邮件发送功能

```bash
# 1. 修改 pom.xml 添加依赖
# 2. 在本地开发新功能
# 3. 推送到 GitHub
# 4. 按照 7.1 升级应用版本步骤部署

# 5. 如果需要新的配置项
nano /etc/systemd/system/tinyflow.service
# 添加邮件配置
--spring.mail.host=smtp.qq.com \
--spring.mail.username=your@email.com \
--spring.mail.password=授权码

systemctl daemon-reload
systemctl restart tinyflow
```

---

## 八、安全加固

### 8.1 防火墙配置

```bash
# 查看防火墙状态
ufw status

# 开启防火墙
ufw enable

# 允许 SSH（重要！）
ufw allow 22222/tcp

# 允许 HTTP
ufw allow 80/tcp

# 允许 HTTPS
ufw allow 443/tcp

# 禁止外部访问 MySQL
ufw deny 3306/tcp

# 禁止外部访问 Redis
ufw deny 6379/tcp

# 禁止外部访问后端端口
ufw deny 8080/tcp
```

### 8.2 定期更新系统

```bash
# 更新软件包列表
apt update

# 升级已安装的软件
apt upgrade -y

# 重启服务器（如果需要）
reboot
```

### 8.3 修改默认密码

```bash
# 修改 root 密码
passwd root

# 修改 MySQL root 密码
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY '复杂密码';
FLUSH PRIVILEGES;
```

### 8.4 日志清理（防止磁盘满）

```bash
# 清理旧日志（保留最近7天）
journalctl --vacuum-time=7d

# 查看日志占用空间
journalctl --disk-usage

# 清理 Nginx 日志
echo > /var/log/nginx/access.log
echo > /var/log/nginx/error.log
```

---

## 九、常见问题 FAQ

### Q1: 服务器重启后服务没有自动启动？

```bash
# 检查是否设置了开机自启
systemctl is-enabled tinyflow
systemctl is-enabled nginx
systemctl is-enabled mysql
systemctl is-enabled redis

# 设置开机自启
systemctl enable tinyflow nginx mysql redis
```

### Q2: 磁盘空间不足？

```bash
# 查看磁盘使用
df -h

# 查找大文件
du -h --max-depth=1 / | sort -hr | head -20

# 清理日志
journalctl --vacuum-size=500M

# 清理 Docker（如果使用）
docker system prune -a
```

### Q3: 内存不足？

```bash
# 查看内存使用
free -h

# 重启 TinyFlow 释放内存
systemctl restart tinyflow

# 查看进程内存占用
ps aux --sort=-%mem | head
```

### Q4: 如何查看访问统计？

```bash
# 查看今天的访问量
cat /var/log/nginx/access.log | grep "$(date +%d/%b/%Y)" | wc -l

# 查看最频繁访问的 IP
cat /var/log/nginx/access.log | awk '{print $1}' | sort | uniq -c | sort -rn | head -10

# 查看最常访问的 URL
cat /var/log/nginx/access.log | awk '{print $7}' | sort | uniq -c | sort -rn | head -10
```

### Q5: 如何完全重置项目？

```bash
# 警告：此操作会删除所有数据！

# 1. 停止所有服务
systemctl stop tinyflow nginx

# 2. 删除数据库
mysql -u root -p -e "DROP DATABASE tiny_flow;"
mysql -u root -p -e "CREATE DATABASE tiny_flow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. 清空 Redis
redis-cli -a 123456 FLUSHALL

# 4. 重新部署
cd /opt/TinyFlow
git pull origin main
mvn clean package -DskipTests
cp target/TinyFlow-*.jar /opt/tinyflow/app.jar

# 5. 启动服务
systemctl start tinyflow nginx
```

---

## 十、联系与支持

- **项目文档**：[DEPLOY_ALIYUN.md](./DEPLOY_ALIYUN.md)
- **GitHub**：https://github.com/Layau-code/TinyFlow
- **问题反馈**：提交 GitHub Issue

---

## 附录：快速命令速查表

```bash
# ========== 服务管理 ==========
systemctl restart tinyflow    # 重启后端
systemctl restart nginx       # 重启前端
systemctl restart mysql       # 重启数据库
systemctl restart redis       # 重启缓存

# ========== 日志查看 ==========
journalctl -u tinyflow -f     # 实时后端日志
tail -f /var/log/nginx/access.log  # 实时访问日志

# ========== 健康检查 ==========
systemctl status tinyflow nginx mysql redis  # 查看所有服务状态
df -h && free -h             # 磁盘和内存

# ========== 快速备份 ==========
mysqldump -u root -p123456 tiny-flow > ~/backup_$(date +%Y%m%d).sql

# ========== 更新部署 ==========
cd /opt/TinyFlow && git pull && mvn clean package -DskipTests
systemctl stop tinyflow
cp target/TinyFlow-*.jar /opt/tinyflow/app.jar
systemctl start tinyflow
```

---

**祝运维顺利！** 🚀
