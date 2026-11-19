<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import SearchBar from '@/components/SearchBar.vue'
import { pageUsers, setUserEnabled } from '@/api/user'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', roleCode: '', enabled: null })

async function load() {
  loading.value = true
  try {
    const data = await pageUsers({ ...query })
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(query, { page: 1, keyword: '', roleCode: '', enabled: null })
  load()
}

async function toggle(row, enabled) {
  const action = enabled ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}用户 ${row.username} 吗？`, `${action}用户`, { type: 'warning' })
    await setUserEnabled(row.id, enabled)
    ElMessage.success(`已${action}`)
    await load()
  } catch {
    row.enabled = !enabled
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader title="用户管理" subtitle="管理员可以启用或禁用账号；不能禁用当前登录账号" />

    <div class="panel">
      <SearchBar @search="query.page = 1; load()" @reset="reset">
        <el-input v-model="query.keyword" placeholder="用户名或姓名" clearable style="width: 220px" />
        <el-select v-model="query.roleCode" placeholder="全部角色" clearable style="width: 160px">
          <el-option label="报障人" value="REPORTER" />
          <el-option label="技术员" value="TECHNICIAN" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
        <el-select v-model="query.enabled" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="启用" :value="true" />
          <el-option label="禁用" :value="false" />
        </el-select>
      </SearchBar>

      <el-table v-loading="loading" :data="rows" empty-text="暂无用户">
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="110" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="roleName" label="角色" width="120" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="(value) => toggle(row, value)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
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
  </div>
</template>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
