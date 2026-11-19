# 业务流程测试记录

测试日期：2026-11-19
环境：Windows 11 · JDK 21 · Spring Boot 3.3.5 · MySQL 8.0 · Maven 3.8.8
方式：启动 `campus-repair-1.0.0.jar` 后，用 `scripts/smoke-test.ps1` 发真实 HTTP 请求，并查询数据库核对落库结果。

## 一、单元测试

```text
mvn test
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

覆盖：主流程 4 条合法流转、3 条异常分支、5 条非法流转、状态中文名映射。

## 二、鉴权与越权

| 用例 | 请求 | 期望 | 实际 |
| --- | --- | --- | --- |
| 健康检查免登录 | `GET /api/health` | 200 | 200，`status=UP` |
| 未登录访问 | `GET /api/repair-orders/my` | 401 | 401，`code=401` |
| 查看他人单据 | `stu01` 访问 `stu07` 的单据 12 | 403 | 403，`无权查看该报修单` |
| 本人查看 | `stu07` 访问单据 12 | 200 | 200，返回详情 |
| 技术员新增设备 | `POST /api/devices`（tech01） | 403 | 403，`没有权限执行该操作` |
| 他人验收 | `stu01` 验收 `stu07` 的单 | 403 | 403，`只有报障人本人可以验收` |
| 报障人看统计 | `GET /api/stats/overview`（stu07） | 403 | 403 |

## 三、状态机主流程

测试单：`stu07` 对设备 12 新建报修单。

| 步骤 | 请求 | 期望状态 | 实际 |
| --- | --- | --- | --- |
| 1 | `POST /api/repair-orders` | PENDING | 200，`待受理` |
| 2 | `POST /{id}/accept`（tech01） | INVESTIGATING | 200，`排查中` |
| 3 | `POST /{id}/start-repair`（tech01） | REPAIRING | 200，`修复中` |
| 4 | `POST /{id}/finish`（tech01） | PENDING_CONFIRM | 200，`待确认` |
| 5 | `POST /{id}/confirm` PASS（stu07） | CLOSED | 200，`已关闭` |

### 非法流转

| 用例 | 期望 | 实际 |
| --- | --- | --- |
| 重复受理（已排查中） | 400 | 400，`当前状态为「排查中」，不能执行该操作` |
| 已关闭后再验收 | 400 | 400，`当前状态为「已关闭」，不能执行验收` |
| 非本人补充信息 | 403 | 403，`只有报障人本人可以补充信息` |

### 数据库核对

测试单在 `status_log` 生成 5 条轨迹：

```text
NULL              -> PENDING            报障人提交报修单
PENDING           -> INVESTIGATING      已受理，安排排查
INVESTIGATING     -> REPAIRING          确认故障，开始维修
REPAIRING         -> PENDING_CONFIRM    修复完成，等待报障人确认
PENDING_CONFIRM   -> CLOSED             报障人验收通过，工单关闭
```

`repair_handle` 生成 3 条（ACCEPT / REPAIR / COMPLETE），`confirm_record` 生成 1 条 PASS 记录；
单据关闭后设备状态由 `REPAIRING` 自动恢复为 `NORMAL`。

## 四、异常分支：退回补充

| 步骤 | 请求 | 期望状态 | 实际 |
| --- | --- | --- | --- |
| 1 | `POST /9/return-info`（tech01） | INFO_NEEDED | 200，`待补充` |
| 2 | `POST /9/supplement`（stu01，非本人） | 403 | 403 |
| 3 | `POST /9/supplement`（stu07，本人） | PENDING | 200，`待受理`，故障描述已更新 |

## 五、并发受理（乐观锁）

对同一张 PENDING 单据同时发两个 `accept` 请求：

```text
请求A -> HTTP 200，状态变为 INVESTIGATING
请求B -> HTTP 400，当前状态为「排查中」，不能执行该操作
```

只有一个请求成功，数据库 `version` 只加一次，不会出现重复受理或重复日志。

## 六、分页与统计

| 用例 | 实际 |
| --- | --- |
| `GET /api/repair-orders?status=CLOSED&page=1&size=5`（admin） | 200，分页结构与筛选正确 |
| `GET /api/repair-orders?keyword=图书馆&page=1&size=5`（tech01） | 200，命中单号/描述/设备名/报障人 |
| `GET /api/repair-orders`（stu07） | 200，只返回本人单据 |
| `GET /api/stats/overview`（tech01） | 200，返回总数、未关闭数、平均修复时长、按机房/状态分组 |

## 七、备注

- 测试过程中新建的用户、设备、报修单在测试结束后通过重新导入 `seed.sql` 清理，仓库演示数据保持初始状态。
- 所有响应均为真实运行结果，未手工编造。

## 八、前端页面操作记录

环境：后端 `localhost:8080`，前端 Vite `localhost:5173`，浏览器 Edge。
截图目录：`docs/screenshots/frontend/`。

| 用例 | 操作 | 预期 | 实际 |
| --- | --- | --- | --- |
| 登录 | `admin / 123456` | 进入管理员工作台 | 通过，见 02 |
| 三种角色菜单 | 分别登录 admin、tech01、stu07 | 菜单项不同 | 管理员有用户管理；技术员有统计；报障人没有用户/统计 |
| 报修单列表 | 打开 `/orders` | 分页、筛选正常 | 通过，见 03 |
| 新建报修 | `/orders/create` 选择设备并提交 | 跳转详情 | 通过，见 03b |
| 详情与轨迹 | 打开已关闭工单 | 显示状态轨迹、处理记录、验收记录 | 通过，见 04 |
| 技术员受理按钮 | tech01 打开待受理单 12 | 显示"受理"按钮 | 通过，见 10 |
| 报障人数据隔离 | stu07 打开 `/orders` | 只看到本人单据 | 通过，见 12 |
| 前端路由守卫生效 | stu07 访问 `/users` | 回到工作台 | 通过，见 13 |
| 统计页 | tech01 打开 `/stats` | 卡片、机房分布、趋势正常 | 通过，见 11 |
| 404 | 访问不存在路径 | 显示 404 页面 | 通过，见 14 |
| 构建 | `npm run build` | 构建成功 | 通过，产物在 `frontend/dist` |
