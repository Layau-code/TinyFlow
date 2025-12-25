# HTTPS配置

<cite>
**本文档引用文件**  
- [DEPLOY_ALIYUN.md](file://DEPLOY_ALIYUN.md)
- [deploy.sh](file://deploy.sh)
- [application.yml](file://src/main/resources/application.yml)
</cite>

## 目录
1. [HTTPS配置](#https配置)
2. [Let's Encrypt证书申请流程](#lets-encrypt证书申请流程)
3. [证书自动续期机制](#证书自动续期机制)
4. [强制HTTPS重定向](#强制https重定向)
5. [安全组配置](#安全组配置)
6. [HTTPS的重要性](#https的重要性)

## HTTPS配置

根据`DEPLOY_ALIYUN.md`文档，TinyFlow部署Let's Encrypt免费SSL证书的完整流程如下。该流程通过Certbot工具实现自动化证书申请与Nginx配置更新，确保网站通过加密传输提升安全性和可信度。

**Section sources**
- [DEPLOY_ALIYUN.md](file://DEPLOY_ALIYUN.md#L778-L815)

## Let's Encrypt证书申请流程

为TinyFlow部署Let's Encrypt免费SSL证书需要执行以下步骤：

1. **安装Certbot及其Nginx插件**：
   ```bash
   apt install -y certbot python3-certbot-nginx
   ```

2. **执行证书申请命令**：
   ```bash
   certbot --nginx -d yourdomain.com -d www.yourdomain.com
   ```
   在执行过程中，系统会提示输入邮箱地址并要求同意Let's Encrypt的服务协议。Certbot将自动完成证书申请，并更新Nginx配置文件以启用HTTPS。

3. **域名配置前提**：
   - 必须已完成域名备案（针对中国大陆地区）
   - 域名已正确解析到服务器IP地址
   - 阿里云安全组已开放443端口（HTTPS）

此流程利用Certbot的Nginx插件实现了无缝集成，无需手动修改Nginx配置文件即可完成SSL证书的部署。

**Section sources**
- [DEPLOY_ALIYUN.md](file://DEPLOY_ALIYUN.md#L782-L792)

## 证书自动续期机制

Let's Encrypt证书的有效期为90天，但系统已配置自动续期机制以确保服务连续性：

1. **自动续期原理**：
   Certbot通过系统定时任务（cron job）定期检查证书有效期。当证书剩余有效期不足30天时，将自动触发续期流程。

2. **测试续期功能**：
   可通过以下命令测试续期功能是否正常工作：
   ```bash
   certbot renew --dry-run
   ```
   该命令会模拟续期过程但不会实际更新证书，用于验证配置正确性。

3. **验证续期配置**：
   系统会自动创建定时任务，通常位于`/etc/cron.d/certbot`，确保定期执行续期检查。

此机制确保了证书在到期前能够自动更新，避免因证书过期导致的服务中断。

**Section sources**
- [DEPLOY_ALIYUN.md](file://DEPLOY_ALIYUN.md#L794-L800)

## 强制HTTPS重定向

为确保所有流量都通过加密连接传输，需要配置HTTP到HTTPS的强制重定向：

```bash
# 编辑Nginx配置文件
vim /etc/nginx/sites-available/tinyflow

# 添加以下重定向规则
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

此配置确保所有通过HTTP访问的请求都会被永久重定向到HTTPS，从而强制使用加密连接。重定向状态码为301（永久重定向），有利于搜索引擎优化（SEO）。

**Section sources**
- [DEPLOY_ALIYUN.md](file://DEPLOY_ALIYUN.md#L803-L815)

## 安全组配置

在阿里云服务器上部署HTTPS服务时，必须正确配置安全组规则：

1. **入方向规则**：
   - **TCP 80端口**：允许HTTP访问（用于ACME挑战验证）
   - **TCP 443端口**：允许HTTPS访问
   - **TCP 22端口**：允许SSH管理（建议限制为特定IP）

2. **配置注意事项**：
   - 80端口必须开放，因为Let's Encrypt的ACME协议需要通过HTTP验证域名所有权
   - 443端口是HTTPS服务的标准端口，必须开放以提供加密服务
   - 不应开放数据库（3306）、Redis（6379）等内部服务端口

这些安全组规则确保了服务的可访问性同时维护了服务器的安全性。

**Section sources**
- [DEPLOY_ALIYUN.md](file://DEPLOY_ALIYUN.md#L514-L527)

## HTTPS的重要性

启用HTTPS对TinyFlow服务具有重要意义：

1. **安全性提升**：
   - 所有API请求和前端资源通过加密传输，防止中间人攻击（MITM）
   - 保护用户隐私和敏感数据（如认证令牌）
   - 防止内容被篡改或注入恶意代码

2. **可信度增强**：
   - 浏览器地址栏显示安全锁标志，增加用户信任
   - 避免浏览器标记为"不安全网站"
   - 符合现代Web安全标准和最佳实践

3. **功能兼容性**：
   - 某些现代Web API（如地理位置、推送通知）要求HTTPS环境
   - 提升SEO排名，搜索引擎优先索引HTTPS网站
   - 支持HTTP/2协议，提升页面加载性能

通过部署Let's Encrypt免费SSL证书，TinyFlow能够在不增加成本的情况下实现全面的传输层安全保护。

**Section sources**
- [DEPLOY_ALIYUN.md](file://DEPLOY_ALIYUN.md#L778-L815)