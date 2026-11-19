# 校园网络与机房故障报修平台（T05）

基于 Spring Boot 3 的校园网络与机房故障报修管理系统：报障人提交报修单，网络技术员受理、排查、修复，报障人确认关闭，全流程状态可追踪。

- 课程：JavaWeb 后端开发技术（期末综合项目）
- 选题编号：T05 校园网络与机房故障报修平台
- 本仓库仅包含**后端**部分

## 当前进度

| 阶段 | 内容 | 状态 |
| --- | --- | --- |
| 0 | 仓库初始化、README、.gitignore | 已完成 |
| 1 | 数据库设计（7 张表）、ER 图、种子数据 | 已完成 |
| 2 | Spring Boot 工程骨架、统一返回、全局异常、健康检查 | 已完成 |
| 3 | 登录、BCrypt、JWT、拦截器白名单 | 已完成（注册、角色注解待补） |
| 4 | 报修单创建（防重复）、详情（越权校验）、我的列表 | 进行中：设备 CRUD、状态流转、状态日志、乐观锁待补 |
| 5 | 多表 JOIN 查询、条件分页、统计接口 | 待开发 |
| 6 | 接口文档、Apifox 测试记录、贡献说明 | 待开发 |
| 7 | 答辩冻结版本（tag）、演示材料 | 待开发 |

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 / 框架 | Java 21 · Spring Boot 3.3 · Maven |
| 数据层 | MyBatis-Plus · MySQL 8 |
| 认证鉴权 | JWT（jjwt）· BCrypt（spring-security-crypto）· 拦截器 + ThreadLocal |
| 接口文档 | Knife4j（OpenAPI 3） |

## 接口清单（已实现）

| 方法 | 路径 | 说明 | 是否需要登录 |
| --- | --- | --- | --- |
| GET | `/api/health` | 健康检查 | 否 |
| POST | `/api/auth/login` | 登录，返回 JWT | 否 |
| POST | `/api/repair-orders` | 提交报修单 | 是 |
| GET | `/api/repair-orders/{id}` | 报修单详情（本人或技术员/管理员） | 是 |
| GET | `/api/repair-orders/my` | 我的报修列表 | 是 |

请求头需携带 `Authorization: Bearer <token>`，其余接口按阶段 4/5 继续补充。

## 目录结构

```text
campus-network-repair/
├── cnr-java/                  # 后端源码（Spring Boot 单模块）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/cnr/      # controller / service / mapper / entity / dto / vo / config / security
│       └── resources/         # application.yml、application-dev.yml
├── database/
│   ├── schema.sql             # 建库建表（7 张表）
│   ├── seed.sql               # 模拟数据
│   └── er.mmd、er-diagram.png # ER 图源文件与导出图
├── doc/
│   └── database-design.md     # 表结构、索引、状态枚举说明
└── README.md
```

## 环境要求

- JDK 21（**必须**：pom 中 `java.version=21`，用 JDK 17 构建会报"不支持发行版本 21"）
- Maven 3.6+
- MySQL 8.0+
- Apifox 或 Postman（接口测试）

## 快速开始

### 1. 初始化数据库

```bash
# 注意：schema.sql 开头会 DROP DATABASE campus_repair 后重建，执行前确认没有需要保留的数据
mysql --default-character-set=utf8mb4 -u root -p < database/schema.sql
mysql --default-character-set=utf8mb4 -u root -p < database/seed.sql
```

必须加 `--default-character-set=utf8mb4`，否则命令行客户端按本地字符集解析中文，seed 会报
`Data too long for column 'role_name'`。用 Navicat / Workbench 导入时同样选择 utf8mb4。

### 2. 数据库配置

`cnr-java/src/main/resources/application-dev.yml` 已随仓库提供本地默认值
（`localhost:3306/campus_repair`，账号 `root`，密码 `123456`），可直接启动。
如与本机不一致，用环境变量覆盖即可，无需改文件：

| 变量名 | 说明 | 示例 |
| --- | --- | --- |
| `DB_URL` | 数据库连接地址 | `jdbc:mysql://localhost:3306/campus_repair?...` |
| `DB_USERNAME` | 数据库账号 | `root` |
| `DB_PASSWORD` | 数据库密码 | 自定义 |
| `JWT_SECRET` | JWT 签名密钥（≥32 字符） | 自定义 |
| `JWT_EXPIRE_HOURS` | Token 有效期（小时） | `24` |
| `SERVER_PORT` | 服务端口 | `8080` |
| `SPRING_PROFILES_ACTIVE` | 激活环境 | `dev` |

需要放真实密码时写在 `application-local.yml`，该文件已被 `.gitignore` 忽略。

### 3. 构建并启动

```bash
cd cnr-java
mvn clean package
java -jar target/campus-repair-1.0.0.jar
```

### 4. 验证

```bash
curl http://localhost:8080/api/health
# {"code":200,"message":"成功","data":{"service":"campus-repair","status":"UP"}}

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

接口文档：启动后访问 <http://localhost:8080/doc.html>（Knife4j）。

## 演示账号

全部为模拟数据，密码统一为 `123456`：

| 账号 | 角色 |
| --- | --- |
| `admin` | 管理员 |
| `tech01` ~ `tech03` | 网络技术员 |
| `stu01` ~ `stu07` | 报障人 |

## 数据说明

- 全部为模拟数据，不含真实个人信息、真实密码或密钥
- 默认密码与 JWT 密钥仅用于本地开发，部署时通过环境变量覆盖

## 版本管理

- 按功能块提交，提交信息使用 `feat:` / `fix:` / `docs:` 前缀
- 答辩冻结版本打 tag：`git tag -a v1.0.0 -m "答辩冻结版本"` 后 `git push origin --tags`

## 后续计划

- [ ] 阶段 4：设备/机房 CRUD、5 状态流转 + 非法流转拒绝、状态日志、乐观锁防并发、角色权限
- [ ] 阶段 5：多表 JOIN 详情、多条件分页、统计接口
- [ ] 阶段 6：Knife4j 截图、Apifox 测试集合、业务流程记录、贡献说明
- [ ] 阶段 7：答辩 PPT 与演示视频
