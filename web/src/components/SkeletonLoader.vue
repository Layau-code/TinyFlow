<template>
  <div :class="wrapperClass">
    <div v-for="n in count" :key="n" class="bg-gray-200 animate-pulse rounded" :style="{height: height+'px'}"></div>
  </div>
</template>

<script setup>
const props = defineProps({ count: { type: Number, default: 1 }, height: { type: Number, default: 160 }, vertical: { type: Boolean, default: true } })
const wrapperClass = props.vertical ? 'space-y-2' : 'flex gap-2'
</script>

<style scoped></style>