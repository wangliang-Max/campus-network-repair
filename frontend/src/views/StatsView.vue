<script setup>
import { computed, onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import { getOverview, getTrend } from '@/api/stats'

const loading = ref(false)
const overview = ref(null)
const trend = ref([])

const maxRoom = computed(() => Math.max(1, ...(overview.value?.byRoom || []).map((item) => item.count)))
const maxTrend = computed(() =>
  Math.max(1, ...trend.value.flatMap((item) => [item.created, item.closed])),
)

async function load() {
  loading.value = true
  try {
    const [o, t] = await Promise.all([getOverview(), getTrend(7)])
    overview.value = o
    trend.value = t
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page" v-loading="loading">
    <PageHeader title="统计报表" subtitle="平均修复时长按状态日志中的关闭时间计算">
      <template #actions>
        <el-button @click="load">刷新</el-button>
      </template>
    </PageHeader>

    <template v-if="overview">
      <div class="stats">
        <StatCard label="报修单总数" :value="overview.totalOrders" />
        <StatCard label="未关闭" :value="overview.openOrders" tone="amber" />
        <StatCard label="已关闭" :value="overview.closedOrders" tone="teal" />
        <StatCard label="平均修复时长" :value="`${overview.avgRepairHours}h`" tone="navy" />
      </div>

      <div class="grid">
        <div class="panel">
          <h3>未关闭单据按机房</h3>
          <div v-if="overview.byRoom?.length" class="bars">
            <div v-for="item in overview.byRoom" :key="item.name" class="bar-row">
              <span class="bar-label">{{ item.name }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: `${(item.count / maxRoom) * 100}%` }" />
              </div>
              <span class="bar-value">{{ item.count }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无未关闭单据" />
        </div>

        <div class="panel">
          <h3>状态分布</h3>
          <div class="status-list">
            <div v-for="item in overview.byStatus" :key="item.name" class="status-item">
              <StatusTag :status="item.name" />
              <strong>{{ item.count }}</strong>
            </div>
          </div>
        </div>
      </div>

      <div class="panel">
        <h3>近 7 天新增与关闭</h3>
        <div class="trend">
          <div v-for="item in trend" :key="item.date" class="trend-col">
            <div class="trend-bars">
              <div class="trend-bar created" :style="{ height: `${(item.created / maxTrend) * 100}%` }" :title="`新增 ${item.created}`" />
              <div class="trend-bar closed" :style="{ height: `${(item.closed / maxTrend) * 100}%` }" :title="`关闭 ${item.closed}`" />
            </div>
            <span class="trend-date">{{ item.date.slice(5) }}</span>
            <span class="trend-num">{{ item.created }} / {{ item.closed }}</span>
          </div>
        </div>
        <div class="legend">
          <span><i class="dot created" />新增</span>
          <span><i class="dot closed" />关闭</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.grid {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 16px;
}

h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #0b2545;
}

.bars {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bar-row {
  display: grid;
  grid-template-columns: 130px 1fr 30px;
  gap: 10px;
  align-items: center;
  font-size: 13px;
}

.bar-track {
  height: 16px;
  background: #f0f4f8;
  border-radius: 8px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #0e7c86;
  border-radius: 8px;
}

.bar-value {
  text-align: right;
  font-weight: 600;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px dashed #e4e7eb;
  padding-bottom: 10px;
}

.trend {
  display: flex;
  gap: 18px;
  align-items: flex-end;
  height: 180px;
  padding: 0 8px;
}

.trend-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  justify-content: flex-end;
  gap: 6px;
}

.trend-bars {
  display: flex;
  gap: 3px;
  align-items: flex-end;
  height: 120px;
  width: 100%;
  justify-content: center;
}

.trend-bar {
  width: 14px;
  border-radius: 3px 3px 0 0;
  min-height: 2px;
}

.created {
  background: #0e7c86;
}

.closed {
  background: #b7791f;
}

.trend-date {
  font-size: 12px;
  color: #52606d;
}

.trend-num {
  font-size: 11px;
  color: #7b8794;
}

.legend {
  display: flex;
  gap: 18px;
  margin-top: 12px;
  font-size: 12px;
  color: #52606d;
}

.dot {
  display: inline-block;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  margin-right: 5px;
}

@media (max-width: 1000px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
