<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { register as registerApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

const mode = ref('login')
const loading = ref(false)
const formRef = ref()
const form = reactive({
  username: 'stu07',
  password: '123456',
  realName: '',
  phone: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]{4,20}$/, message: '4-20 位字母、数字或下划线', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    if (mode.value === 'login') {
      await user.login({ username: form.username, password: form.password })
      ElMessage.success('登录成功')
    } else {
      const data = await registerApi({
        username: form.username,
        password: form.password,
        realName: form.realName,
        phone: form.phone,
      })
      user.setAuth(data)
      await user.fetchProfile()
      ElMessage.success('注册成功，已自动登录')
    }
    router.replace(route.query.redirect || '/dashboard')
  } finally {
    loading.value = false
  }
}

function fill(username) {
  form.username = username
  form.password = '123456'
}
</script>

<template>
  <div class="login">
    <div class="left">
      <div class="mark">T05</div>
      <h1>校园网络与机房<br />故障报修平台</h1>
      <p>报障、受理、维修、验收的全流程状态追踪</p>
      <div class="points">
        <span>六状态流转</span>
        <span>三方角色权限</span>
        <span>全程留痕可统计</span>
      </div>
    </div>

    <div class="right">
      <div class="card">
        <h2>{{ mode === 'login' ? '登录' : '报障人注册' }}</h2>
        <p class="muted">JavaWeb · MVVM · Docker 共用项目</p>
        <el-form ref="formRef" :model="form" :rules="mode === 'login' ? rules : registerRules" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" size="large" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" size="large" show-password placeholder="请输入密码" @keyup.enter="submit" />
          </el-form-item>
          <template v-if="mode === 'register'">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" size="large" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" size="large" placeholder="选填" />
            </el-form-item>
          </template>
          <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
            {{ mode === 'login' ? '登录' : '注册并登录' }}
          </el-button>
        </el-form>

        <div class="switch">
          <span v-if="mode === 'login'">没有账号？<el-link type="primary" @click="mode = 'register'">报障人注册</el-link></span>
          <span v-else>已有账号？<el-link type="primary" @click="mode = 'login'">返回登录</el-link></span>
        </div>

        <div v-if="mode === 'login'" class="demo">
          <span class="muted">演示账号：</span>
          <el-link @click="fill('admin')">admin</el-link>
          <el-link @click="fill('tech01')">tech01</el-link>
          <el-link @click="fill('stu07')">stu07</el-link>
          <span class="muted">密码 123456</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  background: #fff;
}

.left {
  background: #0b2545;
  color: #fff;
  padding: 72px 64px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.mark {
  font-size: 72px;
  font-weight: 800;
  color: #12345c;
  line-height: 1;
  margin-bottom: 24px;
}

h1 {
  font-size: 40px;
  line-height: 1.35;
  margin: 0 0 18px;
}

.left p {
  color: #c9d6e3;
  font-size: 17px;
  margin: 0 0 28px;
}

.points {
  display: flex;
  gap: 18px;
  color: #8fb3d9;
  font-size: 13px;
}

.right {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: #f7f9fb;
}

.card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border: 1px solid #d9e2ec;
  border-radius: 14px;
  padding: 32px;
}

h2 {
  margin: 0 0 6px;
  color: #0b2545;
  font-size: 26px;
}

.card > p {
  margin: 0 0 22px;
  font-size: 13px;
}

.submit {
  width: 100%;
  margin-top: 6px;
}

.switch {
  margin-top: 16px;
  font-size: 13px;
  color: #52606d;
}

.demo {
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px dashed #d9e2ec;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  font-size: 12px;
}

@media (max-width: 900px) {
  .login {
    grid-template-columns: 1fr;
  }
  .left {
    display: none;
  }
}
</style>
