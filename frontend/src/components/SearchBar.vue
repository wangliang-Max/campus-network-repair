<script setup>
defineEmits(['search', 'reset'])
</script>

<template>
  <div class="search-bar">
    <div class="fields">
      <slot />
    </div>
    <div class="buttons">
      <el-button type="primary" @click="$emit('search')">查询</el-button>
      <el-button @click="$emit('reset')">重置</el-button>
    </div>
  </div>
</template>

<style scoped>
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.fields {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
}

.buttons {
  display: flex;
  gap: 8px;
}
</style>
