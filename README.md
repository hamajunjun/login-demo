# 社区论坛（login-demo）

基于 Spring Boot 的社区论坛项目，实现了用户、帖子、评论回复、点赞、收藏、关注、私信、消息通知、管理后台等完整功能，并集成 Redis、RabbitMQ、WebSocket 等中间件，针对缓存穿透/击穿、消息异步化、实时推送等真实场景做了落地。

> 个人学习项目，用于系统掌握 Spring Boot 企业级常用技术栈。

## 技术栈

| 分类 | 技术 | 说明 |
|------|------|------|
| 基础框架 | Spring Boot 2.6.13 / JDK 17 | 项目骨架 |
| 持久层 | MyBatis + MySQL 8 | 注解方式写 SQL |
| 认证 | JWT（java-jwt） | 登录认证 + 拦截器鉴权 |
| 缓存 | Redis + Redisson | 热点缓存、分布式锁、布隆过滤器 |
| 消息队列 | RabbitMQ | 通知异步化、死信队列、幂等消费 |
| 实时推送 | WebSocket | 在线通知实时推送（token 认证 + 多端连接） |
| 接口文档 | Knife4j（OpenAPI3） | 在线接口文档 |
| 其他 | PageHelper / jBCrypt / Lombok / AOP | 分页 / 加密 / 简化代码 / 接口日志 |

## 功能清单

- **用户**：注册、登录、修改资料、头像上传、修改密码、注销账号、启用/禁用
- **帖子**：发布、编辑、删除、分页列表、热门榜、全文搜索、浏览量
- **互动**：评论（树形回复）、点赞、收藏、关注
- **消息**：私信、通知（点赞/评论/回复）、WebSocket 实时推送、未读角标
- **小区**：小区列表、添加、综合评分
- **管理后台**：用户管理、帖子管理、重置密码

## 项目结构

```
com.example.logindemo
├── controller     # 接口层
├── service        # 业务逻辑层
├── mapper         # 数据访问层（MyBatis）
├── entity         # 实体类
├── dto            # 数据传输对象（如 NotificationMessage）
├── config         # 配置类（RabbitMQ、Redis、WebSocket、JWT 等）
├── interceptor    # 拦截器（JWT 认证、管理员鉴权）
├── aspect         # AOP 切面（接口日志）
├── listener       # MQ 消费者
├── job            # 定时任务（布隆过滤器重建）
├── websocket      # WebSocket 处理器
├── util           # 工具类（JWT、布隆过滤器、Redis 等）
└── common         # 统一返回结果
```

## 环境要求

- JDK 17
- Maven 3.6+
- MySQL 8.0
- Redis 5+
- RabbitMQ 3.8+

## 启动步骤

1. 初始化数据库：执行 `schema.sql`
2. 修改 `src/main/resources/application.properties` 中的 MySQL / Redis / RabbitMQ 连接信息
3. 用 IDEA 打开项目，运行 `LoginDemoApplication`
4. 浏览器访问 `http://localhost:8080/login.html`

## 核心亮点

### 1. 缓存穿透 / 击穿的完整解决方案
- **穿透**：布隆过滤器（用户名 / 帖子 ID）+ 缓存空值，双重防护
- **击穿**：Redisson 分布式锁（`RLock` + 看门狗），热点 key 重建时只放一个请求进数据库
- **过滤器维护**：启动时 `CommandLineRunner` 加载全量数据，每天凌晨 `@Scheduled` 定时重建，解决删除后残留问题

### 2. 消息通知异步化
- 点赞 / 评论 / 回复统一通过 RabbitMQ 异步投递，业务代码与通知逻辑解耦
- 事务提交成功后再发消息（`TransactionSynchronization.afterCommit`），避免事务回滚仍发通知
- 死信队列兜底处理失败消息
- 幂等消费：Redis `setIfAbsent` + messageId，防止消息重复消费

### 3. WebSocket 实时推送
- 后端主动推送在线用户，收到点赞 / 评论立即提醒
- **身份认证**：连接时携带 JWT，后端解析 token 得到真实用户，防止伪造 userId
- **多端连接**：同一用户多页面在线，所有连接都能收到推送
- 数据库留档 + WebSocket 实时，双通道保证

### 4. SQL 优化
- 针对高频列表查询设计复合索引，用 EXPLAIN 验证从 `type=ALL` 优化到 `type=ref`
- 帖子标题/内容、用户名用 MySQL FULLTEXT 全文索引，解决模糊搜索全表扫描

## 接口文档

启动后访问 Knife4j：`http://localhost:8080/doc.html`

## 已知待优化

- `PostFavoriteServiceImpl.listMyFavorites` 存在 N+1 查询，可优化为 JOIN
- 个别静态资源（如 favicon）有 401 提示，不影响功能
- 部分日志 / 注释为中文，部署到非 UTF-8 环境需注意编码
