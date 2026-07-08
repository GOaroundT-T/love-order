# love-order 情侣私房菜点餐小程序

一个给情侣使用的温馨点餐小程序：一方点想吃的菜，另一方在“专属厨房”里接单、做饭、更新状态。

## 项目结构

```text
frontend/  Vue 3 + TypeScript + uni-app 前端，可运行 H5 / 微信小程序
backend/   Spring Boot + PostgreSQL 后端 API
docs/      部署和使用文档
```

## 当前 MVP 能力

- 账号登录：`lover / 123456`、`chef / 123456`
- 浏览菜品分类和菜品
- 加入购物车并填写爱心备注
- 创建订单、查看订单、更新订单状态
- 用户资料编辑
- 情侣绑定
- WebSocket 订单通知薄切片
- Docker Compose 启动 PostgreSQL + 后端

## 本地快速启动

### 后端 + 数据库

```bash
docker compose up -d postgres
cd backend
mvn spring-boot:run
```

后端地址：

```text
http://localhost:8080
```

接口文档：

```text
http://localhost:8080/doc.html
```

### 前端

```bash
cd frontend
pnpm install
pnpm dev:h5
```

前端接口地址配置在：

```text
frontend/env/.env
```

默认指向：

```text
http://localhost:8080
```

## 部署教程

详细 Docker/VPS 部署步骤见：

[docs/deployment.md](docs/deployment.md)

## Demo 账号

| 账号 | 密码 | 说明 |
| --- | --- | --- |
| `lover` | `123456` | 点餐方 |
| `chef` | `123456` | 做饭方 |

两个账号在初始化脚本中默认已互相绑定，方便直接测试情侣点单流程。
