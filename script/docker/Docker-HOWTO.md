# Docker Build & Up

目标：用一套 `docker-compose.yml` 启动校刊汇后端、管理后台、App H5、Gateway、MySQL 主从和 Redis，并通过 `--env-file` 在 `dev / test / uat / prod` 环境之间直接切换。

默认部署边界：浏览器和小程序只访问 `gateway`；`yudao-server` 只在 Docker 网络内暴露 `48080`，不直接发布到宿主机或公网。管理后台和 H5 容器自带 Nginx，仍可通过本机调试端口直连。

## 启动

```shell
cd /Users/connor/workspace/xiaokanhui/ruoyi-vue-pro/script/docker
docker compose --env-file env/dev.env up -d --build
docker compose --env-file env/test.env up -d --build
docker compose --env-file env/uat.env up -d --build
docker compose --env-file env/prod.env up -d --build
```

首次启动会构建后端和前端镜像，并初始化 MySQL master 数据库。已有数据卷时，MySQL 官方镜像不会重复执行初始化 SQL。

## 环境文件

已提交的环境文件：

```text
env/dev.env
env/test.env
env/uat.env
env/prod.env
```

每个环境文件都完整定义：

- compose 项目名、镜像仓库和镜像 tag。
- gateway 域名入口端口，以及无域名 fallback 端口。
- 管理后台和 H5 本机调试端口。
- gateway 域名、公开 URL、小程序 API/H5 URL。
- MySQL、Redis、JVM、Maven 构建参数和文件存储目录。

生产环境的密码默认使用 `CHANGE_ME_*` 占位，部署前必须替换。

## 访问地址

### dev

```text
域名入口端口：http://localhost:18080
管理后台无域名入口：http://localhost:18081
H5 无域名入口：http://localhost:18082
API 无域名入口：http://localhost:18083/actuator/health
管理后台调试直连：http://localhost:8080
H5 调试直连：http://localhost:3000
```

### test

```text
域名入口端口：http://服务器IP:28080
管理后台无域名入口：http://服务器IP:28081
H5 无域名入口：http://服务器IP:28082
API 无域名入口：http://服务器IP:28083/actuator/health
```

### uat

```text
域名入口端口：http://服务器IP:38080
管理后台无域名入口：http://服务器IP:38081
H5 无域名入口：http://服务器IP:38082
API 无域名入口：http://服务器IP:38083/actuator/health
```

### prod

```text
管理后台：https://admin.xiaoyuanyuedu.cn
H5：https://h5.xiaoyuanyuedu.cn
H5 备用入口：https://www.xiaoyuanyuedu.cn
API：https://api.xiaoyuanyuedu.cn/actuator/health
```

生产 `gateway` 的域名入口默认发布宿主机 `80` 和 `443`；无域名 fallback 端口和 admin/app 调试端口绑定 `127.0.0.1`，不对公网开放。

## Gateway

`gateway` 使用 Nginx 官方镜像模板能力，模板位于：

```text
/Users/connor/workspace/xiaokanhui/ruoyi-vue-pro/script/docker/nginx/templates/edge-gateway.conf.template
/Users/connor/workspace/xiaokanhui/ruoyi-vue-pro/script/docker/nginx/templates/edge-gateway-https.conf.template
```

启动时 Nginx 自动将 `GATEWAY_TEMPLATE` 指定的模板渲染成 `/etc/nginx/conf.d/default.conf`。

支持两种访问模式：

- 有域名：`ADMIN_DOMAIN`、`H5_DOMAIN`、`API_DOMAIN` 通过 `GATEWAY_HTTP_PORT` 分流。
- 无域名：`GATEWAY_ADMIN_PORT`、`GATEWAY_H5_PORT`、`GATEWAY_API_PORT` 分别进入管理后台、H5、API。
- 生产 HTTPS：`edge-gateway-https.conf.template` 监听 `443`，`80` 保留 `/nginx-health`、`/.well-known/acme-challenge/` 和 HTTP 到 HTTPS 跳转。

本地可通过 Host 头验证域名分流：

```shell
curl -H "Host: admin.dev.localhost" http://localhost:18080/
curl -H "Host: h5.dev.localhost" http://localhost:18080/
curl -H "Host: api.dev.localhost" http://localhost:18080/actuator/health
```

只修改 gateway 模板时，不需要重新构建镜像：

```shell
docker compose --env-file env/dev.env up -d --force-recreate gateway
```

生产证书挂载由环境变量控制：

```dotenv
GATEWAY_TEMPLATE=edge-gateway-https.conf.template
GATEWAY_HTTPS_PORT=443
CERTBOT_CERT_NAME=xiaoyuanyuedu.cn
LETSENCRYPT_HOST_PATH=/etc/letsencrypt
CERTBOT_WEBROOT_HOST_PATH=/var/www/certbot
```

`gateway` 会以只读方式挂载宿主机证书目录，并每 12 小时执行一次 `nginx -s reload`，用于读取 Certbot 续签后的新证书。

## Let's Encrypt

当前生产证书名为 `xiaoyuanyuedu.cn`，覆盖：

```text
www.xiaoyuanyuedu.cn
admin.xiaoyuanyuedu.cn
h5.xiaoyuanyuedu.cn
api.xiaoyuanyuedu.cn
```

证书路径：

```text
/etc/letsencrypt/live/xiaoyuanyuedu.cn/fullchain.pem
/etc/letsencrypt/live/xiaoyuanyuedu.cn/privkey.pem
```

由于生产 `gateway` 会占用宿主机 `80`，Certbot 续签应使用 webroot 模式，而不是 standalone 模式：

```shell
sudo mkdir -p /var/www/certbot
sudo certbot certonly --webroot \
  -w /var/www/certbot \
  --cert-name xiaoyuanyuedu.cn \
  --expand \
  -d www.xiaoyuanyuedu.cn \
  -d admin.xiaoyuanyuedu.cn \
  -d h5.xiaoyuanyuedu.cn \
  -d api.xiaoyuanyuedu.cn

sudo certbot renew --dry-run --cert-name xiaoyuanyuedu.cn --no-random-sleep-on-renew
```

如果后续需要把根域名 `xiaoyuanyuedu.cn` 加入证书，必须先确认 `@` 的 A 记录解析到生产服务器，再在上面的命令中追加 `-d xiaoyuanyuedu.cn`。

## 构建参数

管理后台 Docker 构建固定使用同源 API：

```dotenv
VITE_BASE_URL=
VITE_API_URL=/admin-api
```

App H5 Docker 构建固定使用同源 API：

```dotenv
SHOPRO_BASE_URL=
SHOPRO_API_PATH=/app-api
SHOPRO_WEBSOCKET_PATH=/infra/ws
SHOPRO_H5_URL=${H5_PUBLIC_URL}
```

微信小程序不使用 Docker Nginx 同源代理，也不作为容器服务运行。小程序构建时按环境使用完整 API/H5 地址：

```dotenv
SHOPRO_BASE_URL=${MP_BASE_URL}
SHOPRO_API_PATH=/app-api
SHOPRO_WEBSOCKET_PATH=/infra/ws
SHOPRO_H5_URL=${MP_H5_URL}
```

## 常用命令

```shell
# 展开并检查配置
docker compose --env-file env/dev.env config
docker compose --env-file env/test.env config
docker compose --env-file env/uat.env config
docker compose --env-file env/prod.env config

# 构建指定服务
docker compose --env-file env/dev.env build server
docker compose --env-file env/dev.env build admin
docker compose --env-file env/dev.env build app

# 查看状态和日志
docker compose --env-file env/dev.env ps
docker compose --env-file env/dev.env logs -f server
docker compose --env-file env/dev.env logs -f admin
docker compose --env-file env/dev.env logs -f app
docker compose --env-file env/dev.env logs -f gateway

# 验证 gateway
curl http://localhost:18080/nginx-health
curl http://localhost:18083/actuator/health

# 验证生产 HTTPS gateway
curl -I https://admin.xiaoyuanyuedu.cn/
curl -I https://h5.xiaoyuanyuedu.cn/
curl -I https://api.xiaoyuanyuedu.cn/actuator/health

# 验证后端健康。后端不发布宿主机端口，健康检查在容器内执行。
docker compose --env-file env/dev.env exec server \
  curl -fsS http://127.0.0.1:48080/actuator/health

# 验证 MySQL 主从
docker compose --env-file env/dev.env exec mysql-slave \
  mysql -uroot -p123456 -e "SHOW REPLICA STATUS\\G"
```

## 代码更新后的部署

```shell
cd /Users/connor/workspace/xiaokanhui/ruoyi-vue-pro/script/docker

# 后端代码更新
docker compose --env-file env/dev.env up -d --build server

# 管理后台代码更新
docker compose --env-file env/dev.env up -d --build admin

# App H5 代码更新
docker compose --env-file env/dev.env up -d --build app

# Dockerfile、Nginx 或 compose 配置更新
docker compose --env-file env/dev.env up -d --build server admin app gateway
```

如果只修改环境文件，通常不需要重新 build，只需重建容器让运行时环境变量生效。但管理后台和 H5 的构建期变量需要重新 build：

```shell
docker compose --env-file env/dev.env up -d --force-recreate server gateway
docker compose --env-file env/dev.env up -d --build admin app
```

## 缓存与数据

后端 Dockerfile 使用 BuildKit cache mount 缓存 Maven 本地仓库：

```text
/root/.m2/repository
```

管理后台 Dockerfile 使用 BuildKit cache mount 缓存 pnpm store：

```text
/pnpm/store
```

App H5 Dockerfile 使用 BuildKit cache mount 缓存 npm cache：

```text
/root/.npm
```

`mysql-replication/master/init/ruoyi-vue-pro.sql` 只会在 MySQL master 数据卷首次创建时执行。已有数据卷时，更新 init SQL 不会自动改库；需要用迁移 SQL 直接落库，或在开发环境明确执行 `down -v` 后重建数据卷。

重置开发数据：

```shell
docker compose --env-file env/dev.env down -v
docker compose --env-file env/dev.env up -d --build
```

`down -v` 会删除 MySQL 和 Redis 数据卷，只适合开发或可重建环境。

## MySQL 主从重新初始化

开发环境默认 `REPLICA_RESEED_ON_INIT=true`。`mysql-replica-init` 启动后会先检查当前主从是否已经健康；如果 `mysql-slave` 已经正确连接 `mysql-master`，会直接跳过，不会重复 dump 或覆盖从库数据。

只有在从库未配置、复制不健康或强制重建 `mysql-replica-init` 后检测不通过时，脚本才会清理从库复制状态和 GTID，再从 master dump 当前数据库到 slave，最后重新建立 GTID 复制。

如果只想重置复制配置、不想覆盖从库数据库，可以在对应环境文件中设置：

```dotenv
REPLICA_RESEED_ON_INIT=false
```

## 说明

- `mysql-replication` 子目录仍可单独用于调试数据库主从，但完整部署请使用本目录主 `docker-compose.yml`。
- `yudao-server` 镜像不再依赖手工复制 jar，构建时会从后端根工程执行 Maven package。
- `server` 只在 Docker 网络内暴露 `48080`，默认不提供 `SERVER_PORT`。
- 无域名环境使用 gateway fallback 端口；有域名环境使用 Host 头分流。
- 生产 HTTPS 可由外层负载均衡或后续 Nginx TLS 模板接入。
