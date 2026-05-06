<template>
  <div class="min-h-screen pt-14" style="background-color:var(--tf-bg-page)">
    <!-- Hero Section -->
    <section class="hero py-20 px-6">
      <div class="max-w-4xl mx-auto text-center">
        <h1 class="text-5xl font-bold mb-6" style="color:#1F2329">
          关于 TinyFlow
        </h1>
        <div class="text-xl" style="color:#646A73;line-height:1.8">
          世界上没有两个完全相同的雨滴，就像这个系统中的每一个短链接都是独一无二的
        </div>
      </div>
    </section>

    <!-- Philosophy Section -->
    <section class="py-16 px-6">
      <div class="max-w-5xl mx-auto">
        <div class="fs-card p-8 md:p-12">
          <h2 class="text-3xl font-bold mb-6 text-center" style="color:#1F2329">
            为什么叫 TinyFlow？
          </h2>
          
          <div class="space-y-8">
            <!-- 雨滴的独特性 -->
            <div class="flex items-start gap-4">
              <div class="shrink-0 w-12 h-12 rounded-full flex items-center justify-center" style="background:linear-gradient(135deg, #3370FF 0%, #38BDF8 100%)">
                <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v3.586L7.707 9.293a1 1 0 00-1.414 1.414l3 3a1 1 0 001.414 0l3-3a1 1 0 00-1.414-1.414L11 10.586V7z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="flex-1">
                <h3 class="text-xl font-semibold mb-3" style="color:#1F2329">微小而独特 (Tiny)</h3>
                <p class="text-base leading-relaxed" style="color:#646A73">
                  每一滴雨水的形状、大小、轨迹都不尽相同，即使在显微镜下，你也找不到两滴完全一样的雨滴。
                  这种自然界的独特性，恰如其分地诠释了短链接系统的核心特征——<strong>每一个短链接都拥有唯一的标识</strong>。
                  通过<strong>号段模式分布式 ID 生成器 + Hashids 算法 + Base58 编码</strong>，
                  TinyFlow 能稳定生成 6 位无冲突短码，单机 TPS 超过 10000+，确保每个短链接都是独一无二的存在。
                </p>
              </div>
            </div>

            <!-- 汇聚成流 -->
            <div class="flex items-start gap-4">
              <div class="shrink-0 w-12 h-12 rounded-full flex items-center justify-center" style="background:linear-gradient(135deg, #3370FF 0%, #38BDF8 100%)">
                <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M2 11a1 1 0 011-1h2a1 1 0 011 1v5a1 1 0 01-1 1H3a1 1 0 01-1-1v-5zM8 7a1 1 0 011-1h2a1 1 0 011 1v9a1 1 0 01-1 1H9a1 1 0 01-1-1V7zM14 4a1 1 0 011-1h2a1 1 0 011 1v12a1 1 0 01-1 1h-2a1 1 0 01-1-1V4z" />
                </svg>
              </div>
              <div class="flex-1">
                <h3 class="text-xl font-semibold mb-3" style="color:#1F2329">汇聚成流 (Flow)</h3>
                <p class="text-base leading-relaxed" style="color:#646A73">
                  雨滴落下后，它们会汇聚成溪流、河流，最终奔向大海。这种<strong>从微小到宏大的汇聚过程</strong>，
                  象征着短链接系统中数据的流动与聚合。每一次点击、每一个访问，都如同雨滴汇入河流，
                  最终形成可供分析的<strong>数据洪流</strong>。TinyFlow 采用 <strong>RabbitMQ 消息队列</strong>异步处理点击统计，
                  消费者每 2 秒批量聚合刷库，系统 TPS 提升 5 倍，消息丢失率低于 0.01%。
                  正如涓涓细流汇成江河，每一次点击数据最终都汇聚成完整的统计图表。
                </p>
              </div>
            </div>

            <!-- 持续流动 -->
            <div class="flex items-start gap-4">
              <div class="shrink-0 w-12 h-12 rounded-full flex items-center justify-center" style="background:linear-gradient(135deg, #3370FF 0%, #38BDF8 100%)">
                <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="flex-1">
                <h3 class="text-xl font-semibold mb-3" style="color:#1F2329">持续流动</h3>
                <p class="text-base leading-relaxed" style="color:#646A73">
                  河流永不停息，数据也在不断流动。TinyFlow 构建了 <strong>Caffeine (L1) + Redis (L2) + MySQL</strong> 三级缓存体系，
                  缓存命中率达 85%+，数据库 IO 降低 70%。启动时预热 Top 1000 热点短链到本地缓存，
                  确保高频访问的数据如流水般顺畅。配合 <strong>Resilience4j</strong> 熔断降级机制，
                  即使 Redis 或 MySQL 故障，系统依然能够平稳运行，<strong>数据流动永不中断</strong>。
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Core Features -->
    <section class="py-16 px-6" style="background:linear-gradient(180deg, transparent 0%, rgba(51,112,255,0.03) 100%)">
      <div class="max-w-5xl mx-auto">
        <h2 class="text-3xl font-bold mb-12 text-center" style="color:#1F2329">
          核心技术特性
        </h2>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Feature 1 -->
          <div class="fs-card p-6 hover:shadow-lg transition">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background:rgba(51,112,255,0.1)">
                <span class="text-2xl">🚀</span>
              </div>
              <h3 class="text-lg font-semibold" style="color:#1F2329">高性能架构</h3>
            </div>
            <p class="text-sm leading-relaxed" style="color:#646A73">
              压测验证可稳定支撑 3000+ QPS，P99 延迟小于 100ms。
              采用号段模式预分配 ID + 多级缓存 + 消息队列异步解耦，
              确保系统在高并发场景下依然丝滑流畅。
            </p>
          </div>

          <!-- Feature 2 -->
          <div class="fs-card p-6 hover:shadow-lg transition">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background:rgba(51,112,255,0.1)">
                <span class="text-2xl">🔐</span>
              </div>
              <h3 class="text-lg font-semibold" style="color:#1F2329">无冲突生成</h3>
            </div>
            <p class="text-sm leading-relaxed" style="color:#646A73">
              双 Buffer 机制异步加载 ID 段，Hashids 算法混淆 ID，
              Base58 编码生成 6 位可读短码，确保每个短链接都是独一无二的，
              就像世界上没有两滴相同的雨水。
            </p>
          </div>

          <!-- Feature 3 -->
          <div class="fs-card p-6 hover:shadow-lg transition">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background:rgba(51,112,255,0.1)">
                <span class="text-2xl">💾</span>
              </div>
              <h3 class="text-lg font-semibold" style="color:#1F2329">多级缓存</h3>
            </div>
            <p class="text-sm leading-relaxed" style="color:#646A73">
              L1 本地缓存 + L2 分布式缓存 + L3 数据库，逐级回填策略。
              缓存预热机制加载热点数据，命中率 85%+，
              数据库压力降低 70%，冷启动延迟降低 60%。
            </p>
          </div>

          <!-- Feature 4 -->
          <div class="fs-card p-6 hover:shadow-lg transition">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background:rgba(51,112,255,0.1)">
                <span class="text-2xl">📊</span>
              </div>
              <h3 class="text-lg font-semibold" style="color:#1F2329">数据洪流</h3>
            </div>
            <p class="text-sm leading-relaxed" style="color:#646A73">
              每一次点击都是一滴雨水，汇聚成数据的河流。
              RabbitMQ 异步统计 + 批量刷库，消息吞吐 5000+/s，
              丢失率低于 0.01%，为你呈现完整的访问趋势与来源分布。
            </p>
          </div>

          <!-- Feature 5 -->
          <div class="fs-card p-6 hover:shadow-lg transition">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background:rgba(51,112,255,0.1)">
                <span class="text-2xl">🛡️</span>
              </div>
              <h3 class="text-lg font-semibold" style="color:#1F2329">熔断降级</h3>
            </div>
            <p class="text-sm leading-relaxed" style="color:#646A73">
              Resilience4j 保障服务稳定性，失败率超 50% 或慢调用超 80% 自动熔断。
              Redis 熔断 → MySQL，MySQL 熔断 → 默认值，
              确保数据流动永不中断。
            </p>
          </div>

          <!-- Feature 6 -->
          <div class="fs-card p-6 hover:shadow-lg transition">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background:rgba(51,112,255,0.1)">
                <span class="text-2xl">📈</span>
              </div>
              <h3 class="text-lg font-semibold" style="color:#1F2329">全链路监控</h3>
            </div>
            <p class="text-sm leading-relaxed" style="color:#646A73">
              Prometheus 采集指标，Grafana 可视化监控大盘，
              Zipkin 追踪全链路耗时。每一滴数据的流动轨迹都清晰可见，
              性能瓶颈一目了然。
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Tech Stack -->
    <section class="py-16 px-6">
      <div class="max-w-5xl mx-auto">
        <h2 class="text-3xl font-bold mb-12 text-center" style="color:#1F2329">
          技术栈
        </h2>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <!-- Frontend -->
          <div class="fs-card p-6">
            <h3 class="text-lg font-semibold mb-4" style="color:#1F2329">前端</h3>
            <div class="space-y-2 text-sm" style="color:#646A73">
              <div>✓ Vue 3 + Composition API</div>
              <div>✓ Vite 5 极速构建</div>
              <div>✓ Tailwind CSS 原子化</div>
              <div>✓ ECharts 数据可视化</div>
              <div>✓ Vue I18n 国际化</div>
            </div>
          </div>

          <!-- Backend -->
          <div class="fs-card p-6">
            <h3 class="text-lg font-semibold mb-4" style="color:#1F2329">后端</h3>
            <div class="space-y-2 text-sm" style="color:#646A73">
              <div>✓ Spring Boot 3.5.7</div>
              <div>✓ Spring Data JPA</div>
              <div>✓ MySQL 8.0 持久化</div>
              <div>✓ Redis 7 分布式缓存</div>
              <div>✓ RabbitMQ 消息队列</div>
            </div>
          </div>

          <!-- Infrastructure -->
          <div class="fs-card p-6">
            <h3 class="text-lg font-semibold mb-4" style="color:#1F2329">基础设施</h3>
            <div class="space-y-2 text-sm" style="color:#646A73">
              <div>✓ Caffeine 本地缓存</div>
              <div>✓ Resilience4j 熔断器</div>
              <div>✓ Prometheus 指标采集</div>
              <div>✓ Grafana 监控大盘</div>
              <div>✓ Zipkin 链路追踪</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Team -->
    <section class="py-16 px-6" style="background:linear-gradient(180deg, rgba(51,112,255,0.03) 0%, transparent 100%)">
      <div class="max-w-4xl mx-auto text-center">
        <h2 class="text-3xl font-bold mb-6" style="color:#1F2329">
          关于作者
        </h2>
        <div class="fs-card p-8 inline-block">
          <div class="w-24 h-24 rounded-full mx-auto mb-4 flex items-center justify-center text-4xl" style="background:linear-gradient(135deg, #3370FF 0%, #38BDF8 100%);color:white">
            L
          </div>
          <h3 class="text-xl font-semibold mb-2" style="color:#1F2329">Layau</h3>
          <p class="text-sm mb-4" style="color:#646A73">全栈开发者 / 高并发系统架构爱好者</p>
          <div class="flex items-center justify-center gap-4">
            <a href="https://github.com/Layau-code" target="_blank" class="text-sm hover:underline" style="color:#3370FF">
              GitHub
            </a>
            <span style="color:#E5E7EB">|</span>
            <a href="mailto:18970931397@163.com" class="text-sm hover:underline" style="color:#3370FF">
              Email
            </a>
          </div>
        </div>
        <p class="mt-8 text-base leading-relaxed max-w-2xl mx-auto" style="color:#646A73">
          TinyFlow 是一个学习高并发系统设计的实战项目，适合作为面试分享或技术交流。
          希望这个项目能够帮助更多同学理解分布式系统的设计思路与性能优化实践。
        </p>
      </div>
    </section>

    <!-- CTA -->
    <section class="py-16 px-6">
      <div class="max-w-4xl mx-auto text-center">
        <div class="fs-card p-12">
          <h2 class="text-3xl font-bold mb-4" style="color:#1F2329">
            开始使用 TinyFlow
          </h2>
          <p class="text-lg mb-8" style="color:#646A73">
            加入我们，探索短链接系统的无限可能
          </p>
          <div class="flex items-center justify-center gap-4 flex-wrap">
            <button @click="$router.push('/')" class="fs-btn-primary px-8 py-3">
              立即体验
            </button>
            <a href="https://github.com/Layau-code/TinyFlow" target="_blank" class="fs-btn-secondary px-8 py-3 inline-block">
              查看源码
            </a>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'AboutPage'
}
</script>

<style scoped>
.fs-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 2px 0 rgba(31,35,41,0.08);
  border: 1px solid var(--tf-border);
  transition: all 0.3s ease;
}

.fs-card:hover {
  box-shadow: 0 4px 12px 0 rgba(31,35,41,0.12);
}

.fs-btn-primary {
  background: linear-gradient(135deg, #3370FF 0%, #2B5FE6 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.fs-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(51,112,255,0.3);
}

.fs-btn-secondary {
  background: white;
  color: #3370FF;
  border: 1px solid #3370FF;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
}

.fs-btn-secondary:hover {
  background: rgba(51,112,255,0.05);
  transform: translateY(-2px);
}
</style>
