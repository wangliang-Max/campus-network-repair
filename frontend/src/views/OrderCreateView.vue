<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { pageDevices } from '@/api/device'
import { createOrder } from '@/api/order'

const router = useRouter()
const formRef = ref()
const devices = ref([])
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  deviceId: null,
  faultDesc: '',
  priority: 'NORMAL',
})

const rules = {
  deviceId: [{ required: true, message: '请选择故障设备', trigger: 'change' }],
  faultDesc: [
    { required: true, message: '请填写故障描述', trigger: 'blur' },
    { max: 500, message: '故障描述最长 500 字', trigger: 'blur' },
  ],
}

onMounted(async () => {
  loading.value = true
  try {
    const data = await pageDevices({ page: 1, size: 100 })
    devices.value = data.records
  } finally {
    loading.value = false
  }
})

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = await createOrder({ ...form })
    ElMessage.success('报修单已提交')
    router.replace(`/orders/${data.id}`)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page">
    <PageHeader title="新建报修" subtitle="选择故障设备并描述现象，提交后可在详情页跟踪处理进度" />

    <div class="panel form-panel">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" v-loading="loading">
        <el-form-item label="故障设备" prop="deviceId">
          <el-select v-model="form.deviceId" filterable placeholder="按编号或名称搜索" style="width: 100%">
            <el-option
              v-for="device in devices"
              :key="device.id"
              :value="device.id"
              :label="`${device.deviceCode} · ${device.deviceName} · ${device.roomName}`"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio value="NORMAL">普通</el-radio>
            <el-radio value="URGENT">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input
            v-model="form.faultDesc"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请描述故障现象、影响范围和发生时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交报修</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.form-panel {
  max-width: 760px;
}
</style>
