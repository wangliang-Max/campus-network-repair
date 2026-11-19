# 前端设计说明

## 1. 技术栈

- Vue 3（Composition API + `<script setup>`）
- Vite 6
- Vue Router 4
- Pinia 2
- Axios
- Element Plus

## 2. 目录结构

```text
frontend/
├── src/
│   ├── api/            # auth / device / order / user / stats
│   ├── components/     # 5 个复用组件
│   ├── layouts/        # MainLayout 侧边栏 + 顶栏 + 内容区
│   ├── router/         # 嵌套路由、动态路由、守卫
│   ├── stores/         # user、app 两个 Pinia store
│   ├── styles/         # 全局样式与主题变量
│   ├── utils/          # axios 封装、时间格式化
│   └── views/          # 10 个页面
├── nginx/              # 生产环境反向代理模板
├── Dockerfile          # Node 构建 -> Nginx
└── vite.config.js      # /api 代理
```

## 3. 页面与路由

| 页面 | 路由 | 角色 | 说明 |
| --- | --- | --- | --- |
| 登录 | `/login` | 公开 | 登录 / 报障人注册切换 |
| 工作台 | `/dashboard` | 全部 | 统计卡片 + 最近工单 |
| 报修单列表 | `/orders` | 全部 | 条件筛选 + 分页 |
| 新建报修 | `/orders/create` | REPORTER / ADMIN | 设备下拉 + 故障描述 |
| 报修单详情 | `/orders/:id` | 本人或技术员/管理员 | 轨迹、处理记录、验收、动作按钮 |
| 设备管理 | `/devices` | 全部（写操作 ADMIN） | 增删改查、状态切换 |
| 用户管理 | `/users` | ADMIN | 启用/禁用账号 |
| 统计报表 | `/stats` | TECHNICIAN / ADMIN | 概览、机房分布、7 天趋势 |
| 个人中心 | `/profile` | 全部 | 资料与修改密码 |
| 404 | `/:pathMatch(.*)*` | 公开 | 未匹配路由 |

共 10 个页面，满足"不少于 8 个"的要求。

## 4. 角色菜单

| 菜单 | 报障人 | 技术员 | 管理员 |
| --- | --- | --- | --- |
| 工作台 | 是 | 是 | 是 |
| 报修单 | 是 | 是 | 是 |
| 设备管理 | 是（只读） | 是（只读） | 是（可写） |
| 统计报表 | 否 | 是 | 是 |
| 用户管理 | 否 | 否 | 是 |
| 个人中心 | 是 | 是 | 是 |

路由守卫读取 `meta.roles`，无权限时回到工作台；后端仍会再校验一次，前端守卫只做体验优化。

## 5. 复用组件

| 组件 | props | emits | 插槽 | 用途 |
| --- | --- | --- | --- | --- |
| `PageHeader` | title、subtitle | - | `actions` | 页面标题与右侧操作区 |
| `StatCard` | label、value、hint、tone | - | - | 工作台/统计卡片 |
| `StatusTag` | status、label | - | - | 工单状态、优先级、设备状态 |
| `SearchBar` | - | search、reset | 默认插槽 | 筛选条件容器 |
| `DeviceFormDialog` | modelValue、device | update:modelValue、saved | - | 新增与编辑共用弹窗 |

## 6. 状态管理

### user store

- `token`、`profile`
- `login()`：调用登录接口，写入 token 与用户信息
- `fetchProfile()`：刷新页面后恢复用户信息
- `logout()`：清理 token、profile、localStorage
- 登录状态持久化到 `localStorage`，刷新不丢失

### app store

- `sidebarCollapsed`：侧边栏折叠状态，持久化到 `localStorage`

## 7. Axios 封装

- `baseURL = /api`，开发期由 Vite proxy 转发到 `localhost:8080`
- 请求拦截器自动携带 `Authorization: Bearer <token>`
- 响应拦截器统一解包 `Result`：`code=200` 返回 `data`
- 401：清理登录状态并跳转 `/login?redirect=原路径`
- 403 / 500 / 网络错误：ElMessage 提示
- 提交按钮使用 `loading` 防重复点击

## 8. 页面状态

- 列表：`v-loading` 加载中、`empty-text` 空数据、请求失败由拦截器提示
- 表单：Element Plus 校验规则，错误提示在字段下方
- 详情：无轨迹/无处理记录时使用 `el-empty`
- 统计：数据为空时使用 `el-empty`

## 9. 构建与部署

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173
npm run build    # 产物在 dist/
```

- 生产环境使用 Nginx 托管 `dist/`，`try_files` 支持刷新不 404
- `/api/` 反向代理到后端，`BACKEND_HOST` 由环境变量注入
- Dockerfile 使用 Node 构建 + Nginx 运行的多阶段构建

## 10. 页面截图

截图位于 `docs/screenshots/frontend/`：

| 文件 | 内容 |
| --- | --- |
| 01-login.png | 登录页 |
| 02-dashboard-admin.png | 管理员工作台 |
| 03-orders-list.png | 报修单列表 |
| 03b-order-create.png | 新建报修 |
| 04-order-detail-admin.png | 报修单详情 |
| 05-devices.png | 设备管理 |
| 06-users.png | 用户管理 |
| 07-stats.png | 统计报表 |
| 08-profile.png | 个人中心 |
| 09-dashboard-tech.png | 技术员工作台 |
| 10-order-detail-tech.png | 技术员查看待受理单 |
| 11-stats-tech.png | 技术员统计 |
| 12-orders-reporter.png | 报障人只能看自己的单 |
| 13-reporter-guard.png | 报障人访问用户管理被拦回工作台 |
| 14-404.png | 404 页面 |
