<template>
  <button :class="btnClass" @click="onCopy">
    <span v-if="copied">{{ $t('actions.copied') }}！</span>
    <span v-else>{{ $t('actions.copy') }}</span>
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { useClipboard } from '../composables/useStats'
const props = defineProps({ text: { type: String, required: true }, size: { type: String, default: 'sm' } })
const { copied, copy } = useClipboard()
const btnClass = computed(() => `px-3 h-9 rounded-lg ${copied.value ? 'bg-[#00B976] text-white' : 'bg-white border'} shadow`)
async function onCopy(){ await copy(props.text) }
</script>

<style scoped></style>