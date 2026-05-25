# 前端优化设计方案

## 概述

本文档描述日记系统（随风平台）的8项前端优化需求，包括命名修改、交互优化、功能新增和bug修复。

## 需求清单

1. 系统命名修改：全局替换"日记系统"为"随风"，"日历查看"改为"往事"
2. 移动端清单输入框放大问题修复
3. 日历点击日期新开详情页面
4. 日期选择器不可为空
5. 清单完成后标记优化
6. 清单修改功能
7. 清单待办/已办Tab切换
8. 移动端上传413错误修复

---

## 1. 系统命名修改

### 目标
将前端所有"日记系统"替换为"随风"，导航菜单"日历查看"改为"往事"。

### 修改范围

| 文件 | 修改内容 |
|------|----------|
| `diary-vue/src/views/Layout.vue` | 标题 `<h1>日记系统</h1>` → `<h1>随风</h1>` |
| `diary-vue/src/views/Login.vue` | 标题 `<h2>日记系统</h2>` → `<h2>随风</h2>` |
| `diary-vue/index.html` | `<title>` 标签内容改为"随风" |
| `diary-vue/src/views/Layout.vue` | 导航菜单"日历查看" → "往事" |

### 实施步骤
1. 使用全局搜索找到所有"日记系统"出现的位置
2. 逐一替换为"随风"
3. 修改导航菜单文字

---

## 2. 移动端清单输入框放大问题

### 问题描述
在iOS移动端，当用户点击清单输入框时，Safari会自动放大页面，导致用户体验不佳。

### 解决方案
为清单输入框设置 `font-size: 16px`，这是iOS的标准做法——当输入框字体小于16px时，Safari会自动放大。

### 修改文件
- `diary-vue/src/views/DiaryEdit.vue`

### CSS修改
```css
.checklist-input .el-input__inner {
  font-size: 16px;
}
```

---

## 3. 日历点击日期新开详情页

### 目标
点击日历中的具体日期时，不再弹出对话框，而是跳转到新的详情页面。

### 路由设计
- 新增路由：`/calendar/:date`
- 路由参数格式：`YYYY-MM-DD`（如 `2026-05-25`）

### 新增组件
创建 `diary-vue/src/views/CalendarDetail.vue`，复用原弹窗内容：
- 清单列表（只读展示）
- 日记内容
- 图片展示

### 修改文件
| 文件 | 修改内容 |
|------|----------|
| `diary-vue/src/router/index.js` | 添加 `/calendar/:date` 路由 |
| `diary-vue/src/views/Calendar.vue` | 点击日期时 `router.push('/calendar/' + date)` |
| `diary-vue/src/views/CalendarDetail.vue` | 新建组件，展示指定日期的完整内容 |

### CalendarDetail.vue 布局
```
┌─────────────────────────────────┐
│ ← 返回    2026-05-25    [图片]  │
├─────────────────────────────────┤
│ 清单                            │
│ ☑ 已完成项目1                    │
│ ☐ 待办项目2                      │
├─────────────────────────────────┤
│ 日记内容                        │
│ 今天发生了...                    │
├─────────────────────────────────┘
│ [图片轮播]                      │
└─────────────────────────────────┘
```

---

## 4. 日期选择器不可为空

### 目标
日期选择器默认显示当天日期，且不允许清空。

### 解决方案
在 `el-date-picker` 组件上添加 `:clearable="false"` 属性。

### 修改文件
- `diary-vue/src/views/DiaryEdit.vue`

### 代码修改
```vue
<el-date-picker
  v-model="currentDate"
  type="date"
  format="YYYY-MM-DD"
  value-format="YYYY-MM-DD"
  :clearable="false"
  @change="onDateChange"
/>
```

---

## 5. 清单完成后标记优化

### 目标
- 复选框完成时显示绿色
- 移除清单内容的背景色

### 修改文件
- `diary-vue/src/views/DiaryEdit.vue`

### 样式修改
```css
/* 移除现有的 .done 背景色样式 */
/* .done { background-color: #f0f9eb; } 删除 */

/* 复选框绿色样式 */
.el-checkbox.is-checked .el-checkbox__inner {
  background-color: #67c23a;
  border-color: #67c23a;
}
```

---

## 6. 清单修改功能

### 目标
点击清单文字后进入编辑模式，可修改清单内容。

### 交互设计
1. 显示状态：展示清单文字 + 编辑图标
2. 点击后：切换为输入框，自动聚焦
3. 回车或失焦：保存修改，切回显示状态
4. ESC键：取消修改，恢复原内容

### 修改文件
| 文件 | 修改内容 |
|------|----------|
| `diary-vue/src/views/DiaryEdit.vue` | 添加编辑状态管理 |
| `diary-vue/src/api/diary.js` | 添加更新清单项API（调用现有PUT接口） |

### 后端API（已存在）
- `PUT /api/diary/items/{id}` - 更新清单项内容
- `PUT /api/diary/items/{id}/toggle` - 切换完成状态

### 数据流
```
点击清单文字 → editingItemId = item.id → 显示input
         ↓
回车/失焦 → 调用API更新 → editingItemId = null → 显示文字
```

---

## 7. 清单待办/已办Tab

### 目标
在清单区域顶部添加"待办"和"已办"两个Tab，直观展示清单完成情况。

### 交互设计
- Tab位置：清单区域顶部，"清单"标签页内部
- 默认Tab：待办
- 待办Tab：显示 `completed = false` 的清单项
- 已办Tab：显示 `completed = true` 的清单项
- 完成/取消完成操作后，自动更新对应Tab的列表

### 修改文件
- `diary-vue/src/views/DiaryEdit.vue`

### 布局结构
```
┌─────────────────────────────────┐
│ [清单] [日记]                     │ ← 现有Tab
├─────────────────────────────────┤
│ [待办 (3)] [已办 (2)]            │ ← 新增子Tab
├─────────────────────────────────┤
│ 分类A                            │
│   ☐ 项目1                        │
│   ☐ 项目2                        │
│ 分类B                            │
│   ☐ 项目3                        │
└─────────────────────────────────┘
```

### 状态管理
```javascript
const activeTab = ref('pending') // 'pending' | 'completed'
const filteredItems = computed(() => {
  return activeTab.value === 'pending' 
    ? items.value.filter(i => !i.completed)
    : items.value.filter(i => i.completed)
})
```

---

## 8. 移动端上传413错误

### 问题描述
移动端上传图片时返回413 (Request Entity Too Large) 错误。

### 根因分析
Spring Cloud Gateway使用WebFlux响应式栈，默认缓冲区大小限制约为1MB，超过此限制会返回413。

### 解决方案
调整Gateway的缓冲区配置。

### 修改文件
- `diary-system/diary-gateway/src/main/resources/application.yml`

### 配置修改
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        max-in-memory-size: 10MB
  codec:
    max-in-memory-size: 10MB
```

---

## 实施优先级

1. **P0 - 紧急修复**
   - 8. 移动端上传413错误（影响核心功能）

2. **P1 - 体验优化**
   - 2. 移动端清单输入框放大问题
   - 4. 日期选择器不可为空
   - 5. 清单完成后标记优化

3. **P2 - 功能增强**
   - 3. 日历点击日期新开详情页
   - 6. 清单修改功能
   - 7. 清单待办/已办Tab

4. **P3 - 命名调整**
   - 1. 系统命名修改

---

## 测试要点

1. **命名修改**：检查所有页面是否正确显示"随风"和"往事"
2. **移动端输入框**：在iOS设备上测试清单输入框不再放大
3. **日历详情页**：测试日期跳转和内容展示
4. **日期选择器**：确认无法清空日期
5. **清单样式**：检查完成状态的复选框和内容样式
6. **清单编辑**：测试点击编辑、保存、取消功能
7. **待办/已办Tab**：测试Tab切换和数据过滤
8. **文件上传**：在移动端测试上传1MB以上图片

---

## 涉及文件汇总

### 前端文件
- `diary-vue/src/views/Layout.vue`
- `diary-vue/src/views/Login.vue`
- `diary-vue/src/views/DiaryEdit.vue`
- `diary-vue/src/views/Calendar.vue`
- `diary-vue/src/views/CalendarDetail.vue`（新建）
- `diary-vue/src/router/index.js`
- `diary-vue/index.html`

### 后端文件
- `diary-system/diary-gateway/src/main/resources/application.yml`
- `diary-system/diary-core/.../controller/ItemController.java`（可能需要新增PUT接口）
