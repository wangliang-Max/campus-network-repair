# 接口说明

基础地址：`http://localhost:8080`
在线文档：启动后访问 `/doc.html`（Knife4j），OpenAPI JSON 见 `doc/api-test/openapi.json`。

## 通用约定

### 认证

除健康检查和登录/注册外，请求头必须携带：

```http
Authorization: Bearer <token>
```

### 统一返回

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

| code | HTTP | 含义 |
| --- | --- | --- |
| 200 | 200 | 成功 |
| 400 | 400 | 参数校验失败 |
| 401 | 401 | 未登录或 Token 过期 |
| 403 | 403 | 角色或数据权限不足 |
| 404 | 404 | 资源不存在 |
| 1001 | 400 | 业务规则不满足（重复提交、非法流转等） |
| 500 | 500 | 系统异常 |

### 分页返回

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [],
    "total": 12,
    "page": 1,
    "size": 10,
    "pages": 2
  }
}
```

## 认证

### POST /api/auth/register

报障人自助注册，成功后直接返回 JWT。

```json
{
  "username": "stu08",
  "password": "123456",
  "realName": "张三",
  "phone": "13800000000"
}
```

### POST /api/auth/login

```json
{
  "username": "admin",
  "password": "123456"
}
```

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "admin",
    "realName": "王磊",
    "roleCode": "ADMIN"
  }
}
```

## 设备

### GET /api/devices

查询参数：`page`、`size`、`keyword`（编号/名称）、`roomName`、`status`。

```bash
curl "http://localhost:8080/api/devices?page=1&size=5&roomName=图书馆机房&status=FAULT" \
  -H "Authorization: Bearer $TOKEN"
```

### GET /api/devices/rooms

返回去重后的机房列表，用于前端筛选。

### POST /api/devices（ADMIN）

```json
{
  "deviceCode": "DEV-013",
  "deviceName": "接入交换机",
  "roomName": "实验楼A1机房",
  "status": "NORMAL",
  "remark": "新增设备"
}
```

`deviceCode` 唯一；`status` 只能为 `NORMAL` / `FAULT` / `REPAIRING`。

### PUT /api/devices/{id}（ADMIN）

请求体同新增。

### DELETE /api/devices/{id}（ADMIN）

设备存在报修记录时返回 400：`该设备已有 N 条报修记录，不能删除`。

## 报修单

### POST /api/repair-orders

```json
{
  "deviceId": 12,
  "faultDesc": "备用光纤收发器无信号",
  "priority": "NORMAL"
}
```

同一报障人对同一设备存在未关闭单据时返回 400：`该设备已有未关闭的报修单，请勿重复提交`。

### GET /api/repair-orders

查询参数：`page`、`size`、`status`、`priority`、`keyword`、`roomName`、`onlyMine`。

- 报障人：无论传什么，只返回自己的单据；
- 技术员/管理员：默认返回全部，可用 `onlyMine=true` 过滤。

### GET /api/repair-orders/{id}

返回详情，`data` 关键字段：

| 字段 | 说明 |
| --- | --- |
| `status` / `statusLabel` | 状态码与中文名 |
| `statusLogs` | 状态变更轨迹（含操作人、备注、时间） |
| `handles` | 技术员处理记录 |
| `confirms` | 验收记录 |
| `allowedActions` | 当前用户可执行的动作，供前端渲染按钮 |

### 状态流转接口

| 接口 | 流转 | 角色 |
| --- | --- | --- |
| `POST /{id}/accept` | PENDING → INVESTIGATING | TECHNICIAN / ADMIN |
| `POST /{id}/start-repair` | INVESTIGATING → REPAIRING | TECHNICIAN / ADMIN |
| `POST /{id}/finish` | REPAIRING → PENDING_CONFIRM | TECHNICIAN / ADMIN |
| `POST /{id}/return-info` | INVESTIGATING → INFO_NEEDED | TECHNICIAN / ADMIN |
| `POST /{id}/supplement` | INFO_NEEDED → PENDING | 报障人本人 |
| `POST /{id}/confirm` | PENDING_CONFIRM → CLOSED / REPAIRING | 报障人本人 |

技术员动作请求体（可空）：

```json
{
  "remark": "已受理，安排排查",
  "content": "电话确认故障现象"
}
```

补充信息：

```json
{ "faultDesc": "补充：每天 8-9 点必现，已录屏" }
```

验收：

```json
{ "result": "PASS", "score": 5, "comment": "恢复很快，满意" }
```

非法流转示例（重复受理）：

```json
{
  "code": 1001,
  "message": "当前状态为「排查中」，不能执行该操作",
  "data": null
}
```

## 统计

### GET /api/stats/overview（TECHNICIAN / ADMIN）

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "totalOrders": 12,
    "openOrders": 8,
    "closedOrders": 4,
    "avgRepairHours": 24.4,
    "byRoom": [
      { "name": "图书馆机房", "count": 2 }
    ],
    "byStatus": [
      { "name": "CLOSED", "count": 4 }
    ]
  }
}
```

平均修复时长 = `AVG(关闭时间 - 提交时间)`，按 `status_log` 中 `to_status='CLOSED'` 的记录计算。
