# 校园网络与机房故障报修平台（T05）

基于 Spring Boot 3 的校园网络与机房故障报修管理系统：报障人提交报修单，网络技术员受理、排查、修复，报障人确认关闭，全流程状态可追踪，含角色权限、JWT 认证、状态日志与统计报表。

- 课程：JavaWeb 后端开发技术（期末综合项目，与 Docker、MVVM 课程共用选题库）
- 选题编号：T05 校园网络与机房故障报修平台
- 本仓库仅包含**后端**部分（前端见其他课程交付）

---

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 / 框架 | Java 21 · Spring Boot 3.x · Maven |
| 数据层 | MyBatis-Plus · MySQL 8 |
| 认证鉴权 | JWT（jjwt）· BCrypt（spring-security-crypto） |
| 接口文档 | Knife4j |
| 部署 | 环境变量注入配置 · Docker（Docker 课程要求，见 backend/Dockerfile） |

## 功能概览

- **三种角色**：报障人（学生/教职工）、网络技术员、系统管理员，接口按角色鉴权
- **主流程 5 状态**：待受理 → 排查中 → 修复中 → 待确认 → 已关闭
- **异常分支**：无法复现 → 退回补充信息 → 重受理；验收不通过 → 返工
- **工程规范**：Controller / Service / Mapper 三层，Entity / DTO / VO，统一 Result，全局异常，错误码规范
- **安全**：密码 BCrypt 存储；JWT 拦截器校验，白名单明确；防水平越权；非法状态流转拒绝；乐观锁防并发重复提交
- **统计**：平均修复时长、未修复单数、按机房分组统计
- **接口**：RESTful 风格，25+ 个接口

## 目录结构

```
campus-network-repair/
├── backend/                   # 后端源码（pom.xml、Dockerfile）
├── database/                  # schema.sql、seed.sql
├── docs/
│   ├── requirements.md        # 需求说明：角色、功能、状态流转图
│   ├── database-design.md    # ER 图、表结构、索引说明
│   ├── api-doc/              # 接口文档导出或截图
│   ├── api-test/             # Apifox/Postman 测试集合与结果
│   ├── business-flow-record.md   # 主流程、异常流程、统计的测试数据与结果
│   ├── contribution-table.md # 分工与提交记录
│   └── ai-usage-statement.md # AI 工具使用说明
└── README.md
```

## 环境要求

- JDK 21
- Maven 3.9+
- MySQL 8.0+
- Apifox 或 Postman（接口测试）

## 快速开始

### 1. 初始化数据库

```bash
# 在空库执行（保证一次成功）
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

### 2. 配置环境变量

数据库与密钥信息全部通过环境变量注入，**不硬编码、不提交到仓库**。见下方「环境变量与多环境配置」。

### 3. 构建并启动

```bash
cd backend
mvn clean package
java -jar target/*.jar
```

### 4. 验证

```bash
# 健康检查接口（供容器探针使用）
curl http://localhost:8080/api/health
```

返回 `{"code":200,"message":"ok"}` 即启动成功。

## 环境变量与多环境配置

### 环境变量列表

| 变量名 | 说明 | 示例 |
| --- | --- | --- |
| `DB_URL` | 数据库连接地址 | `jdbc:mysql://localhost:3306/campus_repair?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false` |
| `DB_USERNAME` | 数据库账号 | `root` |
| `DB_PASSWORD` | 数据库密码 | 自定义 |
| `JWT_SECRET` | JWT 签名密钥（≥32 字符随机串） | 自定义 |
| `SPRING_PROFILES_ACTIVE` | 激活的环境（默认 dev） | `dev` / `prod` |

### 配置注入方式（application.yml 不写死密钥）

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/campus_repair?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
jwt:
  secret: ${JWT_SECRET:please-change-me-to-a-long-random-secret}
```

> 冒号后为默认值，仅本地开发兜底；正式运行必须通过环境变量覆盖。本地私密配置可写在 `application-local.yml`，该文件已加入 `.gitignore`，禁止提交。

### 多环境配置说明

- `application.yml`：通用配置（端口、MyBatis-Plus、日志级别等）
- `application-dev.yml`：开发环境配置
- `application-prod.yml`：生产环境配置
- 通过 `SPRING_PROFILES_ACTIVE` 切换；敏感项（数据库密码、JWT 密钥）一律走环境变量，不写入任何配置文件。

## 接口文档与测试

- 启动后访问 Knife4j 在线文档：`http://localhost:8080/doc.html`
- Apifox/Postman 测试集合：见 `docs/api-test/`（覆盖主流程、异常流程、越权场景）
- 测试数据与结果：见 `docs/business-flow-record.md`

## 提交与版本管理

- 项目以 Git 管理，提交历史即开发过程记录（见 `git log`）
- 答辩冻结版本以 tag 标记：`git tag -a v1.0.0 -m "答辩冻结版本"`
- 分工说明：见 `docs/contribution-table.md`（本项目由单人独立完成全部模块）
- AI 工具使用说明：见 `docs/ai-usage-statement.md`

## 数据说明

- 全部使用**模拟数据**，不含任何真实个人信息、真实密码或密钥
- 演示账号见 `database/seed.sql`

## 待补充

- [ ] 数据库账号/密码/JWT 密钥按实际环境配置（不要提交到仓库）
- [ ] 实际端口号与默认库名如与本地不一致，同步修改 `application.yml`
- [ ] 补充实际运行截图（Knife4j、Apifox 测试通过、mvn package 日志）到 `docs/`
