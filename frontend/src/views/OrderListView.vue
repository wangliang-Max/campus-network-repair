<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import SearchBar from '@/components/SearchBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useUserStore } from '@/stores/user'
import { pageOrders } from '@/api/order'
import { getRooms } from '@/api/device'
import { formatTime } from '@/utils/format'

const router = useRouter()
const user = useUserStore()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const rooms = ref([])

const query = reactive({
  page: 1,
  size: 10,
  status: '',
  priority: '',
  keyword: '',
  roomName: '',
})

async function load() {
  loading.value = true
  try {
    const data = await pageOrders({ ...query })
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(query, { page: 1, status: '', priority: '', keyword: '', roomName: '' })
  load()
}

function changePage(page) {
  query.page = page
  load()
}

onMounted(async () => {
  rooms.value = await getRooms()
  load()
})
</script>

<template>
  <div class="page">
    <PageHeader title="报修单" :subtitle="user.isStaff ? '全部工单，可按状态、优先级和机房筛选' : '这里只显示你自己提交的报修单'">
      <template #actions>
        <el-button v-if="['REPORTER', 'ADMIN'].includes(user.role)" type="primary" @click="router.push('/orders/create')">
          新建报修
        </el-button>
      </template>
    </PageHeader>

    <div class="panel">
      <SearchBar @search="query.page = 1; load()" @reset="reset">
        <el-input v-model="query.keyword" placeholder="单号 / 故障描述 / 设备 / 报障人" clearable style="width: 260px" />
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="待受理" value="PENDING" />
          <el-option label="排查中" value="INVESTIGATING" />
          <el-option label="修复中" value="REPAIRING" />
          <el-option label="待确认" value="PENDING_CONFIRM" />
          <el-option label="待补充" value="INFO_NEEDED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-select v-model="query.priority" placeholder="全部优先级" clearable style="width: 130px">
          <el-option label="普通" value="NORMAL" />
          <el-option label="紧急" value="URGENT" />
        </el-select>
        <el-select v-model="query.roomName" placeholder="全部机房" clearable style="width: 160px">
          <el-option v-for="room in rooms" :key="room" :label="room" :value="room" />
        </el-select>
      </SearchBar>

      <el-table v-loading="loading" :data="rows" empty-text="没有符合条件的报修单">
        <el-table-column prop="orderNo" label="单号" width="180" />
        <el-table-column label="设备 / 机房" min-width="190">
          <template #default="{ row }">
            <div>{{ row.deviceName }}</div>
            <div class="muted small">{{ row.roomName }}</div>
          </template>
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
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/orders/${row.id}`)">详情</el-link>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.size"
          :current-page="query.page"
          @current-change="changePage"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.small {
  font-size: 12px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
