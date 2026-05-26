<template>
  <div class="calendar-detail">
    <div class="back-bar">
      <button class="back-btn" @click="router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
        返回
      </button>
    </div>
    <DiaryEdit :initial-date="route.params.date" readonly />
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import DiaryEdit from './DiaryEdit.vue'

const route = useRoute()
const router = useRouter()
</script>

<style scoped>
.calendar-detail {
  min-height: 100%;
}
.back-bar {
  max-width: 640px;
  margin: 0 auto;
  padding: 16px 16px 0;
}
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 20px;
  border: 1.5px solid #d6c8b0;
  background: transparent;
  color: #8c7a5e;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.back-btn:hover {
  background: #f5f0e8;
  border-color: #b8a682;
}
</style>
