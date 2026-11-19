<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createDevice, updateDevice } from '@/api/device'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  device: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref()
const saving = ref(false)
const isEdit = computed(() => Boolean(props.device?.id))

const form = reactive({
  deviceCode: '',
  deviceName: '',
  roomName: '',
  status: 'NORMAL',
  remark: '',
})

const rules = {
  deviceCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  roomName: [{ required: true, message: '请输入机房名称', trigger: 'blur' }],
}

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) return
    Object.assign(form, {
      deviceCode: props.device?.deviceCode || '',
      deviceName: props.device?.deviceName || '',
      roomName: props.device?.roomName || '',
      status: props.device?.status || 'NORMAL',
      remark: props.device?.remark || '',
    })
    formRef.value?.clearValidate()
  },
)

function close() {
  emit('update:modelValue', false)
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      await updateDevice(props.device.id, { ...form })
      ElMessage.success('设备已更新')
    } else {
      await createDevice({ ...form })
      ElMessage.success('设备已新增')
    }
    emit('saved')
    close()
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑设备' : '新增设备'"
    width="520px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="设备编号" prop="deviceCode">
        <el-input v-model="form.deviceCode" placeholder="如 DEV-018" />
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input v-model="form.deviceName" placeholder="如 核心交换机" />
      </el-form-item>
      <el-form-item label="所在机房" prop="roomName">
        <el-input v-model="form.roomName" placeholder="如 实验楼A1机房" />
      </el-form-item>
      <el-form-item label="设备状态">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="正常" value="NORMAL" />
          <el-option label="故障" value="FAULT" />
          <el-option label="维修中" value="REPAIRING" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>
