#!/usr/bin/env bash

# ==============================================================================
# love-order 一键服务器部署脚本
# ==============================================================================
#
# 适用场景：
#   你买了一台阿里云 / 腾讯云 / 华为云 / 轻量服务器，想一条命令部署：
#   1. PostgreSQL 数据库
#   2. Spring Boot 后端服务
#
# 使用方式：
#   bash scripts/deploy-server.sh
#
# 常用参数：
#   --yes       跳过确认提示，适合你已经检查过配置时使用
#   --no-build  不重新构建后端镜像，只启动/重启已有容器
#   --logs      启动后继续跟随 backend 日志
#   --help      查看帮助
#
# 注意：
#   - 这个脚本不部署前端 H5/小程序，只部署后端 + 数据库。
#   - 这个脚本不会执行 docker compose down -v，避免误删数据库数据。
#   - 第一次构建后端可能较慢，因为 Docker/Maven 要下载依赖。
#
# ==============================================================================

# set -euo pipefail 是 Shell 脚本里常见的安全写法：
# -e：任意命令失败时立即停止脚本，避免错误继续扩大。
# -u：使用未定义变量时报错，避免变量写错还继续执行。
# -o pipefail：管道中任何一个命令失败，都让整个管道失败。
set -euo pipefail

YES=false
NO_BUILD=false
FOLLOW_LOGS=false

print_help() {
  cat <<'EOF'
love-order 一键服务器部署脚本

用法：
  bash scripts/deploy-server.sh [选项]

选项：
  --yes       跳过确认提示，发现默认密码/占位域名时也继续部署
  --no-build  不重新构建后端镜像，只执行 docker compose up -d
  --logs      启动完成后继续跟随 backend 日志
  --help      显示这段帮助信息

这个脚本会做什么：
  1. 检查 Docker 是否安装
  2. 检查 Docker daemon 是否启动
  3. 检查 docker compose 是否可用
  4. 检查 docker-compose.yml 和 backend/Dockerfile 是否存在
  5. 提醒你检查数据库密码、CORS 域名、PostgreSQL 端口暴露等生产风险
  6. 执行 docker compose up -d --build 启动 PostgreSQL + 后端
  7. 显示容器状态和后端日志
  8. 尝试访问 /dish/categories 做接口 smoke check

示例：
  bash scripts/deploy-server.sh
  bash scripts/deploy-server.sh --yes
  bash scripts/deploy-server.sh --yes --no-build
  bash scripts/deploy-server.sh --logs
EOF
}

print_step() {
  printf '\n\033[1;34m==> %s\033[0m\n' "$1"
}

print_warn() {
  printf '\033[1;33m警告：%s\033[0m\n' "$1"
}

print_ok() {
  printf '\033[1;32m完成：%s\033[0m\n' "$1"
}

die() {
  printf '\033[1;31m错误：%s\033[0m\n' "$1" >&2
  exit 1
}

for arg in "$@"; do
  case "$arg" in
    --yes)
      YES=true
      ;;
    --no-build)
      NO_BUILD=true
      ;;
    --logs)
      FOLLOW_LOGS=true
      ;;
    --help|-h)
      print_help
      exit 0
      ;;
    *)
      die "未知参数：$arg。使用 --help 查看用法。"
      ;;
  esac
done

# BASH_SOURCE[0] 是当前脚本文件路径。
# 下面这几行用于计算项目根目录，保证你从任何目录执行脚本都能正确找到 docker-compose.yml。
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$REPO_ROOT/docker-compose.yml"
BACKEND_DOCKERFILE="$REPO_ROOT/backend/Dockerfile"

cd "$REPO_ROOT"

print_step "检查部署文件"
[[ -f "$COMPOSE_FILE" ]] || die "找不到 docker-compose.yml，请确认你在 love-order 项目里。"
[[ -f "$BACKEND_DOCKERFILE" ]] || die "找不到 backend/Dockerfile，后端镜像无法构建。"
print_ok "已找到 docker-compose.yml 和 backend/Dockerfile"

print_step "检查 Docker 命令是否安装"
# command -v docker：检查系统 PATH 中能不能找到 docker 命令。
# 如果没有 Docker，需要先在服务器上安装 Docker。
command -v docker >/dev/null 2>&1 || die "未安装 Docker。Ubuntu 可执行：sudo apt install -y docker.io docker-compose-plugin"
print_ok "Docker 命令存在：$(docker --version)"

print_step "检查 Docker daemon 是否启动"
# docker info 会连接 Docker 后台服务（daemon）。
# Docker 命令存在不代表 daemon 已启动；Mac 要打开 Docker Desktop，Linux 要启动 docker 服务。
if ! docker info >/dev/null 2>&1; then
  cat <<'EOF'
Docker daemon 没有启动或当前用户没有权限访问 Docker。

如果你在 Mac/Windows：
  请打开 Docker Desktop，等它显示 Running 后再执行脚本。

如果你在 Ubuntu 服务器：
  sudo systemctl start docker
  sudo systemctl enable docker

如果提示权限不足：
  sudo usermod -aG docker $USER
  然后退出 SSH 重新登录。
EOF
  exit 1
fi
print_ok "Docker daemon 正在运行"

print_step "检查 Docker Compose 是否可用"
# docker compose version：检查 Docker Compose v2 插件是否可用。
# 本项目用 compose 同时管理 postgres 和 backend 两个容器。
docker compose version >/dev/null 2>&1 || die "docker compose 不可用。Ubuntu 可执行：sudo apt install -y docker-compose-plugin"
print_ok "Docker Compose 可用：$(docker compose version)"

print_step "检查生产配置风险"
WARNINGS=0

# grep -q：安静模式搜索文本。找到就返回 0，找不到返回非 0。
# 这里不是为了阻止部署，而是提醒你上线前最好改掉默认值。
if grep -q 'POSTGRES_PASSWORD: postgres' "$COMPOSE_FILE"; then
  print_warn "PostgreSQL 密码仍是默认值 postgres。真实上线建议改成强密码。"
  WARNINGS=$((WARNINGS + 1))
fi

if grep -q 'SPRING_DATASOURCE_PASSWORD: postgres' "$COMPOSE_FILE"; then
  print_warn "后端连接数据库的密码仍是默认值 postgres。真实上线建议和数据库密码一起修改。"
  WARNINGS=$((WARNINGS + 1))
fi

if grep -q 'your-domain.com' "$COMPOSE_FILE"; then
  print_warn "CORS 里仍包含占位域名 your-domain.com。上线后应改成你的前端域名。"
  WARNINGS=$((WARNINGS + 1))
fi

if grep -q '"5432:5432"' "$COMPOSE_FILE"; then
  print_warn "PostgreSQL 5432 端口当前会映射到服务器。生产环境建议不要在安全组开放 5432。"
  WARNINGS=$((WARNINGS + 1))
fi

if [[ "$WARNINGS" -gt 0 && "$YES" != true ]]; then
  printf '\n发现 %s 个需要你关注的配置项。是否继续部署？[y/N] ' "$WARNINGS"
  read -r answer
  case "$answer" in
    y|Y|yes|YES)
      print_ok "你选择继续部署"
      ;;
    *)
      die "已取消部署。你可以先修改 docker-compose.yml，或确认后用 --yes 跳过提示。"
      ;;
  esac
else
  print_ok "配置检查完成"
fi

print_step "启动 PostgreSQL 和后端"
if [[ "$NO_BUILD" == true ]]; then
  # docker compose up -d：按 docker-compose.yml 启动服务。
  # -d 表示 detached 后台运行，关闭终端后容器仍会运行。
  echo "执行：docker compose up -d"
  docker compose up -d
else
  # docker compose up -d --build：先按 backend/Dockerfile 重新构建后端镜像，再后台启动服务。
  # 第一次执行会下载 Docker 基础镜像和 Maven 依赖，所以会比较慢。
  echo "执行：docker compose up -d --build"
  docker compose up -d --build
fi
print_ok "启动命令已执行"

print_step "查看容器状态"
# docker compose ps：查看 compose 管理的服务是否 Up，以及端口映射是否正确。
docker compose ps

print_step "查看后端最近日志"
# docker compose logs --tail=80 backend：只看 backend 最近 80 行日志。
# 如果看到 Started LoveOrderApplication，通常说明 Spring Boot 启动成功。
docker compose logs --tail=80 backend

print_step "接口 smoke check"
if command -v curl >/dev/null 2>&1; then
  # /dish/categories 是公开接口，不需要登录，适合用来确认后端和数据库都能工作。
  if curl -fsS http://127.0.0.1:8080/dish/categories >/tmp/love-order-smoke.json; then
    print_ok "接口可访问：http://127.0.0.1:8080/dish/categories"
  else
    print_warn "接口 smoke check 未通过。请查看上面的 backend 日志，或执行：docker compose logs -f backend"
  fi
else
  print_warn "服务器没有 curl，跳过接口自动检查。你可以手动打开 /dish/categories 测试。"
fi

cat <<'EOF'

==============================================================================
部署脚本执行完毕
==============================================================================

你接下来可以访问：

  接口文档： http://服务器IP:8080/doc.html
  菜品接口： http://服务器IP:8080/dish/categories

常用运维命令：

  查看容器状态：
    docker compose ps

  查看后端日志：
    docker compose logs -f backend

  重启服务：
    docker compose restart

  停止服务（不删除数据库数据）：
    docker compose down

  重新构建并启动后端：
    docker compose up -d --build backend

重要提醒：

  1. 真实上线前请把 docker-compose.yml 里的数据库密码改成强密码。
  2. 生产环境不建议在云服务器安全组开放 5432 端口。
  3. 微信小程序正式上线需要 HTTPS 域名，而不是 http://服务器IP:8080。

EOF

if [[ "$FOLLOW_LOGS" == true ]]; then
  print_step "持续跟随 backend 日志，按 Ctrl+C 退出日志查看"
  docker compose logs -f backend
fi
