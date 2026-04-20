# Docker Build & Up

目标：用一套 `docker-compose.yml` 启动校刊汇开发环境，包括 MySQL 主从、Redis、`yudao-server` 和管理后台。

## 启动

```shell
cd /Users/connor/workspace/king/ruoyi-vue-pro/script/docker
cp .env.example .env
docker compose --env-file .env up -d --build
```

首次启动会构建后端和前端镜像，并初始化 MySQL master 数据库。已有数据卷时，MySQL 官方镜像不会重复执行初始化 SQL。

## 环境切换

后端 jar 只构建一份，`yudao-server/src/main/resources` 下的 `application.yaml`、`application-local.yaml`、`application-dev.yaml`、`application-docker.yaml` 都会被打进包内；运行时通过 `.env` 的 `SPRING_PROFILES_ACTIVE` 决定加载哪个 Spring Profile。

当前 compose 默认使用：

```dotenv
SPRING_PROFILES_ACTIVE=docker
```

`docker` profile 是容器部署专用配置，数据库和 Redis 默认走 compose 服务名：

```text
mysql-master:3306
mysql-slave:3306
redis:6379
```

如果要复用已有 `local` 或 `dev` 配置，可以修改 `.env`：

```dotenv
SPRING_PROFILES_ACTIVE=local
# 或
SPRING_PROFILES_ACTIVE=dev
```

如果你希望加载 `dev` 的其它配置，但数据库、Redis 仍使用 compose 内置服务，可以把 `docker` 放在最后：

```dotenv
SPRING_PROFILES_ACTIVE=dev,docker
```

`docker` profile 会从 `.env` 读取常用运行参数，例如：

```dotenv
MYSQL_DATABASE=ruoyi-vue-pro
MYSQL_ROOT_PASSWORD=123456
REDIS_DATABASE=5
REDIS_PASSWORD=
QUARTZ_AUTO_STARTUP=true
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info
```

当前 POM 没有定义 Maven 多环境 profile，因此不需要通过 `mvn -Pdev/-Pprod` 生成不同 jar。Dockerfile 预留了 `MAVEN_BUILD_ARGS`，以后如果 POM 增加构建 profile，可在 `.env` 中补充，例如 `MAVEN_BUILD_ARGS=-Pprod`。

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

## 代码更新后的部署

后端代码更新后，重新构建并替换 `server` 容器：

```shell
cd /Users/connor/workspace/king/ruoyi-vue-pro/script/docker
docker compose --env-file .env up -d --build server
```

前端管理后台代码更新后，重新构建并替换 `admin` 容器：

```shell
cd /Users/connor/workspace/king/ruoyi-vue-pro/script/docker
docker compose --env-file .env up -d --build admin
```

如果同时改了后端、前端、Dockerfile、Nginx 或 compose 配置，直接重建相关服务：

```shell
docker compose --env-file .env up -d --build server admin
```

如果只修改 `.env`，通常不需要重新 build，只需要重建容器让环境变量生效：

```shell
docker compose --env-file .env up -d --force-recreate server admin
```

如果只想重启，不涉及镜像或环境变量变化：

```shell
docker compose --env-file .env restart server admin
```

注意：`mysql-replication/master/init/ruoyi-vue-pro.sql` 只会在 MySQL master 数据卷首次创建时执行。已有数据卷时，更新 init SQL 不会自动改库；需要用迁移 SQL 直接落库，或在开发环境明确执行 `docker compose --env-file .env down -v` 后重建数据卷。

## MySQL 主从重新初始化

开发环境默认 `REPLICA_RESEED_ON_INIT=true`。`mysql-replica-init` 启动后会先检查当前主从是否已经健康；如果 `mysql-slave` 已经正确连接 `mysql-master`，会直接跳过，不会重复 dump 或覆盖从库数据。

只有在从库未配置、复制不健康或你强制重建 `mysql-replica-init` 后检测不通过时，脚本才会清理从库复制状态和 GTID，再从 master dump 当前数据库到 slave，最后重新建立 GTID 复制。

这样可以避免复用旧从库数据卷时出现：

```text
Cannot replicate because the source purged required binary logs
```

如果你只想重置复制配置、不想覆盖从库数据库，可以在 `.env` 中设置：

```dotenv
REPLICA_RESEED_ON_INIT=false
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
