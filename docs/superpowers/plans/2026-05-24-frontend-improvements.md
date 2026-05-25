# Frontend Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix 4 frontend UX issues: mobile responsiveness, date switch refresh, inline checklist add, and completed item styling.

**Architecture:** Pure CSS/JS changes to existing Vue 3 SFCs. No new files, no new dependencies. Changes are scoped to 4 files: Layout.vue, DiaryEdit.vue, Calendar.vue, Login.vue.

**Tech Stack:** Vue 3 (Composition API, `<script setup>`), Element Plus, CSS media queries

---

## File Map

| File | Changes |
|------|---------|
| `diary-vue/src/views/Layout.vue` | Mobile sidebar (hamburger + drawer), responsive header |
| `diary-vue/src/views/DiaryEdit.vue` | Responsive columns, date refresh fix, inline add input, green done style |
| `diary-vue/src/views/Calendar.vue` | Green done style (consistent with DiaryEdit) |
| `diary-vue/src/views/Login.vue` | Responsive card width |

---

## Task 1: Date Switch Bug Fix (DiaryEdit.vue)

**Why first:** Smallest change, easy to verify, zero risk of breaking anything.

**Files:**
- Modify: `diary-vue/src/views/DiaryEdit.vue:107-109`

- [ ] **Step 1: Add loadData() call in changeDate()**

In `DiaryEdit.vue`, find `changeDate` function (line 107-109) and add `loadData()`:

```javascript
function changeDate(delta) {
  currentDate.value = dayjs(currentDate.value).add(delta, 'day').format('YYYY-MM-DD')
  loadData()
}
```

- [ ] **Step 2: Verify**

Click left/right arrow buttons in the diary page. Confirm checklist, text content, and images all update to reflect the selected date.

- [ ] **Step 3: Commit**

```bash
git add diary-vue/src/views/DiaryEdit.vue
git commit -m "fix: refresh diary content when using date navigation arrows"
```

---

## Task 2: Completed Item Green Background Style (DiaryEdit.vue + Calendar.vue)

**Files:**
- Modify: `diary-vue/src/views/DiaryEdit.vue:245-248` (`.done` class)
- Modify: `diary-vue/src/views/Calendar.vue:124-127` (`.done` class)

- [ ] **Step 1: Update .done class in DiaryEdit.vue**

Replace the `.done` class in `DiaryEdit.vue` `<style scoped>` section (lines 245-248):

```css
.done {
  background-color: #f0f9eb;
  border-radius: 4px;
  padding: 2px 8px;
}
```

- [ ] **Step 2: Update .done class in Calendar.vue**

Replace the `.done` class in `Calendar.vue` `<style scoped>` section (lines 124-127) with the identical style:

```css
.done {
  background-color: #f0f9eb;
  border-radius: 4px;
  padding: 2px 8px;
}
```

- [ ] **Step 3: Update item-row in DiaryEdit.vue for better visual**

In `DiaryEdit.vue`, update the `.item-row` class to wrap content and give it some padding so the green background looks good:

```css
.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 0;
}
```

No change needed here — the existing style already works. The `.done` class on the span inside the checkbox label will show the green background.

- [ ] **Step 4: Verify**

Check a checklist item — green background should appear with normal text (no strikethrough, no gray). Uncheck it — returns to normal.

- [ ] **Step 5: Commit**

```bash
git add diary-vue/src/views/DiaryEdit.vue diary-vue/src/views/Calendar.vue
git commit -m "style: change completed item style to green background"
```

---

## Task 3: Inline Add Under Category (DiaryEdit.vue)

**Files:**
- Modify: `diary-vue/src/views/DiaryEdit.vue` (template + script + style)

- [ ] **Step 1: Remove the "新增" button and dialog from template**

In `DiaryEdit.vue` template, remove:
1. The "新增" button in the checklist card header (line 20)
2. The entire `el-dialog` block (lines 69-84)

The card header becomes just `<span>清单</span>`.

- [ ] **Step 2: Add inline input under each category group**

In the template, inside the `v-for="category in categories"` loop, after the items `v-for`, add an inline input row:

```html
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
```

- [ ] **Step 3: Update script — remove dialog state, add inline state**

Remove these from `<script setup>`:
- `itemDialogVisible` ref (line 100)
- `itemForm` reactive (line 101)
- `showAddItem` function (lines 129-133)
- The old `handleAddItem` function (lines 135-152)

Add new state and function:

```javascript
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'

// ... existing refs ...

const newItemContents = reactive({})

// Initialize new item inputs when categories load
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
```

- [ ] **Step 4: Add style for inline input row**

Add to `<style scoped>`:

```css
.add-item-row {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
.add-item-row .el-input {
  flex: 1;
}
```

- [ ] **Step 5: Verify**

Under each category, type content in the input and press Enter or click "提交". Item should appear under that category. Empty submission should show warning. Input should clear after successful add.

- [ ] **Step 6: Commit**

```bash
git add diary-vue/src/views/DiaryEdit.vue
git commit -m "feat: add checklist items inline under category instead of dialog"
```

---

## Task 4: Mobile Responsive Layout

**Files:**
- Modify: `diary-vue/src/views/Layout.vue`
- Modify: `diary-vue/src/views/DiaryEdit.vue`
- Modify: `diary-vue/src/views/Login.vue`

- [ ] **Step 1: Update Layout.vue — hamburger menu + drawer for mobile**

Replace the entire `Layout.vue` template and styles with:

```vue
<template>
  <el-container class="layout-container">
    <el-header>
      <div class="header-left">
        <el-icon class="hamburger" @click="sidebarVisible = true"><Operation /></el-icon>
        <h1>日记系统</h1>
      </div>
      <div class="header-right">
        <span>{{ userStore.userInfo?.nickname }}</span>
        <el-button text @click="handleLogout">退出</el-button>
      </div>
    </el-header>
    <el-container>
      <!-- Desktop sidebar -->
      <el-aside width="200px" class="desktop-aside">
        <el-menu :default-active="route.path" router>
          <el-menu-item index="/diary">
            <el-icon><Edit /></el-icon>
            <span>今日日记</span>
          </el-menu-item>
          <el-menu-item index="/calendar">
            <el-icon><Calendar /></el-icon>
            <span>日历查看</span>
          </el-menu-item>
          <el-menu-item index="/category">
            <el-icon><Setting /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <!-- Mobile drawer -->
      <el-drawer v-model="sidebarVisible" direction="ltr" size="200px" :show-close="false">
        <el-menu :default-active="route.path" router @select="sidebarVisible = false">
          <el-menu-item index="/diary">
            <el-icon><Edit /></el-icon>
            <span>今日日记</span>
          </el-menu-item>
          <el-menu-item index="/calendar">
            <el-icon><Calendar /></el-icon>
            <span>日历查看</span>
          </el-menu-item>
          <el-menu-item index="/category">
            <el-icon><Setting /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
        </el-menu>
      </el-drawer>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const sidebarVisible = ref(false)

onMounted(async () => {
  try {
    await userStore.fetchUserInfo()
  } catch {
    userStore.logout()
    router.push('/login')
  }
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.hamburger {
  display: none;
  font-size: 20px;
  cursor: pointer;
}
.desktop-aside {
  border-right: 1px solid #eee;
}

@media (max-width: 768px) {
  .hamburger {
    display: block;
  }
  .desktop-aside {
    display: none;
  }
  .el-header h1 {
    font-size: 16px;
  }
}
</style>
```

Key changes:
- Hamburger icon (Operation) visible only on mobile (`@media max-width: 768px`)
- Desktop sidebar preserved as-is
- Mobile uses `el-drawer` that slides in from left
- Drawer auto-closes on menu select (`@select="sidebarVisible = false"`)
- `import { ref }` added (was not imported before since no refs were used in script)

- [ ] **Step 2: Update DiaryEdit.vue — responsive columns**

In `DiaryEdit.vue` template, change the `el-col` components from fixed span to responsive:

```html
<!-- Before -->
<el-col :span="12">

<!-- After -->
<el-col :xs="24" :sm="24" :md="12">
```

Both `el-col` elements (line 15 and line 38) need this change.

- [ ] **Step 3: Update DiaryEdit.vue — responsive image grid**

In `DiaryEdit.vue` `<style scoped>`, add responsive styles:

```css
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
```

- [ ] **Step 4: Update Login.vue — responsive card width**

In `Login.vue` `<style scoped>`, change `.login-card` width:

```css
.login-card {
  width: min(400px, 90vw);
}
```

- [ ] **Step 5: Verify on mobile viewport**

Open browser dev tools → set viewport to 375px width:
- Layout: hamburger visible, sidebar hidden, content full width
- Click hamburger: drawer slides in with nav items
- DiaryEdit: columns stacked vertically, images smaller
- Login: card fits within 90% of screen width

- [ ] **Step 6: Commit**

```bash
git add diary-vue/src/views/Layout.vue diary-vue/src/views/DiaryEdit.vue diary-vue/src/views/Login.vue
git commit -m "feat: add mobile responsive layout with hamburger menu"
```

---

## Verification Checklist

After all tasks are complete, verify end-to-end:

1. **Mobile (375px):**
   - [ ] Hamburger menu opens drawer
   - [ ] Diary columns stack vertically
   - [ ] Login card fits screen
   - [ ] All buttons/inputs are tappable (not too small)

2. **Desktop (1200px+):**
   - [ ] Sidebar shows normally
   - [ ] Diary columns side-by-side
   - [ ] All existing functionality preserved

3. **Date navigation:**
   - [ ] Left/right arrows refresh all content

4. **Inline add:**
   - [ ] Input under each category works
   - [ ] Enter key submits
   - [ ] Empty input shows warning
   - [ ] Input clears after success

5. **Done style:**
   - [ ] Checked items show green background
   - [ ] No strikethrough or gray text
   - [ ] Unchecking removes green background
   - [ ] Same style in Calendar view dialog
