<template>
  <div class="chart-type-switcher">
    <div class="type-group">
      <label v-for="t in types" :key="t.value" class="type-btn">
        <input type="radio" :value="t.value" v-model="internal" />
        {{ t.label }}
      </label>
    </div>
    <div class="engine-switch">
      <button :class="['engine-btn', { active: internalEngine === 'echarts' }]" @click="setEngine('echarts')">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/><path d="M7 12h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
        ECharts
      </button>
      <button :class="['engine-btn', { active: internalEngine === 'antv' }]" @click="setEngine('antv')">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="4" y="4" width="16" height="16" stroke="currentColor" stroke-width="1.5" rx="2"/></svg>
        AntV
      </button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, watch, ref } from 'vue';
export default defineComponent({
  name: 'ChartTypeSwitcher',
  props: { modelValue: { type: String, default: 'line' }, engine: { type: String, default: 'echarts' } },
  emits: ['update:modelValue', 'switch-engine'],
  setup(props, { emit }) {
    const internal = ref(props.modelValue);
    const internalEngine = ref(props.engine || 'echarts');
    const types = [{ value: 'line', label: '折线' }, { value: 'bar', label: '柱状' }, { value: 'area', label: '面积' }, { value: 'pie', label: '饼图' }];
    watch(internal, (v) => emit('update:modelValue', v));
    watch(() => props.modelValue, (v) => internal.value = v);
    watch(() => props.engine, (v) => internalEngine.value = v || 'echarts');

    function setEngine(e: string){
      internalEngine.value = e
      emit('switch-engine', e)
    }

    return { internal, types, internalEngine, setEngine };
  },
});
</script>

<style scoped>
.chart-type-switcher { display:flex; gap:8px; align-items:center; }
.type-group { display:flex; gap:6px; }
.type-btn { font-size:12px; cursor:pointer; }
.type-btn input { margin-right:4px; }
.engine-switch { display:flex; gap:6px; margin-left:8px; }
.engine-btn { padding:6px 8px; border-radius:6px; border:1px solid #e5e7eb; background:#fff; cursor:pointer; font-size:12px; display:flex; gap:6px; align-items:center; }
.engine-btn.active { background:#2563eb; color:white; border-color:transparent }
</style>
