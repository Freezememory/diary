# Frontend Improvements Design Spec

**Date:** 2026-05-24
**Project:** diary-vue (Diary System Frontend)
**Scope:** 4 UX improvements identified from mobile usage

## Context

The diary system was deployed to a cloud server and tested on mobile. Four issues were identified:
1. Mobile layout is broken — content is squeezed together
2. Date navigation arrows don't refresh the page content
3. Adding checklist items requires selecting a category every time
4. Completed checklist items use strikethrough, which is hard to read

## Improvement 1: Mobile Responsive Layout

**Problem:** No responsive CSS exists. Fixed pixel widths (200px sidebar, `el-col :span="12"`) cause layout to break on narrow screens.

**Solution:** CSS media queries at `max-width: 768px` breakpoint.

### Layout.vue changes:
- Add a hamburger menu button visible only on mobile (`< 768px`)
- Sidebar becomes a slide-out drawer (`el-drawer`) on mobile instead of fixed `el-aside`
- Header remains but shrinks: smaller font, tighter padding
- On desktop (`> 768px`), current layout is preserved

### DiaryEdit.vue changes:
- `el-col :span="12"` → `el-col :xs="24" :sm="24" :md="12"` so columns stack on mobile
- Image thumbnails: `width: 80px; height: 80px` on mobile (down from 100px)
- Date header: flex-wrap for narrow screens

### Login.vue changes:
- Login card width: `width: min(400px, 90vw)` to fit mobile screens

**Files to modify:**
- `diary-vue/src/views/Layout.vue`
- `diary-vue/src/views/DiaryEdit.vue`
- `diary-vue/src/views/Login.vue`

## Improvement 2: Date Switch Refresh

**Problem:** `changeDate(delta)` (line 107-109 in DiaryEdit.vue) only updates `currentDate.value` but does not call `loadData()`. The date picker's `@change` handler calls `loadData()`, but the arrow buttons bypass it.

**Solution:** Add `loadData()` at the end of `changeDate()`.

```javascript
function changeDate(delta) {
  currentDate.value = dayjs(currentDate.value).add(delta, 'day').format('YYYY-MM-DD')
  loadData()
}
```

**Files to modify:**
- `diary-vue/src/views/DiaryEdit.vue` (line 107-109)

## Improvement 3: Inline Add Under Category

**Problem:** Adding a checklist item opens a dialog where user must select a category from a dropdown. This is redundant since items are already grouped by category.

**Solution:** Replace the dialog with an inline input field under each category group.

### Implementation:
- Remove the "新增" button from card header
- Remove the `el-dialog` and related `itemForm` / `itemDialogVisible` state
- Add an inline input + submit button below each category's items
- The `categoryId` is automatically set to the current category
- Press Enter or click button to submit
- After submit, clear the input and refresh data

### UI per category group:
```
[Category Name]
  □ Item 1          [x]
  □ Item 2          [x]
  [输入新内容...    提交]
```

**Files to modify:**
- `diary-vue/src/views/DiaryEdit.vue`

## Improvement 4: Completed Item Style

**Problem:** Completed items use `text-decoration: line-through; color: #999` which is hard to read on mobile.

**Solution:** Change `.done` class to use green background with normal text.

```css
.done {
  background-color: #f0f9eb;
  border-radius: 4px;
  padding: 2px 8px;
  text-decoration: none;
  color: inherit;
}
```

Apply this style to the item row container (not just the text span) for better visual effect.

**Files to modify:**
- `diary-vue/src/views/DiaryEdit.vue` (`.done` class)
- `diary-vue/src/views/Calendar.vue` (`.done` class, keep consistent)

## Verification

1. **Mobile test:** Open browser dev tools, set viewport to 375px width. Verify:
   - Sidebar collapses to hamburger menu
   - Diary columns stack vertically
   - Login card fits screen
   - All text/buttons are tappable (not too small)
2. **Date switch:** Click left/right arrows, verify diary content, checklist, and images update
3. **Inline add:** Under each category, type in the input and press Enter. Verify item appears under correct category
4. **Done style:** Check an item, verify green background appears with normal text. Uncheck, verify it returns to normal
