<template>
  <!-- ...existing code... -->
  <div class="detail-page">
    <div class="header">
      <button @click="goBack">返回</button>
      <h2>{{ title }}</h2>
      <div class="actions">
        <button @click="exportCSV">导出 CSV</button>
        <button @click="refresh">刷新</button>
      </div>
    </div>

    <div class="detail-main">
      <chart-card :title="title" :chart-data="mainChart" :chart-type="chartType" @point-click="onDrill" />
      <div class="side-charts">
        <chart-card v-for="g in groupCharts" :key="g.key" :title="g.title" :chart-data="g.data" chart-type="bar" @point-click="onDrill" />
      </div>
    </div>

    <div class="events-table">
      <table>
        <thead><tr><th>时间</th><th>事件</th><th>指标</th></tr></thead>
        <tbody>
          <tr v-for="row in rows" :key="row.id"><td>{{ row.time }}</td><td>{{ row.msg }}</td><td>{{ row.value }}</td></tr>
        </tbody>
      </table>
      <pagination :page.sync="page" :total="totalPages" @change="loadRows" />
    </div>
  </div>
  <!-- ...existing code... -->
</template>

<script lang="ts">
import { ref, onMounted } from 'vue';
import ChartCard from '@/components/ChartCard.vue';
import { getDetail } from '@/services/statsService';

export default {
  name: 'StatisticsDetail',
  components: { ChartCard },
  setup(props: any, { attrs }: any) {
    const route = require('vue-router').useRoute();
    const router = require('vue-router').useRouter();
    const id = route.params.id;
    const title = ref('统计详情');
    const mainChart = ref({ x: [], series: [] });
    const groupCharts = ref<Array<any>>([]);
    const rows = ref<Array<any>>([]);
    const page = ref(1);
    const pageSize = 20;
    const totalPages = ref(1);
    const chartType = ref('line');

    const loadDetail = async () => {
      try {
        const res = await getDetail({ id, page: page.value, pageSize });
        title.value = res.title || title.value;
        mainChart.value = res.mainChart;
        groupCharts.value = res.groups || [];
        rows.value = res.rows || [];
        totalPages.value = Math.ceil((res.total || 0) / pageSize);
      } catch (e) {
        console.error(e);
      }
    };

    const loadRows = async () => {
      await loadDetail();
    };

    const goBack = () => router.back();
    const refresh = () => loadDetail();
    const exportCSV = () => {
      // 简单 CSV 导出
      const csv = [ ['time','msg','value'], ...rows.value.map(r => [r.time, `"${(r.msg||'').replace(/"/g,'""')}"`, r.value]) ].map(r=>r.join(',')).join('\n');
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a'); a.href = url; a.download = `${title.value || 'export'}.csv`; a.click(); URL.revokeObjectURL(url);
    };

    const onDrill = (payload: any) => {
      // 点击继续钻取或筛选
      // ...existing code...
      console.log('drill', payload);
    };

    onMounted(loadDetail);

    return { title, mainChart, groupCharts, rows, page, totalPages, chartType, goBack, refresh, exportCSV, onDrill, loadRows };
  },
};
</script>

<style scoped>
.detail-page .header { display:flex; align-items:center; justify-content:space-between; gap:8px; }
.detail-main { display:flex; gap:12px; }
.side-charts { display:flex; flex-direction:column; gap:8px; width:360px; }
.events-table { margin-top:16px; }
</style>

