<template>
  <div class="calendar-detail">
    <div class="detail-header">
      <el-button @click="router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>{{ route.params.date }}</h2>
    </div>

    <el-card v-if="categories.length > 0" class="section-card">
      <template #header><h3>清单</h3></template>
      <div v-for="category in categories" :key="category.id" class="category-group">
        <h4>{{ category.name }}</h4>
        <div v-for="item in getItemsByCategory(category.id)" :key="item.id" class="item-row">
          <el-checkbox :model-value="item.isDone === 1" disabled>
            <span :class="{ done: item.isDone === 1 }">{{ item.content }}</span>
          </el-checkbox>
        </div>
      </div>
      <el-empty v-if="items.length === 0" description="无清单" />
    </el-card>

    <el-card class="section-card">
      <template #header><h3>日记</h3></template>
      <p class="diary-text">{{ content?.textContent || '无内容' }}</p>
    </el-card>

    <el-card v-if="images.length > 0" class="section-card">
      <template #header><h3>图片</h3></template>
      <div class="image-list">
        <el-image v-for="img in images" :key="img.id" :src="resolveImageUrl(img.imageUrl)"
          fit="cover" :preview-src-list="images.map(i => resolveImageUrl(i.imageUrl))" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, getItems, getContent, getImages } from '../api/diary'
import { resolveImageUrl } from '../utils/request'

const route = useRoute()
const router = useRouter()
const categories = ref([])
const items = ref([])
const content = ref(null)
const images = ref([])

function getItemsByCategory(categoryId) {
  return items.value.filter(item => item.categoryId === categoryId)
}

async function loadData() {
  const date = route.params.date
  try {
    const [catRes, itemRes, contentRes, imgRes] = await Promise.allSettled([
      getCategories(),
      getItems(date),
      getContent(date),
      getImages(date)
    ])
    if (catRes.status === 'fulfilled') categories.value = catRes.value.data
    if (itemRes.status === 'fulfilled') items.value = itemRes.value.data
    if (contentRes.status === 'fulfilled') content.value = contentRes.value.data
    if (imgRes.status === 'fulfilled') images.value = imgRes.value.data
  } catch (e) {
    console.error('加载日记详情失败', e)
  }
}

onMounted(loadData)
</script>

<style scoped>
.calendar-detail {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}
.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.detail-header h2 {
  margin: 0;
}
.section-card {
  margin-bottom: 16px;
}
.category-group {
  margin-bottom: 10px;
}
.category-group h4 {
  color: #666;
  margin-bottom: 5px;
}
.item-row {
  padding: 3px 0;
}
.done {
  text-decoration: line-through;
  color: #999;
}
.diary-text {
  line-height: 1.8;
  white-space: pre-wrap;
}
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.image-list .el-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
}
</style>
