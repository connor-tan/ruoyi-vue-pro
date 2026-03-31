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
