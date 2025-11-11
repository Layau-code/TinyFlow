<template>
  <div class="flex items-center gap-1" title="Language">
    <button
      class="px-2 h-8 rounded-md border text-sm"
      :class="{ 'bg-[#111] text-white border-[#333]': locale==='zh' }"
      @click="change('zh')"
    >中</button>
    <button
      class="px-2 h-8 rounded-md border text-sm"
      :class="{ 'bg-[#111] text-white border-[#333]': locale==='en' }"
      @click="change('en')"
    >EN</button>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { setLocale } from '../i18n'

const { locale } = useI18n({ useScope: 'global' })

function change(l) {
  if (locale.value !== l) {
    setLocale(l)
  }
}
</script>

<style scoped>
button { border-color: #E5E7EB; color: #374151; background: #fff; }
button:hover { opacity: 0.9 }
</style>