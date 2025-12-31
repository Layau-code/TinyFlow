<template>
  <!-- ...existing code... -->
  <div class="stats-page">
    <div class="controls">
      <!-- 时间范围、指标、分组、图表类型 -->
      <!-- ...existing code... -->
      <date-range-picker v-model="range" @change="onRangeChange" />
      <select v-model="metric" @change="fetchSummary">
        <option value="count">Count</option>
        <option value="avg_latency">Avg Latency</option>
        <option value="error_rate">Error Rate</option>
      </select>
      <select v-model="groupBy" @change="fetchSummary">
        <option value="none">No Group</option>
        <option value="service">Service</option>
        <option value="endpoint">Endpoint</option>
      </select>
      <chart-type-switcher v-model:chartType="chartType" />
      <button @click="fetchSummary" :disabled="loading">刷新</button>
    </div>

    <div v-if="loading" class="skeleton-grid">
      <!-- ...existing code... skeleton placeholders ... -->
    </div>

    <div v-else class="charts-grid">
      <chart-card
        v-for="card in cards"
        :key="card.key"
        :title="card.title"
        :chart-data="card.data"
        :chart-type="chartType"
        @point-click="onPointClick"
      />
    </div>

    <pagination v-if="totalPages>1" :page.sync="page" :total="totalPages" @change="fetchSummary" />
  </div>
  <!-- ...existing code... -->
</template>

<script lang="ts">
import { ref, reactive, watch, onMounted } from 'vue';
// ...existing imports...
import ChartCard from '@/components/ChartCard.vue';
import ChartTypeSwitcher from '@/components/ChartTypeSwitcher.vue';
import { getSummary } from '@/services/statsService';

export default {
  name: 'StatisticsPage',
  components: { ChartCard, ChartTypeSwitcher /* ...existing... */ },
  setup() {
    const range = ref({ from: null, to: null });
    const metric = ref('count');
    const groupBy = ref('none');
    const chartType = ref('line');
    const loading = ref(false);
    const page = ref(1);
    const pageSize = 8;
    const totalPages = ref(1);
    const cards = ref<Array<any>>([]);

    const fetchSummary = async () => {
      loading.value = true;
      try {
        const res = await getSummary({ range: range.value, metric: metric.value, groupBy: groupBy.value, page: page.value, pageSize });
        // map API response to cards: title, key, data {x:[], series:[]}
        cards.value = res.items.map((it: any) => ({
          key: it.id,
          title: it.name,
          data: it.chart, // assume backend returns { x:[], series: [{name, data}] }
        }));
        totalPages.value = Math.ceil(res.total / pageSize);
      } catch (e) {
        // ...existing error handling...
        console.error(e);
      } finally {
        loading.value = false;
      }
    };

    const onRangeChange = () => {
      // 防抖或立即触发
      fetchSummary();
    };

    const onPointClick = (payload: any) => {
      // 点击图表点跳转详情页，传参或路由 state
      const id = payload?.seriesId || payload?.key;
      if (id) {
        // ...existing router usage...
        // eslint-disable-next-line @typescript-eslint/no-var-requires
        const router = require('vue-router').useRouter();
        router.push({ name: 'StatisticsDetail', params: { id } });
      }
    };

    onMounted(fetchSummary);

    return { range, metric, groupBy, chartType, loading, cards, fetchSummary, onPointClick, page, totalPages, onRangeChange };
  },
};
</script>

<style scoped>
/* ...existing styles... */
.stats-page .controls { display:flex; gap:8px; align-items:center; margin-bottom:12px; }
.charts-grid { display:grid; grid-template-columns: repeat(auto-fill,minmax(320px,1fr)); gap:12px; }
.skeleton-grid { /* ...existing... */ }
</style>

