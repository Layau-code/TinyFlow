# 性能压测与可视化优化流程

目标：形成“压测 → 观测 → 瓶颈定位 → 优化 → 回归验证”的闭环，并以图表呈现进展。

## SLO 与关键指标
- 延迟：`p95 < 300ms`（重定向接口）/ `p95 < 800ms`（创建接口）
- 可用性：错误率 `< 1%`
- 吞吐：重定向 `>= 200 RPS` 稳定
- 资源：CPU/内存占用合理，无长时间 Full GC（后续可加）

## 可视化面板（Grafana）
- RPS（按 `uri`）
- p95 延迟（按 `uri`）
- 错误率（5xx / 总请求）
- 重定向延迟 p95 专项

导入仪表：`infra/observability/dashboards/shortener-overview.json`

## 压测脚本（k6）
- 路径：`infra/load/k6/shortener.js`
- 逻辑：先批量创建短码（setup），再以常量到达率压测重定向；同时模拟创建接口的突刺流量。
- 运行示例：
  - 启动后端：`mvn spring-boot:run`
  - 启动观测栈：`docker compose -f infra/observability/docker-compose.yml up -d`
  - 运行压测：`k6 run infra/load/k6/shortener.js`（可用 `BASE_URL` 环境变量指定目标）

## 优化流程（迭代清单）
1. 建立基线：运行 k6，记录 Grafana 的 RPS、p95、错误率，以及 Prometheus 查询结果。
2. 定位瓶颈：
   - p95 升高：查看数据库交互、阻塞调用、线程池饱和；
   - 错误率上升：确认异常类型、是否出现超时中断；
   - RPS 波动：是否有队列堆积、连接池限制、GC 抖动。
3. 具体优化：
   - 连接池/Hikari：调整 `maximumPoolSize`、`connectionTimeout`；
   - 控制器与服务：避免阻塞操作；跳转接口优先缓存读，统计异步化；
   - JPA/SQL：必要索引、减少 N+1、选择性字段；
   - 资源隔离：为统计等非核心路径设置线程池与限流，保护重定向；
   - 降级与熔断（可选迭代）：对统计接口加 TimeLimiter/CircuitBreaker。
4. 回归验证：重复压测，监控图表是否接近/达到 SLO；记录优化前后对比图。
5. 文档与复盘：更新本文件与 README 的“变更与效果”小结。

## 注意事项
- Windows/本机 Docker 使用 `host.docker.internal:8080` 作为 Prometheus 抓取目标（已在配置中设置）。
- 若后端端口变更或部署到服务器，请同步修改 `infra/observability/prometheus.yml`。
- 压测请逐步升压，防止对本机资源造成过载；观察 5 分钟滑窗更稳定。