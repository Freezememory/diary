<template>
  <div class="diary-edit">
    <!-- 日期显示 -->
    <div class="date-area">
      <div class="date-clickable" @click="openDatePicker">
        <span class="date-weekday">{{ weekday }}</span>
        <span class="date-display">{{ formattedDate }}</span>
      </div>
      <el-date-picker ref="datePickerRef" v-model="currentDate" type="date"
        format="YYYY年MM月DD日" value-format="YYYY-MM-DD"
        :clearable="false" :editable="false" @change="loadData"
        class="hidden-date-picker" />
      <div class="date-nav-arrows">
        <button class="date-arrow" @click="changeDate(-1)">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
        </button>
        <button class="date-arrow" @click="changeDate(1)">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>
        </button>
      </div>
    </div>

    <!-- 主标签页：清单 / 日记 / 图片 -->
    <div class="main-tabs-row">
      <button class="tab-btn" :class="{ active: activeTab === 'checklist' }" @click="activeTab = 'checklist'">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"></path><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path></svg>
        清单
      </button>
      <button class="tab-btn" :class="{ active: activeTab === 'diary' }" @click="activeTab = 'diary'">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"></path><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path></svg>
        日记
      </button>
      <button class="tab-btn" :class="{ active: activeTab === 'images' }" @click="activeTab = 'images'">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
        图片
      </button>
    </div>

    <!-- 清单内容 -->
    <div v-show="activeTab === 'checklist'" class="tab-content">
      <!-- 待办/已办 切换 -->
      <div class="segment-control">
        <div class="segment-track">
          <button class="segment-btn" :class="{ active: checklistTab === 'pending' }" @click="checklistTab = 'pending'">
            待办
            <span v-if="pendingCount" class="badge">{{ pendingCount }}</span>
          </button>
          <button class="segment-btn" :class="{ active: checklistTab === 'completed' }" @click="checklistTab = 'completed'">
            已办
            <span v-if="completedCount" class="badge">{{ completedCount }}</span>
          </button>
          <div class="segment-indicator" :class="{ right: checklistTab === 'completed' }"></div>
        </div>
      </div>

      <!-- 清单列表 -->
      <div class="checklist-area">
        <template v-for="category in categories" :key="category.id">
          <div v-if="getFilteredItemsByCategory(category.id).length > 0 || checklistTab === 'pending'" class="category-block">
            <div class="category-label">
              <span class="category-dot"></span>
              {{ category.name }}
            </div>

            <!-- 每个清单项 -->
            <div v-for="item in getFilteredItemsByCategory(category.id)" :key="item.id" class="item-card">
              <label class="item-check">
                <input type="checkbox" :checked="item.isDone === 1" :disabled="readonly"
                  @change="handleToggle(item)" />
                <span class="checkmark">
                  <svg v-if="item.isDone === 1" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                </span>
              </label>
              <div class="item-content" v-if="editingItemId === item.id">
                <input
                  ref="editInputRef"
                  v-model="editContent"
                  class="edit-input"
                  @keyup.enter="saveEdit(item)"
                  @blur="saveEdit(item)"
                  @keyup.esc="cancelEdit"
                />
              </div>
              <div class="item-content" v-else>
                <span :class="['item-text', { done: item.isDone === 1 }]" @click.stop="!readonly && startEdit(item)">{{ item.content }}</span>
              </div>
              <button v-if="!readonly" class="item-delete" @click="handleDeleteItem(item.id)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
              </button>
            </div>

            <!-- 添加新清单项（仅待办模式 且 非只读） -->
            <div v-if="checklistTab === 'pending' && !readonly" class="add-row">
              <input
                v-model="newItemContents[category.id]"
                class="add-input"
                placeholder="添加新项..."
                @keyup.enter="handleAddItemInline(category.id)"
              />
              <button class="add-btn" @click="handleAddItemInline(category.id)" :disabled="!newItemContents[category.id]?.trim()">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
              </button>
            </div>
          </div>
        </template>

        <div v-if="filteredItems.length === 0" class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" opacity="0.3"><path d="M9 11l3 3L22 4"></path><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path></svg>
          <span>{{ checklistTab === 'pending' ? '今天没有待办事项' : '还没有完成的事项' }}</span>
        </div>
      </div>
    </div>

    <!-- 日记内容 -->
    <div v-show="activeTab === 'diary'" class="tab-content diary-tab">
      <div class="diary-editor">
        <textarea
          v-model="textContent"
          class="diary-textarea"
          placeholder="记录今天的想法..."
          rows="16"
          :readonly="readonly"
          @blur="handleSaveContent"
        ></textarea>
        <div class="diary-footer">
          <span class="char-count">{{ textContent.length }} 字</span>
        </div>
      </div>
    </div>

    <!-- 图片 tab -->
    <div v-show="activeTab === 'images'" class="tab-content images-tab">
      <div class="images-header" v-if="!readonly">
        <el-upload :show-file-list="false" :before-upload="handleUpload">
          <button class="upload-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
            上传图片
          </button>
        </el-upload>
      </div>

      <div v-if="images.length > 0" class="gallery-grid">
        <div v-for="img in images" :key="img.id" class="gallery-card">
          <el-image :src="resolveImageUrl(img.imageUrl)" fit="cover"
            :preview-src-list="images.map(i => resolveImageUrl(i.imageUrl))"
            :preview-src-list-index="images.indexOf(img)"
            preview-teleported
            class="gallery-img" />
          <button v-if="!readonly" class="gallery-delete" @click.stop="handleDeleteImage(img.id)">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
        </div>
      </div>

      <div v-else class="images-empty">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" opacity="0.3"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
        <span>暂无图片</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getCategories, getItems, createItem, toggleItem, deleteItem, updateItem,
  getContent, saveContent, getImages, uploadImage, deleteImage } from '../api/diary'
import { resolveImageUrl } from '../utils/request'

const props = defineProps({
  readonly: { type: Boolean, default: false },
  initialDate: { type: String, default: '' }
})

const currentDate = ref(props.initialDate || dayjs().format('YYYY-MM-DD'))
const activeTab = ref('checklist')
const categories = ref([])
const items = ref([])
const textContent = ref('')
const images = ref([])
const newItemContents = reactive({})
const editingItemId = ref(null)
const editContent = ref('')
const checklistTab = ref('pending')
const datePickerRef = ref(null)

const formattedDate = computed(() => {
  const d = dayjs(currentDate.value)
  return `${d.year()}年${d.format('M月D日')}`
})

const weekday = computed(() => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[dayjs(currentDate.value).day()]
})

const filteredItems = computed(() => {
  return checklistTab.value === 'pending'
    ? items.value.filter(i => i.isDone === 0)
    : items.value.filter(i => i.isDone === 1)
})

const pendingCount = computed(() => items.value.filter(i => i.isDone === 0).length)
const completedCount = computed(() => items.value.filter(i => i.isDone === 1).length)

function getFilteredItemsByCategory(categoryId) {
  return filteredItems.value.filter(item => item.categoryId === categoryId)
}

function openDatePicker() {
  if (props.readonly) return
  datePickerRef.value?.focus()
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
  if (props.readonly) return
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

const editInputRef = ref(null)
const isSaving = ref(false)

async function startEdit(item) {
  editingItemId.value = item.id
  editContent.value = item.content
  await nextTick()
  const inputEl = editInputRef.value
  if (inputEl) {
    const el = Array.isArray(inputEl) ? inputEl[0] : inputEl
    el?.focus()
  }
}

async function saveEdit(item) {
  if (isSaving.value) return
  isSaving.value = true
  const newContent = editContent.value.trim()
  if (!newContent || newContent === item.content) {
    editingItemId.value = null
    isSaving.value = false
    return
  }
  try {
    await updateItem(item.id, { content: newContent })
    ElMessage.success('修改成功')
    editingItemId.value = null
    loadData()
  } catch (e) {
    console.error('修改清单失败', e)
  } finally {
    isSaving.value = false
  }
}

function cancelEdit() {
  editingItemId.value = null
  editContent.value = ''
}

let saveTimer = null
function handleSaveContent() {
  if (props.readonly) return
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

onMounted(() => {
  if (props.initialDate) currentDate.value = props.initialDate
  loadData()
})
onUnmounted(() => clearTimeout(saveTimer))
</script>

<style scoped>
.diary-edit {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px 16px;
  font-family: 'Georgia', 'Noto Serif SC', serif;
  position: relative;
}

/* ---- 日期区域 ---- */
.date-area {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.date-clickable {
  display: inline-flex;
  flex-direction: column;
  gap: 1px;
  padding: 6px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}
.date-clickable:hover {
  background: #f0ebe3;
}
.date-weekday {
  font-size: 11px;
  color: #b8a682;
  letter-spacing: 1px;
}
.date-display {
  font-size: 18px;
  font-weight: 600;
  color: #4a3f30;
  line-height: 1.3;
}
.hidden-date-picker {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: none;
}
.date-nav-arrows {
  display: flex;
  gap: 6px;
}
.date-arrow {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 1.5px solid #d6c8b0;
  background: transparent;
  color: #8c7a5e;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.date-arrow:hover {
  background: #f5f0e8;
  border-color: #b8a682;
}

/* ---- 主标签页 ---- */
.main-tabs-row {
  display: flex;
  gap: 4px;
  margin-bottom: 20px;
  background: #f5f0e8;
  border-radius: 12px;
  padding: 4px;
}
.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 0;
  border: none;
  background: transparent;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  color: #8c7a5e;
  transition: all 0.25s ease;
  font-family: inherit;
  font-weight: 500;
}
.tab-btn.active {
  background: #fff;
  color: #4a3f30;
  box-shadow: 0 1px 3px rgba(74, 63, 48, 0.08);
}
.tab-btn:not(.active):hover {
  color: #6b5d47;
}

/* ---- 待办/已办 切换 ---- */
.segment-control {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.segment-track {
  display: flex;
  position: relative;
  background: #efe8dc;
  border-radius: 24px;
  padding: 3px;
}
.segment-btn {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 24px;
  border: none;
  background: transparent;
  border-radius: 22px;
  cursor: pointer;
  font-size: 13px;
  color: #8c7a5e;
  transition: color 0.25s;
  font-family: inherit;
  font-weight: 500;
}
.segment-btn.active {
  color: #4a3f30;
}
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #d6c8b0;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  line-height: 1;
}
.segment-btn.active .badge {
  background: #8c7a5e;
}
.segment-indicator {
  position: absolute;
  top: 3px;
  left: 3px;
  width: calc(50% - 3px);
  height: calc(100% - 6px);
  background: #fff;
  border-radius: 21px;
  box-shadow: 0 1px 3px rgba(74, 63, 48, 0.1);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.segment-indicator.right {
  transform: translateX(100%);
}

/* ---- 清单区域 ---- */
.checklist-area {
  min-height: 120px;
}
.category-block {
  margin-bottom: 20px;
}
.category-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #b8a682;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  margin-bottom: 10px;
  padding-left: 2px;
}
.category-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #d6c8b0;
}

/* ---- 清单项卡片 ---- */
.item-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  margin-bottom: 6px;
  border-radius: 10px;
  background: #faf8f4;
  border: 1px solid transparent;
  transition: all 0.2s;
}
.item-card:hover {
  border-color: #e8e0d4;
}
.item-check {
  position: relative;
  cursor: pointer;
  flex-shrink: 0;
}
.item-check input[type="checkbox"] {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}
.checkmark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1.5px solid #c4b89a;
  transition: all 0.2s;
}
.item-check input:checked + .checkmark {
  background: #8cb369;
  border-color: #8cb369;
}
.item-content {
  flex: 1;
  min-width: 0;
}
.item-text {
  font-size: 14px;
  color: #4a3f30;
  line-height: 1.5;
  cursor: pointer;
  display: block;
  word-break: break-all;
}
.item-text:hover {
  color: #2d6a4f;
}
.item-text.done {
  text-decoration: line-through;
  color: #b8a682;
}
.edit-input {
  width: 100%;
  border: none;
  border-bottom: 1.5px solid #d6c8b0;
  background: transparent;
  font-size: 14px;
  color: #4a3f30;
  padding: 2px 0;
  outline: none;
  font-family: inherit;
}
.edit-input:focus {
  border-color: #8c7a5e;
}
.item-delete {
  flex-shrink: 0;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: #c4b89a;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.2s;
}
.item-card:hover .item-delete {
  opacity: 1;
}
.item-delete:hover {
  background: #fde8e8;
  color: #e57373;
}

/* ---- 添加新项 ---- */
.add-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  padding-left: 32px;
}
.add-input {
  flex: 1;
  max-width: 280px;
  padding: 7px 12px;
  border: 1.5px dashed #d6c8b0;
  border-radius: 8px;
  background: transparent;
  font-size: 13px;
  color: #4a3f30;
  outline: none;
  font-family: inherit;
  transition: border-color 0.2s;
}
.add-input::placeholder {
  color: #c4b89a;
}
.add-input:focus {
  border-color: #8c7a5e;
  border-style: solid;
}
.add-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: none;
  background: #8c7a5e;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}
.add-btn:hover:not(:disabled) {
  background: #6b5d47;
  transform: scale(1.05);
}
.add-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

/* ---- 空状态 ---- */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 0;
  color: #c4b89a;
  font-size: 14px;
}

/* ---- 日记编辑 ---- */
.diary-tab {
  padding-top: 4px;
}
.diary-editor {
  background: #faf8f4;
  border-radius: 12px;
  border: 1px solid #e8e0d4;
  overflow: hidden;
}
.diary-textarea {
  width: 100%;
  min-height: 320px;
  padding: 20px;
  border: none;
  background: transparent;
  font-size: 15px;
  line-height: 1.8;
  color: #4a3f30;
  resize: vertical;
  outline: none;
  font-family: 'Georgia', 'Noto Serif SC', serif;
}
.diary-textarea::placeholder {
  color: #c4b89a;
}
.diary-footer {
  display: flex;
  justify-content: flex-end;
  padding: 8px 16px;
  border-top: 1px solid #f0ebe3;
}
.char-count {
  font-size: 11px;
  color: #c4b89a;
  font-family: -apple-system, sans-serif;
}

/* ---- 图片 tab ---- */
.images-tab {
  padding-top: 4px;
}
.images-header {
  margin-bottom: 16px;
}
.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border-radius: 20px;
  border: 1.5px dashed #c4b89a;
  background: transparent;
  color: #8c7a5e;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.upload-btn:hover {
  background: #f5f0e8;
  border-color: #8c7a5e;
  border-style: solid;
}
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}
.gallery-card {
  position: relative;
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  background: #f0ebe3;
}
.gallery-img {
  width: 100%;
  height: 100%;
}
.gallery-delete {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.85);
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.2s;
  backdrop-filter: blur(4px);
}
.gallery-card:hover .gallery-delete {
  opacity: 1;
}
.gallery-delete:hover {
  background: #fde8e8;
  color: #e57373;
}
.images-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 64px 0;
  color: #c4b89a;
  font-size: 14px;
}

/* ---- 响应式 ---- */
@media (max-width: 768px) {
  .diary-edit {
    padding: 16px 12px;
  }
  .segment-btn {
    padding: 7px 16px;
    font-size: 12px;
  }
  .add-row {
    padding-left: 0;
  }
  .add-input {
    max-width: none;
  }
  .gallery-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 8px;
  }
}
</style>
