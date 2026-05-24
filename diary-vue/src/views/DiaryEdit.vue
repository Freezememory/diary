<template>
  <div class="diary-edit">
    <div class="date-header">
      <el-button @click="changeDate(-1)">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <el-date-picker v-model="currentDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD"
        @change="loadData" />
      <el-button @click="changeDate(1)">
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>清单</span>
            </div>
          </template>
          <div v-for="category in categories" :key="category.id" class="category-group">
            <h4>{{ category.name }}</h4>
            <div v-for="item in getItemsByCategory(category.id)" :key="item.id" class="item-row">
              <el-checkbox v-model="item.isDone" :true-value="1" :false-value="0"
                @change="handleToggle(item)">
                <span :class="{ done: item.isDone === 1 }">{{ item.content }}</span>
              </el-checkbox>
              <el-button text type="danger" size="small" @click="handleDeleteItem(item.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <div class="add-item-row">
              <el-input
                v-model="newItemContents[category.id]"
                placeholder="输入新内容..."
                size="small"
                @keyup.enter="handleAddItemInline(category.id)"
              />
              <el-button type="primary" size="small" @click="handleAddItemInline(category.id)">
                提交
              </el-button>
            </div>
          </div>
          <el-empty v-if="items.length === 0" description="暂无清单" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="section-card">
          <template #header>
            <span>日记</span>
          </template>
          <el-input v-model="textContent" type="textarea" :rows="10" placeholder="记录今天的想法..."
            @blur="handleSaveContent" />
        </el-card>
        <el-card class="section-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>图片</span>
              <el-upload :show-file-list="false" :before-upload="handleUpload">
                <el-button type="primary" size="small">上传</el-button>
              </el-upload>
            </div>
          </template>
          <div class="image-list">
            <div v-for="img in images" :key="img.id" class="image-item">
              <el-image :src="img.imageUrl" fit="cover" :preview-src-list="[img.imageUrl]" />
              <el-button class="delete-btn" text type="danger" size="small"
                @click="handleDeleteImage(img.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <el-empty v-if="images.length === 0" description="暂无图片" />
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getCategories, getItems, createItem, toggleItem, deleteItem,
  getContent, saveContent, getImages, uploadImage, deleteImage } from '../api/diary'

const currentDate = ref(dayjs().format('YYYY-MM-DD'))
const categories = ref([])
const items = ref([])
const textContent = ref('')
const images = ref([])
const newItemContents = reactive({})

function getItemsByCategory(categoryId) {
  return items.value.filter(item => item.categoryId === categoryId)
}

function changeDate(delta) {
  currentDate.value = dayjs(currentDate.value).add(delta, 'day').format('YYYY-MM-DD')
  loadData()
}

async function loadData() {
  clearTimeout(saveTimer)
  try {
    const [catRes, itemRes, contentRes, imgRes] = await Promise.allSettled([
      getCategories(),
      getItems(currentDate.value),
      getContent(currentDate.value),
      getImages(currentDate.value)
    ])
    if (catRes.status === 'fulfilled') categories.value = catRes.value.data
    if (itemRes.status === 'fulfilled') items.value = itemRes.value.data
    if (contentRes.status === 'fulfilled') textContent.value = contentRes.value.data?.textContent || ''
    if (imgRes.status === 'fulfilled') images.value = imgRes.value.data
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

watch(categories, (newCats) => {
  newCats.forEach(cat => {
    if (!(cat.id in newItemContents)) {
      newItemContents[cat.id] = ''
    }
  })
}, { immediate: true })

async function handleAddItemInline(categoryId) {
  const content = newItemContents[categoryId]?.trim()
  if (!content) {
    ElMessage.warning('请输入内容')
    return
  }
  try {
    await createItem({
      categoryId,
      diaryDate: currentDate.value,
      content
    })
    newItemContents[categoryId] = ''
    ElMessage.success('添加成功')
    loadData()
  } catch (e) {
    console.error('添加清单失败', e)
  }
}

async function handleToggle(item) {
  const prev = item.isDone
  try {
    await toggleItem(item.id)
  } catch (e) {
    item.isDone = prev
    console.error('切换状态失败', e)
  }
}

async function handleDeleteItem(id) {
  try {
    await ElMessageBox.confirm('确定删除该清单？', '提示', { type: 'warning' })
    await deleteItem(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error('删除清单失败', e)
  }
}

let saveTimer = null
function handleSaveContent() {
  clearTimeout(saveTimer)
  saveTimer = setTimeout(async () => {
    try {
      await saveContent(currentDate.value, { textContent: textContent.value })
    } catch (e) {
      console.error('保存日记失败', e)
    }
  }, 1000)
}

async function handleUpload(file) {
  try {
    await uploadImage(currentDate.value, file)
    ElMessage.success('上传成功')
    loadData()
  } catch (e) {
    console.error('上传图片失败', e)
  }
  return false
}

async function handleDeleteImage(id) {
  try {
    await ElMessageBox.confirm('确定删除该图片？', '提示', { type: 'warning' })
    await deleteImage(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error('删除图片失败', e)
  }
}

onMounted(loadData)
onUnmounted(() => clearTimeout(saveTimer))
</script>

<style scoped>
.diary-edit {
  padding: 20px;
}
.date-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}
.section-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.category-group {
  margin-bottom: 15px;
}
.category-group h4 {
  margin-bottom: 10px;
  color: #666;
}
.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 0;
}
.done {
  background-color: #f0f9eb;
  border-radius: 4px;
  padding: 2px 8px;
}
.add-item-row {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
.add-item-row .el-input {
  flex: 1;
}
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.image-item {
  position: relative;
  width: 100px;
  height: 100px;
}
.image-item .el-image {
  width: 100%;
  height: 100%;
}
.delete-btn {
  position: absolute;
  top: 0;
  right: 0;
}

@media (max-width: 768px) {
  .diary-edit {
    padding: 12px;
  }
  .image-item {
    width: 80px;
    height: 80px;
  }
  .date-header {
    gap: 5px;
  }
  .add-item-row {
    flex-direction: column;
  }
}
</style>
