<template>
  <div class="calendar-page">
    <el-calendar v-model="currentDate">
      <template #date-cell="{ data }">
        <div class="calendar-cell" :class="{ 'has-diary': isDiaryDate(data.day) }"
          @click.stop="handleDateClick(data.day)">
          {{ data.day.split('-')[2] }}
          <div v-if="isDiaryDate(data.day)" class="diary-dot"></div>
        </div>
      </template>
    </el-calendar>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getCalendarDates } from '../api/diary'

const router = useRouter()
const currentDate = ref(new Date())
const diaryDates = ref([])

function isDiaryDate(date) {
  return diaryDates.value.includes(date)
}

async function loadCalendarDates() {
  const year = dayjs(currentDate.value).year()
  const month = dayjs(currentDate.value).month() + 1
  try {
    const res = await getCalendarDates(year, month)
    diaryDates.value = res.data
  } catch (e) {
    console.error('加载日历日期失败', e)
  }
}

async function handleDateClick(date) {
  router.push(`/calendar/${date}`)
}

watch(currentDate, loadCalendarDates)
onMounted(loadCalendarDates)
</script>

<style scoped>
.calendar-page {
  padding: 20px;
}
.calendar-cell {
  height: 100%;
  cursor: pointer;
}
.has-diary {
  font-weight: bold;
}
.diary-dot {
  width: 6px;
  height: 6px;
  background: #409eff;
  border-radius: 50%;
  margin: 2px auto 0;
}
</style>
