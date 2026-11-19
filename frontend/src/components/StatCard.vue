<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], default: '--' },
  hint: { type: String, default: '' },
  tone: { type: String, default: 'navy' },
})
</script>

<template>
  <div class="stat" :class="tone">
    <div class="label">{{ label }}</div>
    <div class="value">{{ value }}</div>
    <div v-if="hint" class="hint">{{ hint }}</div>
  </div>
</template>

<style scoped>
.stat {
  background: #fff;
  border: 1px solid #d9e2ec;
  border-radius: 10px;
  padding: 18px 20px;
}

.label {
  font-size: 13px;
  color: #52606d;
}

.value {
  font-size: 30px;
  font-weight: 700;
  margin-top: 8px;
  color: #0b2545;
}

.hint {
  margin-top: 6px;
  font-size: 12px;
  color: #7b8794;
}

.stat.teal .value {
  color: #0e7c86;
}

.stat.amber .value {
  color: #b7791f;
}

.stat.red .value {
  color: #c0392b;
}
</style>
