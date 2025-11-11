<template>
  <div class="qwen-bg fixed inset-0 -z-10 pointer-events-none" aria-hidden="true">
    <Particles id="qwenParticles" :options="options" :init="particlesInit" :loaded="particlesLoaded" />
    <!-- 短码显现（结束态） -->
    <div class="absolute inset-0 flex items-center justify-center">
      <div class="code-reveal font-mono">{{ revealText }}</div>
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { loadFull } from 'tsparticles'

export default defineComponent({
  name: 'QwenParticleBackground',
  data() {
    return {
      container: null,
      revealText: '',
      options: {
        fullScreen: { enable: false },
        background: { color: { value: '#0A1A3F' } },
        fpsLimit: 60,
        detectRetina: true,
        particles: {
          number: { value: 90, density: { enable: true, area: 800 } },
          color: { value: ['#5DA0FF', '#8B7DFF', '#A66BFF'] },
          shape: { type: 'circle' },
          opacity: {
            value: { min: 0.5, max: 0.9 },
            animation: { enable: true, speed: 0.4, minimumValue: 0.25, sync: false }
          },
          size: {
            value: { min: 1, max: 3 },
            random: { enable: true, minimumValue: 1 },
            animation: { enable: true, speed: 2, minimumValue: 0.6, sync: false }
          },
          links: { enable: true, color: { value: '#7E6BFF' }, opacity: 0.12, distance: 120 },
          shadow: { enable: true, blur: 8, color: { value: '#5A6BFF' } },
          twinkle: { particles: { enable: true, frequency: 0.03, opacity: 0.28 } },
          move: {
            enable: true,
            speed: { min: 0.25, max: 0.9 },
            direction: 'none',
            random: true,
            straight: false,
            outModes: { default: 'bounce' },
          }
        },
        interactivity: {
          events: { resize: true, onHover: { enable: true, mode: 'repulse' } },
          modes: { repulse: { distance: 120, duration: 0.4 } }
        },
      },
    }
  },
  methods: {
    async particlesInit(engine) {
      await loadFull(engine)
    },
    particlesLoaded(container) {
      this.container = container
    },
    // 触发压缩：随机 → 聚拢到中心 → 居中横线 + 短码显现
    async playCompression(shortText) {
      this.revealText = ''
      const c = this.container
      if (!c) return
      const base = this.options
      // 阶段2：启用吸附到中心，增强流动感
      const compress = {
        ...base,
        particles: {
          ...base.particles,
          move: {
            ...base.particles.move,
            enable: true,
            speed: { min: 0.8, max: 1.4 },
            direction: 'none',
            attract: { enable: true, rotate: { x: 1200, y: 2400 } },
          }
        }
      }
      // 更新容器配置
      c.options.load(compress)
      await c.refresh()
      c.play()

      // 阶段3：1.6s 后将粒子排成居中横线，并显现短码
      setTimeout(async () => {
        try {
          const w = c.canvas.size.width
          const h = c.canvas.size.height
          const y = h / 2
          const arr = c.particles.array || []
          const count = arr.length
          const spacing = w / (count + 1)
          for (let i = 0; i < count; i++) {
            const p = arr[i]
            const x = spacing * (i + 1)
            p.position.x = x
            p.position.y = y
            // 停止速度，形成整齐有序
            if (p.velocity) {
              p.velocity.horizontal = 0
              p.velocity.vertical = 0
            }
          }
          // 冻结移动，避免抖动
          const stop = {
            ...compress,
            particles: {
              ...compress.particles,
              move: { ...compress.particles.move, enable: false }
            }
          }
          c.options.load(stop)
          await c.refresh()
          c.pause()
        } catch (e) {
          // 失败时降速并直接显现文案作为退化方案
          const fallback = {
            ...compress,
            particles: { ...compress.particles, move: { ...compress.particles.move, speed: { min: 0.05, max: 0.1 } } }
          }
          c.options.load(fallback)
          await c.refresh()
        }
        this.revealText = String(shortText || '').trim()
      }, 1600)
    },
  }
})
</script>

<style scoped>
.qwen-bg {
  background:
    radial-gradient(900px 500px at 15% 25%, rgba(79,157,255,0.14) 0%, rgba(79,157,255,0.08) 35%, rgba(79,157,255,0.02) 70%, transparent 80%),
    radial-gradient(1000px 600px at 80% 30%, rgba(166,107,255,0.12) 0%, rgba(166,107,255,0.06) 40%, rgba(166,107,255,0.02) 70%, transparent 80%),
    linear-gradient(135deg, #0E173A 0%, #1A2C6E 45%, #3C2FA3 75%, #6B2FBF 100%);
}
.code-reveal { color: #FFFFFF; text-shadow: 0 0 16px rgba(79,157,255,0.6), 0 0 6px rgba(122,107,255,0.4); font-size: clamp(18px, 4vw, 28px); letter-spacing: 0.4px; }
</style>