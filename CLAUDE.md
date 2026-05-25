# CLAUDE.md - Diary 项目指南

## 项目概述

随风平台（Diary System），前后端分离架构。

- **后端：** Java 17 / Spring Boot 3.2.5 / Spring Cloud Gateway 微服务架构
- **前端：** Vue 3 + Vite + Element Plus + Pinia
- **数据库：** MySQL
- **构建工具：** Maven（后端）、npm（前端）

## 项目结构

```
diary/
├── diary-system/                    # Java 后端（Maven 多模块）
│   ├── diary-common/                # 公共模块：R<T> 响应封装、异常处理、工具类
│   ├── diary-gateway/               # Spring Cloud Gateway（端口 8080），JWT 认证过滤器
│   ├── diary-user/                  # 用户微服务（端口 8081）：注册、登录、用户信息
│   └── diary-core/                  # 日记核心微服务（端口 8082）：内容、分类、图片、日历
├── diary-vue/                       # Vue 3 前端
│   └── src/
│       ├── api/                     # API 接口层
│       ├── stores/                  # Pinia 状态管理
│       ├── views/                   # 页面组件
│       ├── utils/                   # Axios 请求封装（JWT 拦截器）
│       └── router/                  # 路由配置（含鉴权守卫）
└── docs/                            # 文档和 SQL 脚本
```

## 后端微服务路由

Gateway 统一入口（端口 8080）：
- `/api/user/**` → diary-user（端口 8081）
- `/api/diary/**` → diary-core（端口 8082）

## 代码规范

### Java 后端
- **分层架构：** Controller → Service（接口）→ ServiceImpl → Mapper
- **响应封装：** 统一使用 `R<T>` 类，`R.ok(data)` 成功，`R.fail(msg)` 失败
- **ORM：** MyBatis-Plus + `LambdaQueryWrapper`，无 XML mapper
- **实体注解：** `@TableName`、`@TableId(type = IdType.AUTO)`、`@TableField(fill = FieldFill.INSERT)`
- **Lombok：** `@Data`、`@RequiredArgsConstructor`、`@Getter` 广泛使用
- **JWT 认证：** Gateway 过滤器校验 token，注入 `X-User-Id` 请求头
- **包命名：** `com.diary.{module}`
- **异常处理：** `BusinessException` + `GlobalExceptionHandler`

### Vue 前端
- Vue 3 Composition API + `<script setup>` 语法
- Pinia setup store 模式
- Element Plus UI（中文语言包）
- Axios 统一请求封装，含 JWT 拦截器
- Token 存储于 `localStorage`

## ⚠️ 核心规则

### 会话结束自动提交代码

每次任务完成后，**必须**自动执行以下步骤：

1. `git add` 所有变更文件
2. `git commit` 提交，commit message 使用中文简要描述变更内容

示例流程：
```bash
git add -A
git commit -m "描述变更内容"
```
除非用户明确要求不提交，否则始终执行此规则。

### 安全红线（必须遵守）
- 严禁执行任何删除文件（rm）或格式化相关的命令。
- 如果需要删除文件，必须先向我请示，经过我确认后才能生成具体命令。
- 禁止修改 `.env` 等包含敏感凭证的文件。
