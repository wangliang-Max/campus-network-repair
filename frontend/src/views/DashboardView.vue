<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useUserStore } from '@/stores/user'
import { getOverview } from '@/api/stats'
import { myOrders, pageOrders } from '@/api/order'
import { formatTime } from '@/utils/format'

const router = useRouter()
const user = useUserStore()
const loading = ref(false)
const overview = ref(null)
const recent = ref([])

const cards = computed(() => {
  if (user.isStaff && overview.value) {
    return [
      { label: '报修单总数', value: overview.value.totalOrders, tone: 'navy' },
      { label: '未关闭', value: overview.value.openOrders, tone: 'amber' },
      { label: '已关闭', value: overview.value.closedOrders, tone: 'teal' },
      { label: '平均修复时长', value: `${overview.value.avgRepairHours}h`, tone: 'navy' },
    ]
  }
  const list = recent.value
  return [
    { label: '我的报修', value: list.length, tone: 'navy' },
    { label: '处理中', value: list.filter((item) => !['CLOSED'].includes(item.status)).length, tone: 'amber' },
    { label: '已关闭', value: list.filter((item) => item.status === 'CLOSED').length, tone: 'teal' },
  ]
})

async function load() {
  loading.value = true
  try {
    if (user.isStaff) {
      overview.value = await getOverview()
      const data = await pageOrders({ page: 1, size: 6 })
      recent.value = data.records
    } else {
      recent.value = await myOrders()
    }
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader
      :title="`${user.displayName}，${user.isStaff ? '今日工单概览' : '你的报修进度'}`"
      :subtitle="user.isStaff ? '技术员与管理员可以看到全部工单的实时状态' : '提交后可在报修单详情查看处理轨迹'"
    >
      <template #actions>
        <el-button v-if="['REPORTER', 'ADMIN'].includes(user.role)" type="primary" @click="router.push('/orders/create')">
          新建报修
        </el-button>
        <el-button @click="load">刷新</el-button>
      </template>
    </PageHeader>

    <el-skeleton v-if="loading" :rows="4" animated />
    <template v-else>
      <div class="stats">
        <StatCard v-for="card in cards" :key="card.label" v-bind="card" />
      </div>

      <div class="panel">
        <div class="panel-head">
          <h3>{{ user.isStaff ? '最近工单' : '我的最近报修' }}</h3>
          <el-link type="primary" @click="router.push('/orders')">查看全部</el-link>
        </div>
        <el-table :data="recent" empty-text="暂无报修单">
          <el-table-column prop="orderNo" label="单号" width="180" />
          <el-table-column label="设备 / 机房" min-width="180">
            <template #default="{ row }">{{ row.deviceName }} · {{ row.roomName }}</template>
          </el-table-column>
          <el-table-column v-if="user.isStaff" prop="reporterName" label="报障人" width="100" />
          <el-table-column label="优先级" width="90">
            <template #default="{ row }">
              <StatusTag :status="row.priority" :label="row.priorityLabel" />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <StatusTag :status="row.status" :label="row.statusLabel" />
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-link type="primary" @click="router.push(`/orders/${row.id}`)">详情</el-link>
            </template>
          </el-table-column>
        </el-table>
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

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

h3 {
  margin: 0;
  font-size: 17px;
  color: #0b2545;
}
</style>
