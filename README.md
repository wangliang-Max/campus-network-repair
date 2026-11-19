# 校园网络与机房故障报修平台（T05）

基于 Spring Boot 3 的校园网络与机房故障报修管理系统：报障人提交报修单，网络技术员受理、排查、修复，报障人验收关闭；全流程状态可追踪，带角色权限、状态机、状态日志与统计报表。

- 课程：JavaWeb 后端开发技术（期末综合项目）
- 选题编号：T05 校园网络与机房故障报修平台
- 本仓库包含**后端 + 前端**，并使用 Docker / Kubernetes 部署

## 功能概览

- **三种角色**：报障人（REPORTER）、网络技术员（TECHNICIAN）、系统管理员（ADMIN）
- **六状态状态机**：待受理 → 排查中 → 修复中 → 待确认 → 已关闭；支持"待补充"退回与"验收不通过"返工
- **工单留痕**：每次流转写 `status_log`，技术员动作写 `repair_handle`，验收写 `confirm_record`
- **权限控制**：JWT 登录 + 拦截器白名单 + `@RequireRole` 角色注解 + 报障人数据隔离
- **并发控制**：`@Version` 乐观锁，防止两人同时受理同一张单
- **查询统计**：多表 JOIN 分页查询、关键字/状态/机房筛选、平均修复时长与按机房分组
- **接口文档**：Knife4j（OpenAPI 3），启动后访问 `/doc.html`
- **前端**：Vue 3 + Vite + Element Plus，10 个页面、5 个复用组件、角色菜单、路由守卫

## 当前进度

| 阶段 | 内容 | 状态 |
| --- | --- | --- |
| 0 | 仓库初始化、README、.gitignore | 已完成 |
| 1 | 数据库设计（7 张表）、ER 图、种子数据 | 已完成 |
| 2 | Spring Boot 骨架、统一返回、全局异常、健康检查 | 已完成 |
| 3 | 注册、登录、BCrypt、JWT、拦截器、角色注解 | 已完成 |
| 4 | 设备 CRUD、状态机、状态日志、乐观锁、越权校验 | 已完成 |
| 5 | 多表 JOIN、条件分页、统计接口 | 已完成 |
| 6 | 接口文档、测试记录、贡献说明、AI 使用声明 | 已完成 |
| 7 | Docker 镜像、Compose、Kubernetes、答辩 PPT 与视频 | 进行中 |

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 / 框架 | Java 21 · Spring Boot 3.3 · Maven |
| 数据层 | MyBatis-Plus 3.5 · MySQL 8 |
| 认证鉴权 | JWT（jjwt 0.12）· BCrypt · HandlerInterceptor + ThreadLocal |
| 接口文档 | Knife4j（OpenAPI 3） |
| 测试 | JUnit 5 · PowerShell 端到端冒烟脚本 |

## 接口清单

除 `/api/health`、`/api/auth/**` 外，其余接口都需要 `Authorization: Bearer <token>`。
当前共 **27 个接口**。

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| GET | `/api/health` | 健康检查 | 公开 |
| POST | `/api/auth/register` | 报障人注册，直接返回 JWT | 公开 |
| POST | `/api/auth/login` | 登录 | 公开 |
| GET | `/api/auth/me` | 当前登录用户信息 | 登录用户 |
| PUT | `/api/auth/password` | 修改当前用户密码 | 登录用户 |
| GET | `/api/devices` | 设备分页查询（keyword/roomName/status） | 登录用户 |
| GET | `/api/devices/rooms` | 机房列表 | 登录用户 |
| GET | `/api/devices/{id}` | 设备详情 | 登录用户 |
| POST | `/api/devices` | 新增设备 | ADMIN |
| PUT | `/api/devices/{id}` | 修改设备 | ADMIN |
| DELETE | `/api/devices/{id}` | 删除设备（有报修记录时拒绝） | ADMIN |
| PUT | `/api/devices/{id}/status` | 修改设备状态 | ADMIN |
| GET | `/api/users` | 用户分页查询 | ADMIN |
| PUT | `/api/users/{id}/enabled` | 启用/禁用用户 | ADMIN |
| GET | `/api/users/technicians` | 技术员列表（派单选择） | 登录用户 |
| POST | `/api/repair-orders` | 提交报修单（同人同设备未关闭时拒绝） | 登录用户 |
| GET | `/api/repair-orders` | 分页查询（报障人只能看自己的） | 登录用户 |
| GET | `/api/repair-orders/my` | 我的报修列表 | 登录用户 |
| GET | `/api/repair-orders/{id}` | 详情：含轨迹/处理记录/验收记录/可执行动作 | 本人或技术员/管理员 |
| POST | `/api/repair-orders/{id}/accept` | 受理：待受理 → 排查中 | TECHNICIAN / ADMIN |
| POST | `/api/repair-orders/{id}/start-repair` | 开始维修：排查中 → 修复中 | TECHNICIAN / ADMIN |
| POST | `/api/repair-orders/{id}/finish` | 修复完成：修复中 → 待确认 | TECHNICIAN / ADMIN |
| POST | `/api/repair-orders/{id}/return-info` | 无法复现：排查中 → 待补充 | TECHNICIAN / ADMIN |
| POST | `/api/repair-orders/{id}/supplement` | 补充信息：待补充 → 待受理 | 报障人本人 |
| POST | `/api/repair-orders/{id}/confirm` | 验收：PASS 关闭 / FAIL 返工 | 报障人本人 |
| GET | `/api/stats/overview` | 统计概览 | TECHNICIAN / ADMIN |
| GET | `/api/stats/trend` | 近 N 天新增/关闭趋势 | TECHNICIAN / ADMIN |

详细请求/响应示例见 [docs/api.md](docs/api.md)。

## 状态机

```text
PENDING(待受理)
   │ accept
   ▼
INVESTIGATING(排查中) ── return-info ──▶ INFO_NEEDED(待补充)
   │ start-repair                              │ supplement
   ▼                                           ▼
REPAIRING(修复中) ◀── confirm:FAIL ── PENDING_CONFIRM(待确认) ── confirm:PASS ──▶ CLOSED(已关闭)
   │ finish
   └──────────────────────────────────▶ PENDING_CONFIRM
```

非法流转（如"待受理"直接关闭、"已关闭"再受理）统一抛出业务异常，HTTP 400 + `code=1001`。

## 目录结构

```text
campus-network-repair/
├── backend/                           # Spring Boot 单模块
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/cnr/
│       │   ├── common/                # Result、PageResult、ErrorCode、OrderStatus 状态机
│       │   ├── config/                # MyBatis-Plus、Knife4j、WebMvc、BCrypt
│       │   ├── controller/            # Auth / Device / RepairOrder / Stats / Health
│       │   ├── service/               # 接口与实现
│       │   ├── mapper/                # MyBatis-Plus Mapper
│       │   ├── entity/ dto/ vo/       # 分层数据对象
│       │   ├── security/              # JWT、拦截器、@RequireRole
│       │   └── exception/             # 业务异常与全局处理
│       ├── main/resources/
│       │   ├── application.yml
│       │   ├── application-dev.yml
│       │   └── mapper/RepairOrderMapper.xml
│       └── test/java/com/cnr/common/OrderStatusTest.java
├── database/                          # schema.sql、seed.sql、ER 图
├── docs/                              # 设计、接口、测试、贡献、答辩材料
├── frontend/                          # Vue 3 + Vite + Element Plus
│   ├── src/                           # api / components / router / stores / views
│   ├── nginx/                         # 生产环境反向代理模板
│   └── Dockerfile                     # Node 构建 -> Nginx
├── scripts/smoke-test.ps1             # 端到端冒烟测试脚本
└── README.md
```

## 环境要求

- JDK 21（**必须**：pom 中 `java.version=21`，JDK 17 构建会报"不支持发行版本 21"）
- Maven 3.6+
- MySQL 8.0+
- Node.js 20+（前端）
- Apifox / Postman（可选）

## 快速开始

### 1. 初始化数据库

```bash
# 注意：schema.sql 开头会 DROP DATABASE campus_repair 后重建
mysql --default-character-set=utf8mb4 -u root -p < database/schema.sql
mysql --default-character-set=utf8mb4 -u root -p < database/seed.sql
```

必须加 `--default-character-set=utf8mb4`，否则命令行客户端按本地字符集解析中文，seed 会报
`Data too long for column 'role_name'`。

### 2. 配置

`backend/src/main/resources/application-dev.yml` 已随仓库提供本地默认值
（`localhost:3306/campus_repair`，`root` / `123456`），可直接启动。需要覆盖时用环境变量：

| 变量名 | 说明 | 默认值 |
| --- | --- | --- |
| `DB_URL` | 数据库连接地址 | 本地 `campus_repair` |
| `DB_USERNAME` | 数据库账号 | `root` |
| `DB_PASSWORD` | 数据库密码 | `123456` |
| `JWT_SECRET` | JWT 签名密钥（≥32 字符） | 开发用默认值 |
| `JWT_EXPIRE_HOURS` | Token 有效期（小时） | `24` |
| `SERVER_PORT` | 服务端口 | `8080` |
| `SPRING_PROFILES_ACTIVE` | 激活环境 | `dev` |

真实密码写在 `application-local.yml`（已被 `.gitignore` 忽略）。

### 3. 构建并启动

```bash
cd backend
mvn clean package
java -jar target/campus-repair-1.0.0.jar
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev          # http://localhost:5173，/api 代理到 8080
npm run build        # 产物在 dist/
```

演示账号：`admin`（管理员）、`tech01`（技术员）、`stu07`（报障人），密码 `123456`。

### 4. 验证

```bash
curl http://localhost:8080/api/health
# {"code":200,"message":"成功","data":{"service":"campus-repair","status":"UP"}}

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

接口文档：<http://localhost:8080/doc.html>

## 演示账号

全部为模拟数据，密码统一为 `123456`：

| 账号 | 角色 |
| --- | --- |
| `admin` | 管理员 |
| `tech01` ~ `tech03` | 网络技术员 |
| `stu01` ~ `stu07` | 报障人 |

## 测试

```bash
cd backend
mvn test          # 状态机单元测试（4 条）
```

端到端冒烟（需先启动应用）：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1
```

覆盖：登录/注册、401/403、设备 CRUD、状态机主流程与异常分支、并发受理、分页筛选、统计。
测试记录见 [docs/business-flow-record.md](docs/business-flow-record.md)。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [docs/database-design.md](docs/database-design.md) | ER 图、表结构、索引说明 |
| [docs/requirements.md](docs/requirements.md) | 角色、功能、状态流转与页面清单 |
| [docs/frontend-design.md](docs/frontend-design.md) | 页面、组件、store、路由与 Axios 设计 |
| [docs/api.md](docs/api.md) | 接口清单与请求/响应示例 |
| [docs/business-flow-record.md](docs/business-flow-record.md) | 主流程/异常/越权测试记录 |
| [docs/contribution-table.md](docs/contribution-table.md) | 分工与提交记录 |
| [docs/ai-usage-statement.md](docs/ai-usage-statement.md) | AI 工具使用说明 |
| [docs/defense-outline.md](docs/defense-outline.md) | 答辩提纲与常见问题 |
| [docs/api-doc/openapi.json](docs/api-doc/openapi.json) | 导出的 OpenAPI 3 文档 |
| [docs/screenshots/frontend/](docs/screenshots/frontend/) | 前端页面截图 |

## 数据说明

- 全部为模拟数据，不含真实个人信息、真实密码或密钥
- 默认密码与 JWT 密钥仅用于本地开发，部署时通过环境变量覆盖

## 版本管理

- 按功能块提交，提交信息使用 `feat:` / `fix:` / `docs:` / `test:` 前缀
- 答辩冻结版本：`git tag -a v1.0.0 -m "答辩冻结版本"` 后 `git push origin --tags`

## 后续计划

- [ ] 答辩 PPT 与 2 分钟演示视频（阶段 7）
