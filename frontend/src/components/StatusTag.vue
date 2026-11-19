<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, default: '' },
  label: { type: String, default: '' },
})

const map = {
  PENDING: { type: 'info', text: '待受理' },
  INVESTIGATING: { type: 'warning', text: '排查中' },
  REPAIRING: { type: 'primary', text: '修复中' },
  PENDING_CONFIRM: { type: 'warning', text: '待确认' },
  CLOSED: { type: 'success', text: '已关闭' },
  INFO_NEEDED: { type: 'danger', text: '待补充' },
  NORMAL: { type: 'success', text: '正常' },
  FAULT: { type: 'danger', text: '故障' },
  URGENT: { type: 'danger', text: '紧急' },
}

const current = computed(() => map[props.status] || { type: 'info', text: props.status })
</script>

<template>
  <el-tag :type="current.type" effect="light">{{ label || current.text }}</el-tag>
</template>
