# Docker Build & Up

目标：用一套 `docker-compose.yml` 启动校刊汇开发环境，包括 MySQL 主从、Redis、`yudao-server` 和管理后台。

## 启动

```shell
cd /Users/connor/workspace/king/ruoyi-vue-pro/script/docker
cp .env.example .env
docker compose --env-file .env up -d --build
```

首次启动会构建后端和前端镜像，并初始化 MySQL master 数据库。已有数据卷时，MySQL 官方镜像不会重复执行初始化 SQL。

## 文件说明

```text
.
├── .env.example                    <-- compose 参数模板，复制为 .env 后使用
├── Docker-HOWTO.md
├── docker-compose.yml              <-- 一体化开发编排
├── mysql-replication               <-- MySQL 主从配置和初始化脚本
└── yudao-server
    └── Dockerfile                  <-- 后端 Maven 多阶段构建
```

管理后台 Dockerfile 和 Nginx 配置位于：

```text
/Users/connor/workspace/king/yudao-ui-admin-vue3
├── Dockerfile
├── .dockerignore
└── nginx.conf
```

## 访问地址

- 管理后台：http://localhost:8080
- 后端服务：http://localhost:48080
- MySQL master：`127.0.0.1:3307`
- MySQL slave：`127.0.0.1:3308`
- Redis：`127.0.0.1:6379`

端口、密码、Redis database、JVM 参数等都在 `.env` 中调整。

## 常用命令

```shell
# 展开并检查 compose 配置
docker compose --env-file .env config

# 单独构建后端或前端
docker compose --env-file .env build server
docker compose --env-file .env build admin

# 查看状态和日志
docker compose --env-file .env ps
docker compose --env-file .env logs -f server
docker compose --env-file .env logs -f admin

# 验证后端健康
curl http://localhost:48080/actuator/health

# 验证 MySQL 主从
docker compose --env-file .env exec mysql-slave \
  mysql -uroot -p123456 -e "SHOW REPLICA STATUS\\G"
```

## 重置开发数据

```shell
docker compose --env-file .env down -v
docker compose --env-file .env up -d --build
```

`down -v` 会删除 MySQL 和 Redis 数据卷，只适合开发环境重置。

## 说明

- `mysql-replication` 子目录仍可单独用于调试数据库主从，但完整开发部署请使用本目录的主 `docker-compose.yml`。
- `yudao-server` 镜像不再依赖手工复制 jar，构建时会从后端根工程执行 Maven package。
- 管理后台通过 Nginx 同源代理访问后端，浏览器只需要访问 `http://localhost:8080`。
