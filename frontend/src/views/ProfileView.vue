<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/auth'
import { formatTime } from '@/utils/format'

const user = useUserStore()
const formRef = ref()
const saving = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' },
  ],
  confirmPassword: [
    {
      validator: (_rule, value, callback) => {
        if (value !== form.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    await changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    ElMessage.success('密码已修改')
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="page">
    <PageHeader title="个人中心" subtitle="查看账号信息并修改密码" />

    <div class="grid">
      <div class="panel">
        <h3>账号信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ user.profile?.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ user.profile?.realName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ user.profile?.phone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ user.profile?.roleName }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(user.profile?.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="panel">
        <h3>修改密码</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="form.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="form.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #0b2545;
}

@media (max-width: 900px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
