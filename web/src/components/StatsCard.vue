<template>
  <div class="q-card p-6 flex items-center justify-between">
    <div class="flex items-center gap-3">
      <component :is="icon" class="w-5 h-5" :style="{ color: 'var(--brand-primary)' }" />
      <div>
        <div class="q-card-title">{{ label }}</div>
        <div class="q-number">{{ value }}</div>
      </div>
    </div>
    <slot name="action"></slot>
  </div>
</template>

<script setup>
defineProps({ label: String, value: [String, Number], icon: [Object, Function] })
</script>

<style scoped></style>