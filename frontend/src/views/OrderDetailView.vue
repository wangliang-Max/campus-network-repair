<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import {
  acceptOrder,
  confirmOrder,
  finishOrder,
  getOrder,
  returnInfo,
  startRepair,
  supplementOrder,
} from '@/api/order'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)
const dialogVisible = ref(false)
const actionType = ref('')
const submitting = ref(false)
const actionForm = reactive({ remark: '', content: '' })
const supplementVisible = ref(false)
const supplementForm = reactive({ faultDesc: '' })
const confirmVisible = ref(false)
const confirmForm = reactive({ result: 'PASS', score: 5, comment: '' })

const actionLabels = {
  accept: '受理',
  'start-repair': '开始维修',
  finish: '修复完成',
  'return-info': '退回补充',
  supplement: '补充信息',
  confirm: '验收',
}

const actionTitles = {
  accept: '受理报修单',
  'start-repair': '开始维修',
  finish: '提交修复完成',
  'return-info': '退回补充信息',
}

const allowed = computed(() => detail.value?.allowedActions || [])

async function load() {
  loading.value = true
  try {
    detail.value = await getOrder(route.params.id)
  } finally {
    loading.value = false
  }
}

function openAction(type) {
  if (type === 'supplement') {
    supplementForm.faultDesc = detail.value?.faultDesc || ''
    supplementVisible.value = true
    return
  }
  if (type === 'confirm') {
    confirmVisible.value = true
    return
  }
  actionType.value = type
  actionForm.remark = ''
  actionForm.content = ''
  dialogVisible.value = true
}

async function submitAction() {
  submitting.value = true
  try {
    const id = route.params.id
    const payload = { remark: actionForm.remark, content: actionForm.content }
    if (actionType.value === 'accept') await acceptOrder(id, payload)
    if (actionType.value === 'start-repair') await startRepair(id, payload)
    if (actionType.value === 'finish') await finishOrder(id, payload)
    if (actionType.value === 'return-info') await returnInfo(id, payload)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function submitSupplement() {
  if (!supplementForm.faultDesc.trim()) {
    ElMessage.warning('请填写补充后的故障描述')
    return
  }
  submitting.value = true
  try {
    await supplementOrder(route.params.id, { ...supplementForm })
    ElMessage.success('已补充并重新提交')
    supplementVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function submitConfirm() {
  submitting.value = true
  try {
    await confirmOrder(route.params.id, { ...confirmForm })
    ElMessage.success(confirmForm.result === 'PASS' ? '验收通过，工单已关闭' : '已退回返工')
    confirmVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page" v-loading="loading">
    <PageHeader
      v-if="detail"
      :title="detail.orderNo"
      :subtitle="`${detail.deviceName} · ${detail.roomName} · ${detail.reporterName}`"
    >
      <template #actions>
        <el-button @click="router.push('/orders')">返回列表</el-button>
        <el-button
          v-for="action in allowed"
          :key="action"
          :type="action === 'confirm' || action === 'accept' ? 'primary' : 'default'"
          @click="openAction(action)"
        >
          {{ actionLabels[action] || action }}
        </el-button>
      </template>
    </PageHeader>

    <template v-if="detail">
      <div class="panel">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="当前状态">
            <StatusTag :status="detail.status" :label="detail.statusLabel" />
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <StatusTag :status="detail.priority" :label="detail.priorityLabel" />
          </el-descriptions-item>
          <el-descriptions-item label="报障人">{{ detail.reporterName }}</el-descriptions-item>
          <el-descriptions-item label="设备">{{ detail.deviceName }}</el-descriptions-item>
          <el-descriptions-item label="机房">{{ detail.roomName }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="故障描述" :span="3">{{ detail.faultDesc }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="grid">
        <div class="panel">
          <h3>状态轨迹</h3>
          <el-timeline v-if="detail.statusLogs?.length">
            <el-timeline-item
              v-for="log in detail.statusLogs"
              :key="log.id"
              :timestamp="formatTime(log.createdAt)"
              placement="top"
            >
              <div class="log-title">
                <StatusTag :status="log.toStatus" :label="log.toStatusLabel" />
                <span class="muted">{{ log.operatorName }}：{{ log.remark }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无状态记录" />
        </div>

        <div class="panel">
          <h3>处理记录</h3>
          <el-table :data="detail.handles || []" empty-text="暂无处理记录" size="small">
            <el-table-column prop="action" label="动作" width="110" />
            <el-table-column prop="handlerName" label="处理人" width="90" />
            <el-table-column prop="content" label="内容" min-width="180" />
            <el-table-column label="时间" width="160">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div class="panel" v-if="detail.confirms?.length">
        <h3>验收记录</h3>
        <el-table :data="detail.confirms" size="small">
          <el-table-column prop="confirmName" label="确认人" width="100" />
          <el-table-column prop="result" label="结果" width="90" />
          <el-table-column prop="score" label="评分" width="80" />
          <el-table-column prop="comment" label="评价" min-width="200" />
          <el-table-column label="时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </template>

    <el-dialog v-model="dialogVisible" :title="actionTitles[actionType]" width="480px">
      <el-form label-position="top">
        <el-form-item label="备注（写入状态日志）">
          <el-input v-model="actionForm.remark" maxlength="200" placeholder="可留空，使用默认说明" />
        </el-form-item>
        <el-form-item label="处理内容（写入处理记录）">
          <el-input v-model="actionForm.content" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAction">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="supplementVisible" title="补充故障信息" width="520px">
      <el-input v-model="supplementForm.faultDesc" type="textarea" :rows="5" maxlength="500" show-word-limit />
      <template #footer>
        <el-button @click="supplementVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitSupplement">提交并重新受理</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="confirmVisible" title="验收确认" width="480px">
      <el-form label-position="top">
        <el-form-item label="验收结果">
          <el-radio-group v-model="confirmForm.result">
            <el-radio value="PASS">通过，关闭工单</el-radio>
            <el-radio value="FAIL">不通过，退回返工</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="满意度">
          <el-rate v-model="confirmForm.score" />
        </el-form-item>
        <el-form-item label="评价">
          <el-input v-model="confirmForm.comment" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitConfirm">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 16px;
  margin-top: 16px;
}

h3 {
  margin: 0 0 14px;
  font-size: 16px;
  color: #0b2545;
}

.log-title {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  font-size: 13px;
}

@media (max-width: 1000px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
