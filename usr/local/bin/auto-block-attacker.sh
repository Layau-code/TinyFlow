#!/bin/bash

# TinyFlow 自动检测并封禁攻击IP脚本
# 用法: /usr/local/bin/auto-block-attacker.sh

LOG_FILE="/var/log/nginx/tinyflow_access.log"
NGINX_CONF="/etc/nginx/nginx.conf"
ALERT_LOG="/var/log/tinyflow-security.log"
THRESHOLD=500  # 1分钟内超过500次请求的IP视为攻击

# 确保日志目录存在
mkdir -p /var/log

# 统计最近1分钟的高频IP
echo "[$(date)] ========== 开始检测攻击IP ==========" >> $ALERT_LOG

# 获取最近1分钟的时间戳
CURRENT_TIME=$(date '+%d/%b/%Y:%H:%M')
PREV_MINUTE=$(date -d '1 minute ago' '+%d/%b/%Y:%H:%M')

# 统计该分钟内的高频IP
ATTACK_IPS=$(sudo grep "$CURRENT_TIME\|$PREV_MINUTE" $LOG_FILE 2>/dev/null | \
  awk '{print $1}' | \
  sort | uniq -c | \
  awk -v threshold=$THRESHOLD '$1 > threshold {print $2}')

if [ -z "$ATTACK_IPS" ]; then
  echo "[$(date)] ✅ 未检测到攻击 (阈值: $THRESHOLD req/min)" >> $ALERT_LOG
  exit 0
fi

# 发现攻击，立即追加到Nginx黑名单
echo "[$(date)] 🔴 发现攻击IP，详见下方列表:" >> $ALERT_LOG

for ip in $ATTACK_IPS; do
  REQUEST_COUNT=$(sudo grep "$CURRENT_TIME\|$PREV_MINUTE" $LOG_FILE 2>/dev/null | \
    grep "^$ip " | wc -l)
  echo "[$(date)] ⚠️  IP: $ip (请求数: $REQUEST_COUNT)" >> $ALERT_LOG
  
  # 检查是否已在黑名单中
  if ! sudo grep -q "^\s*$ip\s*1;" $NGINX_CONF; then
    # 添加到Nginx黑名单
    sudo sed -i "/geo \$is_blocked {/a\\        $ip 1;" $NGINX_CONF
    echo "[$(date)] ✅ 已将 $ip 加入黑名单" >> $ALERT_LOG
  fi
done

# 测试Nginx配置语法
if sudo nginx -t 2>&1 | grep -q "successful"; then
  echo "[$(date)] ✅ Nginx配置语法正确" >> $ALERT_LOG
  
  # 重载Nginx
  sudo nginx -s reload
  echo "[$(date)] ✅ 已重载Nginx，黑名单生效" >> $ALERT_LOG
  
  # 发送告警（可选：接入钉钉/企业微信）
  echo "[$(date)] 🔔 【TinyFlow安全告警】已自动封禁 $(echo $ATTACK_IPS | wc -w) 个攻击IP" >> $ALERT_LOG
else
  echo "[$(date)] ❌ Nginx配置有错误，未重载" >> $ALERT_LOG
fi

echo "[$(date)] ========== 检测完成 ==========" >> $ALERT_LOG
