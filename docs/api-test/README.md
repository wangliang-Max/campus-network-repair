# 接口测试集合

## Postman

1. 打开 Postman，Import -> File，选择 `postman-collection.json`。
2. 确认集合变量 `baseUrl = http://localhost:8080`。
3. 先运行「01 认证 / 登录（admin）」，复制返回的 `data.token` 到集合变量 `token`；
   或在集合的 Authorization 中统一使用 `{{token}}`。
4. 按「设备 -> 报修单 -> 统计」顺序运行。

集合覆盖：

- 主流程：提交 -> 受理 -> 维修 -> 完成 -> 验收
- 异常流程：重复受理 400、关闭后再验收 400、验收不通过返工
- 越权：报障人查看他人单据 403、技术员新增设备 403、报障人看统计 403
- 未登录：不带 Token 返回 401

## 实际测试结果

测试记录见 `../business-flow-record.md`，脚本方式见仓库根目录 `scripts/smoke-test.ps1`。

运行脚本：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1
```

最近一次运行：23 个断言全部通过。
