<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="not-found">
    <div class="code">404</div>
    <h1>页面不存在</h1>
    <p class="muted">你访问的地址没有对应的页面，可能工单已经被删除或链接有误。</p>
    <el-button type="primary" @click="router.replace('/dashboard')">回到工作台</el-button>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: #f7f9fb;
}

.code {
  font-size: 96px;
  font-weight: 800;
  color: #0b2545;
}

h1 {
  margin: 0;
  font-size: 24px;
}

p {
  margin: 0 0 10px;
}
</style>
