# 前端优化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实施日记系统（随风平台）的8项前端优化需求，包括命名修改、交互优化、功能新增和bug修复。

**Architecture:** 前端为Vue 3 + Element Plus单页应用，后端为Spring Cloud Gateway微服务架构。本次优化主要涉及前端组件修改、路由新增和后端Gateway配置调整。

**Tech Stack:** Vue 3 Composition API, Element Plus, Vue Router, Pinia, Spring Cloud Gateway, Java 17

---

## 文件结构总览

| 文件 | 操作 | 职责 |
|------|------|------|
| `diary-vue/src/views/Layout.vue` | 修改 | 系统标题和导航菜单命名 |
| `diary-vue/src/views/Login.vue` | 修改 | 登录页标题命名 |
| `diary-vue/index.html` | 修改 | 浏览器标签页标题 |
| `diary-vue/src/views/DiaryEdit.vue` | 修改 | 清单样式、日期选择器、编辑功能、待办/已办Tab |
| `diary-vue/src/views/Calendar.vue` | 修改 | 点击日期跳转替代弹窗 |
| `diary-vue/src/views/CalendarDetail.vue` | 新建 | 日历日期详情页 |
| `diary-vue/src/router/index.js` | 修改 | 添加详情页路由 |
| `diary-system/diary-gateway/src/main/resources/application.yml` | 修改 | Gateway缓冲区配置 |

---

### Task 1: 系统命名修改

**Files:**
- Modify: `diary-vue/src/views/Layout.vue:6,23,40`
- Modify: `diary-vue/src/views/Login.vue:4`
- Modify: `diary-vue/index.html:6`

- [ ] **Step 1: 修改 Layout.vue 标题**

将第6行的 `<h1>日记系统</h1>` 替换为 `<h1>随风</h1>`：

```vue
<h1>随风</h1>
```

- [ ] **Step 2: 修改 Layout.vue 桌面侧边栏导航菜单**

将第23行的 `<span>日历查看</span>` 替换为 `<span>往事</span>`：

```vue
<el-menu-item index="/calendar">
  <el-icon><Calendar /></el-icon>
  <span>往事</span>
</el-menu-item>
```

- [ ] **Step 3: 修改 Layout.vue 移动端抽屉导航菜单**

将第40行的 `<span>日历查看</span>` 替换为 `<span>往事</span>`：

```vue
<el-menu-item index="/calendar">
  <el-icon><Calendar /></el-icon>
  <span>往事</span>
</el-menu-item>
```

- [ ] **Step 4: 修改 Login.vue 标题**

将第4行的 `<h2>日记系统</h2>` 替换为 `<h2>随风</h2>`：

```vue
<h2>随风</h2>
```

- [ ] **Step 5: 修改 index.html 浏览器标签页标题**

将第6行的 `<title>日记系统</title>` 替换为 `<title>随风</title>`：

```html
<title>随风</title>
```

- [ ] **Step 6: 验证修改**

```bash
cd diary-vue && grep -rn "日记系统" src/ index.html
```
Expected: 无输出（所有"日记系统"已替换）

```bash
grep -rn "日历查看" src/
```
Expected: 无输出（"日历查看"已替换为"往事"）

- [ ] **Step 7: 提交**

```bash
git add diary-vue/src/views/Layout.vue diary-vue/src/views/Login.vue diary-vue/index.html
git commit -m "refactor: 系统名称改为随风，日历查看改为往事"
```

---

### Task 2: 修复移动端输入框放大 + 日期选择器不可为空

**Files:**
- Modify: `diary-vue/src/views/DiaryEdit.vue:7-8` (日期选择器)
- Modify: `diary-vue/src/views/DiaryEdit.vue:268-279` (CSS)

- [ ] **Step 1: 为日期选择器添加 clearable=false**

将第7-8行：
```vue
<el-date-picker v-model="currentDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD"
  @change="loadData" />
```

替换为：
```vue
<el-date-picker v-model="currentDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD"
  :clearable="false" @change="loadData" />
```

- [ ] **Step 2: 添加移动端清单输入框 font-size: 16px 样式**

在 `DiaryEdit.vue` 的 `<style scoped>` 区域（第203行开始），在 `.add-item-row .el-input` 样式之后添加：

```css
.add-item-row .el-input {
  flex: 1;
}
.add-item-row .el-input__inner {
  font-size: 16px;
}
```

- [ ] **Step 3: 验证修改**

```bash
cd diary-vue && grep -n "clearable" src/views/DiaryEdit.vue
```
Expected: 输出包含 `:clearable="false"`

```bash
grep -n "font-size: 16px" src/views/DiaryEdit.vue
```
Expected: 输出包含 `font-size: 16px`

- [ ] **Step 4: 提交**

```bash
git add diary-vue/src/views/DiaryEdit.vue
git commit -m "fix: 日期选择器不可清空，移动端输入框禁止缩放"
```

---

### Task 3: 清单完成后标记优化

**Files:**
- Modify: `diary-vue/src/views/DiaryEdit.vue:230-234` (CSS .done 类)

- [ ] **Step 1: 移除 .done 背景色样式，添加绿色复选框样式**

将第230-234行：
```css
.done {
  background-color: #f0f9eb;
  border-radius: 4px;
  padding: 2px 8px;
}
```

替换为：
```css
.done {
  text-decoration: line-through;
  color: #999;
}
:deep(.el-checkbox.is-checked .el-checkbox__inner) {
  background-color: #67c23a;
  border-color: #67c23a;
}
:deep(.el-checkbox.is-checked .el-checkbox__label) {
  color: #999;
}
```

- [ ] **Step 2: 验证修改**

```bash
cd diary-vue && grep -n "background-color: #f0f9eb" src/views/DiaryEdit.vue
```
Expected: 无输出（背景色已移除）

- [ ] **Step 3: 提交**

```bash
git add diary-vue/src/views/DiaryEdit.vue
git commit -m "style: 清单完成标记改为绿色复选框，移除背景色"
```

---

### Task 4: 日历点击日期新开详情页

**Files:**
- Create: `diary-vue/src/views/CalendarDetail.vue`
- Modify: `diary-vue/src/router/index.js:22-26`
- Modify: `diary-vue/src/views/Calendar.vue:6,74-91`

- [ ] **Step 1: 创建 CalendarDetail.vue 组件**

创建文件 `diary-vue/src/views/CalendarDetail.vue`：

```vue
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
```

- [ ] **Step 2: 添加路由配置**

在 `diary-vue/src/router/index.js` 的 children 数组中，在 `category` 路由之后添加新路由：

```javascript
      {
        path: 'calendar/:date',
        name: 'CalendarDetail',
        component: () => import('../views/CalendarDetail.vue')
      }
```

完整 children 数组应为：
```javascript
    children: [
      {
        path: 'diary',
        name: 'DiaryEdit',
        component: () => import('../views/DiaryEdit.vue')
      },
      {
        path: 'calendar',
        name: 'Calendar',
        component: () => import('../views/Calendar.vue')
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('../views/Category.vue')
      },
      {
        path: 'calendar/:date',
        name: 'CalendarDetail',
        component: () => import('../views/CalendarDetail.vue')
      }
    ]
```

- [ ] **Step 3: 修改 Calendar.vue 点击日期跳转**

在 `diary-vue/src/views/Calendar.vue` 中：

1. 在 `<script setup>` 中导入 `useRouter`（第41行附近）：
```javascript
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
```

2. 在脚本中添加 router 实例（第46行附近，在 `const diaryDates = ref([])` 之前）：
```javascript
const router = useRouter()
```

3. 替换 `handleDateClick` 函数（第74-91行），改为跳转：
```javascript
async function handleDateClick(date) {
  router.push(`/calendar/${date}`)
}
```

- [ ] **Step 4: 移除 Calendar.vue 中不再需要的弹窗相关代码**

删除 `<template>` 中的 `<el-dialog>` 部分（第13-36行），以及 `<script setup>` 中不再使用的变量和函数：

删除的变量（第48-53行）：
```javascript
const dialogVisible = ref('')
const selectedDiary = ref(null)
const selectedItems = ref([])
const selectedCategories = ref([])
const selectedImages = ref([])
```

删除的函数 `getItemsByCategory`（第59-61行）。

删除不再需要的 import（第43行）：从 `import { getCalendarDates, getCategories, getItems, getContent, getImages } from '../api/diary'` 中移除 `getCategories, getItems, getContent, getImages`，只保留 `getCalendarDates`。

删除不再需要的 import（第44行）：删除 `import { resolveImageUrl } from '../utils/request'`。

简化后的 `handleDateClick`：
```javascript
async function handleDateClick(date) {
  router.push(`/calendar/${date}`)
}
```

简化后的 Calendar.vue 完整 template：
```vue
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
```

简化后的 Calendar.vue 完整 script：
```vue
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
```

简化后的 Calendar.vue 完整 style（删除不再需要的 `.category-group`、`.item-row`、`.done`、`.image-list` 样式）：
```vue
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
```

- [ ] **Step 5: 验证修改**

```bash
cd diary-vue && grep -n "CalendarDetail" src/router/index.js
```
Expected: 输出包含路由定义

```bash
ls src/views/CalendarDetail.vue
```
Expected: 文件存在

```bash
grep -n "dialogVisible\|selectedDiary" src/views/Calendar.vue
```
Expected: 无输出（弹窗代码已移除）

- [ ] **Step 6: 提交**

```bash
git add diary-vue/src/views/CalendarDetail.vue diary-vue/src/router/index.js diary-vue/src/views/Calendar.vue
git commit -m "feat: 日历点击日期跳转详情页替代弹窗"
```

---

### Task 5: 清单修改功能 + 待办/已办Tab

**Files:**
- Modify: `diary-vue/src/views/DiaryEdit.vue` (清单区域重构)

- [ ] **Step 1: 添加编辑状态变量和待办/已办Tab状态**

在 `DiaryEdit.vue` 的 `<script setup>` 中，在 `const newItemContents = reactive({})` 之后添加：

```javascript
const editingItemId = ref(null)
const editContent = ref('')
const checklistTab = ref('pending')
```

- [ ] **Step 2: 添加筛选计算属性**

在添加的变量之后添加：

```javascript
const filteredItems = computed(() => {
  return checklistTab.value === 'pending'
    ? items.value.filter(i => i.isDone === 0)
    : items.value.filter(i => i.isDone === 1)
})

function getFilteredItemsByCategory(categoryId) {
  return filteredItems.value.filter(item => item.categoryId === categoryId)
}
```

- [ ] **Step 3: 添加编辑相关函数**

在 `handleDeleteItem` 函数之后添加：

```javascript
function startEdit(item) {
  editingItemId.value = item.id
  editContent.value = item.content
}

async function saveEdit(item) {
  const newContent = editContent.value.trim()
  if (!newContent || newContent === item.content) {
    editingItemId.value = null
    return
  }
  try {
    await updateItem(item.id, { content: newContent })
    ElMessage.success('修改成功')
    editingItemId.value = null
    loadData()
  } catch (e) {
    console.error('修改清单失败', e)
  }
}

function cancelEdit() {
  editingItemId.value = null
  editContent.value = ''
}
```

- [ ] **Step 4: 在 import 中添加 updateItem**

将第77行：
```javascript
import { getCategories, getItems, createItem, toggleItem, deleteItem,
  getContent, saveContent, getImages, uploadImage, deleteImage } from '../api/diary'
```

替换为：
```javascript
import { getCategories, getItems, createItem, toggleItem, deleteItem, updateItem,
  getContent, saveContent, getImages, uploadImage, deleteImage } from '../api/diary'
```

同时在 `<script setup>` 的 import 中添加 `computed`：
```javascript
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
```

- [ ] **Step 5: 重写清单Tab的template**

将 `<el-tab-pane label="清单" name="checklist">` 部分（第21-46行）替换为：

```vue
      <el-tab-pane label="清单" name="checklist">
        <el-tabs v-model="checklistTab" class="checklist-sub-tabs">
          <el-tab-pane label="待办" name="pending">
            <div v-for="category in categories" :key="category.id" class="category-group">
              <h4>{{ category.name }}</h4>
              <div v-for="item in getFilteredItemsByCategory(category.id)" :key="item.id" class="item-row">
                <el-checkbox v-model="item.isDone" :true-value="1" :false-value="0"
                  @change="handleToggle(item)">
                  <template v-if="editingItemId === item.id">
                    <el-input v-model="editContent" size="small" @keyup.enter="saveEdit(item)"
                      @blur="saveEdit(item)" @keyup.esc="cancelEdit" ref="editInputRef" autofocus />
                  </template>
                  <template v-else>
                    <span class="item-text" @click.stop="startEdit(item)">{{ item.content }}</span>
                  </template>
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
            <el-empty v-if="filteredItems.length === 0" description="暂无待办清单" />
          </el-tab-pane>
          <el-tab-pane label="已办" name="completed">
            <div v-for="category in categories" :key="category.id" class="category-group">
              <h4>{{ category.name }}</h4>
              <div v-for="item in getFilteredItemsByCategory(category.id)" :key="item.id" class="item-row">
                <el-checkbox v-model="item.isDone" :true-value="1" :false-value="0"
                  @change="handleToggle(item)">
                  <template v-if="editingItemId === item.id">
                    <el-input v-model="editContent" size="small" @keyup.enter="saveEdit(item)"
                      @blur="saveEdit(item)" @keyup.esc="cancelEdit" ref="editInputRef" autofocus />
                  </template>
                  <template v-else>
                    <span class="item-text done" @click.stop="startEdit(item)">{{ item.content }}</span>
                  </template>
                </el-checkbox>
                <el-button text type="danger" size="small" @click="handleDeleteItem(item.id)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <el-empty v-if="filteredItems.length === 0" description="暂无已办清单" />
          </el-tab-pane>
        </el-tabs>
      </el-tab-pane>
```

- [ ] **Step 6: 更新 getItemsByCategory 函数**

将第89-91行：
```javascript
function getItemsByCategory(categoryId) {
  return items.value.filter(item => item.categoryId === categoryId)
}
```

替换为（保留原有函数但不再在 template 中使用，可直接删除）：
```javascript
// 已替换为 getFilteredItemsByCategory
```

或者直接删除 `getItemsByCategory` 函数，因为我们已经在 Step 2 中定义了 `getFilteredItemsByCategory`。

- [ ] **Step 7: 添加清单子Tab和编辑样式**

在 `<style scoped>` 区域添加：

```css
.checklist-sub-tabs {
  margin-bottom: 10px;
}
.item-text {
  cursor: pointer;
}
.item-text:hover {
  color: #409eff;
}
```

- [ ] **Step 8: 验证修改**

```bash
cd diary-vue && grep -n "checklistTab" src/views/DiaryEdit.vue
```
Expected: 输出包含 `checklistTab` 变量定义

```bash
grep -n "editingItemId" src/views/DiaryEdit.vue
```
Expected: 输出包含编辑状态管理

```bash
grep -n "getFilteredItemsByCategory" src/views/DiaryEdit.vue
```
Expected: 输出包含筛选函数

- [ ] **Step 9: 提交**

```bash
git add diary-vue/src/views/DiaryEdit.vue
git commit -m "feat: 清单支持编辑功能和待办/已办Tab切换"
```

---

### Task 6: 修复移动端上传413错误

**Files:**
- Modify: `diary-system/diary-gateway/src/main/resources/application.yml`

- [ ] **Step 1: 添加Gateway缓冲区配置**

在 `diary-system/diary-gateway/src/main/resources/application.yml` 的 `spring:` 下添加配置：

```yaml
server:
  port: 8080

spring:
  application:
    name: diary-gateway
  cloud:
    gateway:
      routes:
        - id: diary-user
          uri: http://localhost:8081
          predicates:
            - Path=/api/user/**
        - id: diary-core
          uri: http://localhost:8082
          predicates:
            - Path=/api/diary/**
      httpclient:
        max-in-memory-size: 10MB
  codec:
    max-in-memory-size: 10MB

jwt:
  secret: diary-system-secret-key-must-be-at-least-256-bits-long!!
```

- [ ] **Step 2: 验证修改**

```bash
cd diary-system && grep -n "max-in-memory-size" diary-gateway/src/main/resources/application.yml
```
Expected: 输出两行 `max-in-memory-size: 10MB`

- [ ] **Step 3: 提交**

```bash
git add diary-system/diary-gateway/src/main/resources/application.yml
git commit -m "fix: Gateway缓冲区限制提升到10MB，解决移动端上传413错误"
```

---

### Task 7: 最终验证和提交

- [ ] **Step 1: 启动前端开发服务器验证**

```bash
cd diary-vue && npm run dev
```

检查以下功能：
1. 浏览器标签页标题显示"随风"
2. 页面左上角显示"随风"
3. 导航菜单显示"往事"
4. 清单输入框在移动端不放大
5. 日期选择器无法清空
6. 清单完成标记为绿色复选框
7. 日历点击日期跳转详情页
8. 清单可点击编辑
9. 清单有待办/已办Tab

- [ ] **Step 2: 确认所有改动已提交**

```bash
git status
```
Expected: 工作区干净，无未提交改动

---

## 实施顺序

| 顺序 | Task | 依赖 | 预计耗时 |
|------|------|------|----------|
| 1 | Task 1: 系统命名修改 | 无 | 5分钟 |
| 2 | Task 2: 移动端修复 | 无 | 5分钟 |
| 3 | Task 3: 清单样式优化 | 无 | 5分钟 |
| 4 | Task 4: 日历详情页 | 无 | 15分钟 |
| 5 | Task 5: 清单编辑+Tab | 无 | 20分钟 |
| 6 | Task 6: 修复413错误 | 无 | 3分钟 |
| 7 | Task 7: 最终验证 | Task 1-6 | 10分钟 |

Task 1-6 互相独立，可并行执行。Task 5 最复杂，涉及清单区域的完全重写。
