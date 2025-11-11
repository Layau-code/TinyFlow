<template>
  <div>
    <component :is="comp" v-bind="compProps" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import EChartsPie from './charts/EChartsPie.vue'
import PieDonut from './charts/PieDonut.vue'

const props = defineProps({ type: { type: String, default: 'pie' }, data: { type: Array, default: () => [] }, colors: { type: Array, default: () => [] }, engine: { type: String, default: 'native' } })

// 现在生成 comp 映射（采用独立 SFC，避免运行时模板编译）
const comp = computed(() => ({ pie: (props.engine==='echarts' ? EChartsPie : PieDonut) }[props.type] || PieDonut))
const compProps = computed(() => ({ data: props.data, colors: props.colors }))
</script>

<style scoped>
svg { transform: rotate(-90deg); }
</style>