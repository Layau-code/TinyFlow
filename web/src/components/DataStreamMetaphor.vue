<template>
  <div class="tf-metaphor relative w-full h-full pointer-events-none select-none">
    <canvas ref="canvas" class="absolute inset-0"></canvas>
    <div class="absolute inset-0 flex items-center justify-center">
      <div class="tf-reveal-code" :style="codeStyle">{{ revealText }}</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataStreamMetaphor',
  props: {
    longUrl: { type: String, default: '' },
    shortUrl: { type: String, default: '' },
  },
  data() {
    return {
      ctx: null,
      canvas: null,
      dpr: 1,
      rafId: 0,
      phase: 'idle', // idle | disperse | collapse | flash | reveal
      particles: [],
      center: { x: 0, y: 0 },
      flash: { active: false, alpha: 0 },
      revealText: '',
      startedAt: 0,
    }
  },
  computed: {
    codeStyle() {
      const visible = this.phase === 'reveal' && this.revealText
      return {
        opacity: visible ? 1 : 0,
        transform: visible ? 'scale(1)' : 'scale(0.96)',
        transition: 'opacity 380ms ease-out, transform 500ms cubic-bezier(0.22, 1, 0.36, 1)'
      }
    }
  },
  mounted() {
    this.canvas = this.$refs.canvas
    this.ctx = this.canvas.getContext('2d')
    this.resize()
    window.addEventListener('resize', this.resize)
    // 初始背景微弱数据流
    this.spawnParticles(this.longUrl || 'https://example.com/...')
    this.phase = 'disperse'
    this.loop()
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.resize)
    cancelAnimationFrame(this.rafId)
  },
  methods: {
    play(longUrl, shortUrl) {
      // 启动一次完整序列：散开 → 内聚闪点 → 短码显现
      const L = String(longUrl || this.longUrl || 'https://example.com/...')
      const S = String(shortUrl || this.shortUrl || 'bit.ly/xyz')
      this.revealText = ''
      this.spawnParticles(L)
      this.phase = 'disperse'
      this.startedAt = performance.now()
      // 1.2s 后进入 collapse
      setTimeout(() => { this.phase = 'collapse' }, 1200)
      // 2.3s 后闪点
      setTimeout(() => { this.phase = 'flash'; this.flash.active = true; this.flash.alpha = 1 }, 2300)
      // 2.7s 后显现短码
      setTimeout(() => { this.phase = 'reveal'; this.revealText = S }, 2700)
    },
    resize() {
      const el = this.canvas.parentElement
      const w = el.clientWidth || 800
      const h = el.clientHeight || 320
      const dpr = Math.min(2, window.devicePixelRatio || 1)
      this.dpr = dpr
      this.canvas.width = Math.floor(w * dpr)
      this.canvas.height = Math.floor(h * dpr)
      this.canvas.style.width = w + 'px'
      this.canvas.style.height = h + 'px'
      this.center.x = this.canvas.width / 2
      this.center.y = this.canvas.height / 2
    },
    spawnParticles(text) {
      const chars = Array.from(text)
      const count = Math.min(260, Math.max(120, chars.length * 3))
      const W = this.canvas.width, H = this.canvas.height
      const arr = []
      for (let i = 0; i < count; i++) {
        const c = chars[i % chars.length]
        const x = Math.random() * W
        const y = Math.random() * H
        const a = Math.random() * Math.PI * 2
        const speed = 0.6 + Math.random() * 1.6
        arr.push({
          c,
          x, y,
          vx: Math.cos(a) * speed,
          vy: Math.sin(a) * speed,
          size: 0.8 + Math.random() * 1.5,
          hue: 230 + Math.random() * 40, // 蓝紫色系
          life: 0,
          arrived: false,
        })
      }
      this.particles = arr
    },
    loop() {
      this.rafId = requestAnimationFrame(this.loop)
      const ctx = this.ctx
      if (!ctx) return
      const W = this.canvas.width, H = this.canvas.height
      // 背景淡淡拖影，暗科技感
      ctx.fillStyle = 'rgba(4,6,10,0.08)'
      ctx.fillRect(0, 0, W, H)
      ctx.globalCompositeOperation = 'lighter'

      const cx = this.center.x, cy = this.center.y
      for (const p of this.particles) {
        if (this.phase === 'disperse') {
          // 轻微涡旋 + 噪声
          const swirl = 0.015
          const dx = p.x - cx, dy = p.y - cy
          p.vx += -dy * swirl
          p.vy += dx * swirl
        } else if (this.phase === 'collapse') {
          // 朝中心缓慢吸附
          const dx = cx - p.x, dy = cy - p.y
          const dist = Math.hypot(dx, dy) || 1
          const force = Math.min(1.6, 0.06 + 80 / dist)
          p.vx += dx * force * 0.0018
          p.vy += dy * force * 0.0018
          if (dist < 12) p.arrived = true
        }
        // 更新位置与阻尼
        p.x += p.vx
        p.y += p.vy
        p.vx *= 0.985
        p.vy *= 0.985
        // 边界轻微回弹
        if (p.x < 0 || p.x > W) p.vx *= -0.9
        if (p.y < 0 || p.y > H) p.vy *= -0.9
        // 绘制发光粒子与轨迹
        const grad = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, 6 * this.dpr)
        grad.addColorStop(0, `hsla(${p.hue}, 90%, 70%, 0.85)`)
        grad.addColorStop(1, `hsla(${p.hue}, 90%, 50%, 0.0)`)
        ctx.fillStyle = grad
        ctx.beginPath()
        ctx.arc(p.x, p.y, (p.size + (this.phase === 'collapse' ? 0.6 : 0)) * this.dpr, 0, Math.PI * 2)
        ctx.fill()
        // 速度线（数据流感）
        ctx.strokeStyle = `hsla(${p.hue}, 100%, 70%, 0.18)`
        ctx.lineWidth = 1 * this.dpr
        ctx.beginPath()
        ctx.moveTo(p.x, p.y)
        ctx.lineTo(p.x - p.vx * 2.4, p.y - p.vy * 2.4)
        ctx.stroke()
      }

      // 闪点效果
      if (this.flash.active) {
        this.flash.alpha *= 0.92
        const r = Math.max(W, H) * 0.25
        const g = ctx.createRadialGradient(cx, cy, 0, cx, cy, r)
        g.addColorStop(0, `rgba(180,200,255,${0.25 * this.flash.alpha})`)
        g.addColorStop(1, `rgba(180,200,255,0)`)
        ctx.fillStyle = g
        ctx.beginPath(); ctx.arc(cx, cy, r, 0, Math.PI * 2); ctx.fill()
        if (this.flash.alpha < 0.02) this.flash.active = false
      }

      ctx.globalCompositeOperation = 'source-over'
    },
  }
}
</script>

<style scoped>
.tf-metaphor { background: radial-gradient(1200px 600px at 50% 50%, #0b0f18 0%, #070a12 40%, #05070d 100%); border-radius: 16px; overflow: hidden; }
.tf-reveal-code { font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, "Liberation Mono", monospace; font-size: clamp(20px, 4vw, 28px); letter-spacing: 0.6px; color: #cfe3ff; text-shadow: 0 0 18px rgba(120,140,255,0.65), 0 0 6px rgba(120,140,255,0.45); filter: drop-shadow(0 4px 8px rgba(0,0,0,0.35)); }
</style>