# 部署教程：情侣私房菜点餐小程序

本项目分为两部分：

- `frontend/`：Vue 3 + TypeScript + uni-app，可运行 H5，也可构建微信小程序。
- `backend/`：Spring Boot + PostgreSQL + Sa-Token，提供用户、菜品、订单、情侣绑定和 WebSocket 通知接口。

本教程按 **Docker + VPS** 方式部署后端和数据库，适合阿里云、腾讯云、华为云、轻量服务器等 Linux 机器。

## 1. 本地开发启动

### 1.1 准备环境

建议版本：

- Node.js >= 20
- pnpm >= 9
- JDK 18+，也可以用 JDK 21
- Maven 3.9+
- PostgreSQL 14+
- Docker / Docker Compose（部署时需要）

### 1.2 启动数据库

如果你本地有 PostgreSQL：

```bash
createdb love_order
psql -U postgres -d love_order -f backend/src/main/resources/db/init.sql
```

如果你想直接用 Docker 启动数据库：

```bash
docker compose up -d postgres
```

初始化脚本位置：

```text
backend/src/main/resources/db/init.sql
```

脚本会创建用户、菜品分类、菜品、订单表，并内置两个 demo 账号：

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `lover` | `123456` | 点餐方 |
| `chef` | `123456` | 做饭方 |

### 1.3 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

接口文档：

```text
http://localhost:8080/doc.html
```

### 1.4 启动前端

```bash
cd frontend
pnpm install
pnpm dev:h5
```

前端接口地址在：

```text
frontend/env/.env
```

默认：

```env
VITE_SERVER_BASEURL = 'http://localhost:8080'
```

## 2. VPS 上部署后端和数据库

### 2.1 安装 Docker

Ubuntu 示例：

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl enable docker
sudo systemctl start docker
```

确认安装：

```bash
docker --version
docker compose version
```

### 2.2 上传代码

可以用 Git 拉取：

```bash
git clone git@github.com:GOaroundT-T/love-order.git
cd love-order
```

如果服务器还没配置 GitHub SSH，也可以先用 HTTPS 或者直接上传压缩包。

### 2.3 修改生产配置

打开根目录的 `docker-compose.yml`，重点修改：

```yaml
POSTGRES_PASSWORD: postgres
SPRING_DATASOURCE_PASSWORD: postgres
APP_CORS_ALLOWED_ORIGINS: https://your-domain.com
```

建议上线前：

- 把数据库密码改成更强的密码。
- 把 `APP_CORS_ALLOWED_ORIGINS` 改成你的前端域名。
- 如果暂时没有域名，可以先填 `http://服务器IP:9000` 或你的 H5 访问地址。

### 2.4 国内网络镜像源说明

如果你在国内网络，直接拉 Docker Hub / Maven Central 可能会非常慢。项目已经做了两处优化：

- `docker-compose.yml` 的 PostgreSQL 镜像使用 `docker.m.daocloud.io/library/postgres:16-alpine`。
- `backend/Dockerfile` 的 Maven 构建阶段使用阿里云 Maven 镜像。

如果你的服务器访问这个镜像源仍然失败，可以把 `docker-compose.yml` 里的镜像地址替换为你自己的镜像加速地址，例如：

```yaml
image: postgres:16-alpine
```

并在 Docker daemon 里配置 registry mirrors。常见位置：

```text
/etc/docker/daemon.json
```

示例：

```json
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io"
  ]
}
```

修改后重启 Docker：

```bash
sudo systemctl daemon-reload
sudo systemctl restart docker
```

### 2.5 启动服务

```bash
docker compose up -d --build
```

查看容器：

```bash
docker compose ps
```

查看后端日志：

```bash
docker compose logs -f backend
```

如果正常，后端会监听：

```text
http://服务器IP:8080
```

### 2.6 放行端口

云服务器安全组和系统防火墙都要放行：

- `8080`：后端 API
- `5432`：PostgreSQL，仅开发调试需要；生产建议不要公网开放
- `80/443`：如果后续用 Nginx 和域名

Ubuntu UFW 示例：

```bash
sudo ufw allow 8080/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
```

## 3. 前端部署

### 3.1 H5 构建

修改 `frontend/env/.env.production`，如果没有这个文件就新建：

```env
VITE_SERVER_BASEURL = 'https://你的后端域名或http://服务器IP:8080'
VITE_APP_PROXY_ENABLE = false
VITE_AUTH_MODE = 'single'
```

构建：

```bash
cd frontend
pnpm build:h5
```

构建产物一般在：

```text
frontend/dist/build/h5
```

可以把这个目录上传到 Nginx、宝塔静态网站、对象存储或 CDN。

### 3.2 微信小程序构建

```bash
cd frontend
pnpm build:mp-weixin
```

然后用微信开发者工具打开生成的 `dist/build/mp-weixin`。

小程序上线注意：

- 后端必须是 HTTPS 域名。
- 微信公众平台后台需要配置 request 合法域名。
- WebSocket 也需要配置 socket 合法域名。

## 4. 常见问题

### 前端提示网络错误

检查：

1. `VITE_SERVER_BASEURL` 是否正确。
2. 后端容器是否启动：`docker compose ps`。
3. 服务器安全组是否放行 8080。
4. 浏览器控制台是否有 CORS 报错。

### 登录失败

默认 demo 账号：

```text
lover / 123456
chef / 123456
```

如果你改过数据库，重新导入初始化脚本或检查 `t_user` 表。

### 数据库初始化脚本没有重新执行

PostgreSQL Docker 镜像只会在数据卷第一次创建时执行 `/docker-entrypoint-initdb.d` 下的脚本。

如果是开发环境，想重置数据库：

```bash
docker compose down -v
docker compose up -d postgres
```

注意：`down -v` 会删除数据库数据。

### WebSocket 没有通知

检查：

1. 用户是否已登录。
2. 两个 demo 用户是否已互相绑定。
3. 前端 `VITE_SERVER_BASEURL` 是否能转换为正确 WebSocket 地址。
4. 如果是 HTTPS，WebSocket 应该走 `wss://`。

## 5. 推荐上线架构

简单 VPS 架构：

```text
用户浏览器/小程序
        ↓
Nginx / HTTPS 域名
        ↓
Spring Boot 后端 :8080
        ↓
PostgreSQL Docker 容器
```

生产建议：

- 用 Nginx 配 HTTPS。
- PostgreSQL 不开放公网端口。
- 数据库密码使用强密码。
- 定期备份 PostgreSQL 数据卷。
- 微信小程序正式上线时，再完整接入微信登录。
