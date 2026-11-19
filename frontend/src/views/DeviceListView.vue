<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import SearchBar from '@/components/SearchBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import DeviceFormDialog from '@/components/DeviceFormDialog.vue'
import { useUserStore } from '@/stores/user'
import { deleteDevice, getRooms, pageDevices, updateDeviceStatus } from '@/api/device'
import { formatTime } from '@/utils/format'

const user = useUserStore()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const rooms = ref([])
const dialogVisible = ref(false)
const editing = ref(null)

const query = reactive({ page: 1, size: 10, keyword: '', roomName: '', status: '' })

async function load() {
  loading.value = true
  try {
    const data = await pageDevices({ ...query })
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(query, { page: 1, keyword: '', roomName: '', status: '' })
  load()
}

function openCreate() {
  editing.value = null
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = row
  dialogVisible.value = true
}

async function changeStatus(row, status) {
  await updateDeviceStatus(row.id, status)
  ElMessage.success('设备状态已更新')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除设备 ${row.deviceCode} 吗？`, '删除设备', { type: 'warning' })
  await deleteDevice(row.id)
  ElMessage.success('设备已删除')
  load()
}

onMounted(async () => {
  rooms.value = await getRooms()
  load()
})
</script>

<template>
  <div class="page">
    <PageHeader title="设备管理" subtitle="机房设备档案；技术员只读，管理员可以维护">
      <template #actions>
        <el-button v-if="user.isAdmin" type="primary" @click="openCreate">新增设备</el-button>
      </template>
    </PageHeader>

    <div class="panel">
      <SearchBar @search="query.page = 1; load()" @reset="reset">
        <el-input v-model="query.keyword" placeholder="设备编号或名称" clearable style="width: 220px" />
        <el-select v-model="query.roomName" placeholder="全部机房" clearable style="width: 170px">
          <el-option v-for="room in rooms" :key="room" :label="room" :value="room" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="正常" value="NORMAL" />
          <el-option label="故障" value="FAULT" />
          <el-option label="维修中" value="REPAIRING" />
        </el-select>
      </SearchBar>

      <el-table v-loading="loading" :data="rows" empty-text="暂无设备">
        <el-table-column prop="deviceCode" label="设备编号" width="140" />
        <el-table-column prop="deviceName" label="设备名称" min-width="150" />
        <el-table-column prop="roomName" label="机房" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" :label="row.statusLabel" />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="建档时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column v-if="user.isAdmin" label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-link type="primary" @click="openEdit(row)">编辑</el-link>
            <el-dropdown style="margin: 0 10px" @command="(status) => changeStatus(row, status)">
              <el-link type="warning">状态</el-link>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="NORMAL">正常</el-dropdown-item>
                  <el-dropdown-item command="FAULT">故障</el-dropdown-item>
                  <el-dropdown-item command="REPAIRING">维修中</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-link type="danger" @click="remove(row)">删除</el-link>
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
          @current-change="(page) => { query.page = page; load() }"
        />
      </div>
    </div>

    <DeviceFormDialog v-model="dialogVisible" :device="editing" @saved="load" />
  </div>
</template>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
