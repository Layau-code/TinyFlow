#!/bin/bash

###############################################################################
# TinyFlow 一键部署脚本
# 适用于阿里云 Ubuntu 服务器
# 作者：Layau
# 日期：2024-12-21
###############################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否为 root 用户
check_root() {
    if [ "$EUID" -ne 0 ]; then 
        log_error "请使用 root 用户运行此脚本"
        exit 1
    fi
}

# 打印欢迎信息
print_banner() {
    clear
    echo -e "${BLUE}"
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║                                                            ║"
    echo "║              TinyFlow 一键部署脚本 v1.0                   ║"
    echo "║                                                            ║"
    echo "║     世界上没有两个相同的雨滴，就像每个短链接都独一无二     ║"
    echo "║                                                            ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo ""
}

# 收集配置信息
collect_config() {
    log_info "开始收集配置信息..."
    echo ""
    
    # MySQL 密码
    read -p "请输入 MySQL root 密码（将创建新密码）: " -s MYSQL_ROOT_PASSWORD
    echo ""
    read -p "请再次输入 MySQL root 密码: " -s MYSQL_ROOT_PASSWORD_CONFIRM
    echo ""
    
    if [ "$MYSQL_ROOT_PASSWORD" != "$MYSQL_ROOT_PASSWORD_CONFIRM" ]; then
        log_error "两次密码输入不一致！"
        exit 1
    fi
    
    # TinyFlow 数据库密码
    read -p "请输入 TinyFlow 数据库用户密码: " -s DB_PASSWORD
    echo ""
    
    # Redis 密码
    read -p "请输入 Redis 密码（如果没有直接回车）: " -s REDIS_PASSWORD
    echo ""
    
    # JWT 密钥
    log_info "正在生成 JWT 密钥..."
    JWT_SECRET=$(openssl rand -base64 32)
    log_success "JWT 密钥已生成"
    
    # 服务器 IP
    SERVER_IP=$(curl -s ifconfig.me || curl -s ipinfo.io/ip || echo "未获取到")
    log_info "检测到服务器 IP: $SERVER_IP"
    read -p "请确认或修改服务器 IP/域名 [$SERVER_IP]: " INPUT_IP
    SERVER_IP=${INPUT_IP:-$SERVER_IP}
    
    echo ""
    log_success "配置信息收集完成！"
    echo ""
}

# 更新系统
update_system() {
    log_info "更新系统包..."
    apt update -y
    apt upgrade -y
    log_success "系统更新完成"
}

# 安装基础软件
install_basic_tools() {
    log_info "安装基础工具..."
    apt install -y curl wget git vim unzip htop net-tools
    log_success "基础工具安装完成"
}

# 安装 Java 17
install_java() {
    log_info "安装 Java 17..."
    apt install -y openjdk-17-jdk
    java -version
    log_success "Java 17 安装完成"
}

# 安装 MySQL
install_mysql() {
    log_info "安装 MySQL..."
    
    # 设置非交互式安装
    export DEBIAN_FRONTEND=noninteractive
    
    apt install -y mysql-server
    systemctl start mysql
    systemctl enable mysql
    
    # 设置 root 密码
    log_info "配置 MySQL root 密码..."
    mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$MYSQL_ROOT_PASSWORD';"
    mysql -e "FLUSH PRIVILEGES;"
    
    # 创建数据库和用户
    log_info "创建 TinyFlow 数据库..."
    mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<EOF
CREATE DATABASE IF NOT EXISTS \`tiny-flow\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'tinyflow'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON \`tiny-flow\`.* TO 'tinyflow'@'localhost';
FLUSH PRIVILEGES;
EOF
    
    log_success "MySQL 安装并配置完成"
}

# 检查 Redis
check_redis() {
    log_info "检查 Redis 状态..."
    
    if command -v redis-cli &> /dev/null; then
        if redis-cli ping &> /dev/null || redis-cli -a "$REDIS_PASSWORD" ping &> /dev/null; then
            log_success "Redis 已运行"
            return 0
        fi
    fi
    
    log_warning "Redis 未运行或未安装"
    read -p "是否需要安装 Redis? (y/n): " install_redis
    
    if [ "$install_redis" = "y" ]; then
        install_redis_server
    fi
}

# 安装 Redis
install_redis_server() {
    log_info "安装 Redis..."
    apt install -y redis-server
    
    # 配置 Redis
    if [ -n "$REDIS_PASSWORD" ]; then
        sed -i "s/# requirepass.*/requirepass $REDIS_PASSWORD/" /etc/redis/redis.conf
    fi
    
    systemctl restart redis-server
    systemctl enable redis-server
    
    log_success "Redis 安装完成"
}

# 安装 Maven
install_maven() {
    log_info "安装 Maven..."
    apt install -y maven
    mvn -version
    log_success "Maven 安装完成"
}

# 安装 Node.js
install_nodejs() {
    log_info "安装 Node.js 18..."
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt install -y nodejs
    node -v
    npm -v
    log_success "Node.js 安装完成"
}

# 安装 Nginx
install_nginx() {
    log_info "安装 Nginx..."
    apt install -y nginx
    systemctl start nginx
    systemctl enable nginx
    log_success "Nginx 安装完成"
}

# 克隆项目
clone_project() {
    log_info "克隆 TinyFlow 项目..."
    
    cd /opt
    if [ -d "TinyFlow" ]; then
        log_warning "项目目录已存在，是否删除重新克隆? (y/n)"
        read -p "> " delete_old
        if [ "$delete_old" = "y" ]; then
            rm -rf TinyFlow
        else
            log_info "使用现有项目目录"
            return 0
        fi
    fi
    
    git clone https://github.com/Layau-code/TinyFlow.git
    cd TinyFlow
    
    log_success "项目克隆完成"
}

# 创建生产配置
create_prod_config() {
    log_info "创建生产环境配置..."
    
    # Redis 密码配置
    if [ -n "$REDIS_PASSWORD" ]; then
        REDIS_PASSWORD_LINE="password: $REDIS_PASSWORD"
    else
        REDIS_PASSWORD_LINE="# password: "
    fi
    
    cat > /opt/TinyFlow/src/main/resources/application-prod.yml <<EOF
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tiny-flow?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci
    username: tinyflow
    password: $DB_PASSWORD
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 100
      minimum-idle: 20
      connection-timeout: 3000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    database-platform: org.hibernate.dialect.MySQL8Dialect

  data:
    redis:
      host: localhost
      port: 6379
      $REDIS_PASSWORD_LINE
      timeout: 2s
      lettuce:
        pool:
          max-active: 200
          max-idle: 50
          max-wait: 1s
          min-idle: 10

server:
  port: 8080
  tomcat:
    threads:
      max: 300
      min-spare: 50
    accept-count: 1000
    max-connections: 10000

app:
  domain: http://$SERVER_IP

clicks:
  mode: redis

cache:
  caffeine:
    spec: maximumSize=50000,expireAfterWrite=30m,recordStats
  warmup:
    enabled: true
    size: 5000

jwt:
  secret: $JWT_SECRET
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

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
EOF
    
    log_success "生产配置创建完成"
}

# 构建项目
build_project() {
    log_info "构建后端项目..."
    cd /opt/TinyFlow
    
    # 使用 Maven 构建
    mvn clean package -DskipTests
    
    if [ ! -f "target/tinyflow-0.0.1-SNAPSHOT.jar" ]; then
        log_error "后端构建失败！"
        exit 1
    fi
    
    log_success "后端构建完成"
    
    log_info "构建前端项目..."
    cd web
    npm install
    npm run build
    
    if [ ! -d "dist" ]; then
        log_error "前端构建失败！"
        exit 1
    fi
    
    log_success "前端构建完成"
    cd /opt/TinyFlow
}

# 配置 Nginx
configure_nginx() {
    log_info "配置 Nginx..."
    
    cat > /etc/nginx/sites-available/tinyflow <<EOF
server {
    listen 80;
    server_name $SERVER_IP;

    # 前端静态资源
    root /opt/TinyFlow/web/dist;
    index index.html;

    # 日志
    access_log /var/log/nginx/tinyflow_access.log;
    error_log /var/log/nginx/tinyflow_error.log;

    # 重要：短链接跳转（必须放在前端路由之前，使用正则匹配）
    location ~ ^/[a-zA-Z0-9]{4,8}\$ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # 后端 API 代理（使用 ^~ 提高优先级）
    location ^~ /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 健康检查（使用 ^~ 提高优先级）
    location ^~ /actuator {
        proxy_pass http://localhost:8080;
        allow 127.0.0.1;
        deny all;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)\$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # 前端路由（放在最后）
    location / {
        try_files \$uri \$uri/ /index.html;
    }

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    gzip_min_length 1000;
}
EOF
    
    # 启用站点
    ln -sf /etc/nginx/sites-available/tinyflow /etc/nginx/sites-enabled/
    rm -f /etc/nginx/sites-enabled/default
    
    # 测试配置
    nginx -t
    systemctl restart nginx
    
    log_success "Nginx 配置完成"
}

# 创建 Systemd 服务
create_systemd_service() {
    log_info "创建 Systemd 服务..."
    
    cat > /etc/systemd/system/tinyflow.service <<EOF
[Unit]
Description=TinyFlow Short URL Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/TinyFlow
ExecStart=/usr/bin/java \\
    -Xms512m \\
    -Xmx1024m \\
    -Dspring.profiles.active=prod \\
    -Dfile.encoding=UTF-8 \\
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
    
    systemctl daemon-reload
    systemctl enable tinyflow
    systemctl start tinyflow
    
    log_success "Systemd 服务创建完成"
}

# 等待服务启动
wait_for_service() {
    log_info "等待服务启动（最多等待 60 秒）..."
    
    for i in {1..60}; do
        if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            log_success "服务启动成功！"
            return 0
        fi
        echo -n "."
        sleep 1
    done
    
    echo ""
    log_error "服务启动超时！请检查日志: journalctl -u tinyflow -f"
    return 1
}

# 打印部署信息
print_deployment_info() {
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                                                            ║${NC}"
    echo -e "${GREEN}║              🎉 TinyFlow 部署成功！🎉                     ║${NC}"
    echo -e "${GREEN}║                                                            ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${BLUE}📍 访问地址：${NC}"
    echo -e "   前端页面: ${GREEN}http://$SERVER_IP${NC}"
    echo -e "   健康检查: ${GREEN}http://$SERVER_IP/actuator/health${NC}"
    echo ""
    echo -e "${BLUE}🔑 重要信息（请妥善保存）：${NC}"
    echo -e "   MySQL Root 密码: $MYSQL_ROOT_PASSWORD"
    echo -e "   TinyFlow 数据库密码: $DB_PASSWORD"
    echo -e "   JWT 密钥: $JWT_SECRET"
    echo ""
    echo -e "${BLUE}📝 常用命令：${NC}"
    echo -e "   查看服务状态: ${YELLOW}systemctl status tinyflow${NC}"
    echo -e "   查看实时日志: ${YELLOW}journalctl -u tinyflow -f${NC}"
    echo -e "   重启服务: ${YELLOW}systemctl restart tinyflow${NC}"
    echo -e "   查看 Nginx 日志: ${YELLOW}tail -f /var/log/nginx/tinyflow_*.log${NC}"
    echo ""
    echo -e "${BLUE}⚠️  安全提醒：${NC}"
    echo -e "   1. 请在阿里云控制台配置安全组，开放 80 端口"
    echo -e "   2. 建议配置 HTTPS 证书（使用 certbot）"
    echo -e "   3. 定期备份数据库和 Redis 数据"
    echo ""
    echo -e "${BLUE}📚 更多信息：${NC}"
    echo -e "   GitHub: ${GREEN}https://github.com/Layau-code/TinyFlow${NC}"
    echo ""
}

# 保存配置信息
save_config() {
    cat > /opt/TinyFlow/deployment_info.txt <<EOF
TinyFlow 部署信息
================
部署时间: $(date)
服务器 IP: $SERVER_IP

数据库信息:
-----------
MySQL Root 密码: $MYSQL_ROOT_PASSWORD
TinyFlow 数据库: tiny-flow
TinyFlow 用户: tinyflow
TinyFlow 密码: $DB_PASSWORD

Redis 信息:
-----------
Redis 地址: localhost:6379
Redis 密码: $REDIS_PASSWORD

JWT 配置:
---------
JWT 密钥: $JWT_SECRET

服务管理:
---------
查看状态: systemctl status tinyflow
查看日志: journalctl -u tinyflow -f
重启服务: systemctl restart tinyflow
EOF
    
    chmod 600 /opt/TinyFlow/deployment_info.txt
    log_success "配置信息已保存到 /opt/TinyFlow/deployment_info.txt"
}

# 主函数
main() {
    print_banner
    check_root
    
    log_info "开始 TinyFlow 自动化部署..."
    echo ""
    
    # 收集配置
    collect_config
    
    # 安装软件
    update_system
    install_basic_tools
    install_java
    install_mysql
    check_redis
    install_maven
    install_nodejs
    install_nginx
    
    # 部署项目
    clone_project
    create_prod_config
    build_project
    configure_nginx
    create_systemd_service
    
    # 等待服务启动
    if wait_for_service; then
        save_config
        print_deployment_info
    else
        log_error "部署失败，请检查日志"
        exit 1
    fi
}

# 执行主函数
main
