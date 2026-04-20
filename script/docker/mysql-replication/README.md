# MySQL 主从复制（开发环境）

这套编排只负责启动本地 `MySQL master + slave`，并在首次启动时把仓库里的
`/sql/mysql/ruoyi-vue-pro.sql` 导入主库，再通过 GTID 自动配置从库复制。

## 启动

```bash
cd /Users/connor/workspace/king/ruoyi-vue-pro/script/docker/mysql-replication
docker compose up -d
```

## 默认连接信息

- master: `127.0.0.1:3307`
- slave: `127.0.0.1:3308`
- database: `ruoyi-vue-pro`
- username: `root`
- password: `123456`

复制账号默认是：

- username: `repl`
- password: `repl123456`

## 验证复制

```bash
docker compose exec mysql-slave mysql -uroot -p123456 -e "SHOW REPLICA STATUS\\G"
```

看到下面两项为 `Yes` 即表示复制已建立：

- `Replica_IO_Running`
- `Replica_SQL_Running`

## 从库重新初始化

默认 `REPLICA_RESEED_ON_INIT=true`。初始化容器会先检查当前主从是否已经健康；如果 `mysql-slave` 已经正确连接 `mysql-master`，会直接跳过，不会重复 dump 或覆盖从库数据。

只有在从库未配置、复制不健康或你强制重建 `mysql-replica-init` 后检测不通过时，脚本才会清理从库复制状态和 GTID，再从 master dump 当前数据库到 slave，最后重新建立 GTID 复制。

这用于避免复用旧从库数据卷时出现 `Cannot replicate because the source purged required binary logs`。如果只想重建复制关系、不想覆盖从库数据，可设置：

```bash
REPLICA_RESEED_ON_INIT=false docker compose up mysql-replica-init
```

## 重新初始化

开发环境如果需要重置主从：

```bash
docker compose down -v
docker compose up -d
```

## 应用层连接示例

- master:
  `jdbc:mysql://127.0.0.1:3307/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true`
- slave:
  `jdbc:mysql://127.0.0.1:3308/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true`
