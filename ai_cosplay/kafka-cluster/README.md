# Kafka 3.6.1 企业级集群部署方案

这是一个基于Docker的Kafka 3.6.1企业级5节点集群解决方案，使用KRaft模式（无需ZooKeeper），提供高可用性、高性能和易于管理的特性。

## 🏗️ 架构概览

### 集群拓扑
```
┌─────────────────────────────────────────────────────────────────┐
│                    Kafka 3.6.1 企业级集群                        │
├─────────────────────────────────────────────────────────────────┤
│  Node 1        Node 2        Node 3        Node 4        Node 5  │
│  kafka1:19092  kafka2:29092  kafka3:39092  kafka4:49092  kafka5:59092│
│  Controller+   Controller+   Controller+   Controller+   Controller+│
│  Broker        Broker        Broker        Broker        Broker   │
├─────────────────────────────────────────────────────────────────┤
│  Kafka UI (8080)  │  Kafka Exporter (9308)  │  JMX (x9999)     │
└─────────────────────────────────────────────────────────────────┘
```

### 技术特性
- **KRaft模式**: 无需ZooKeeper，简化架构，提升性能
- **5节点集群**: 支持2个节点故障，确保高可用性
- **企业级配置**: 优化的生产环境参数设置
- **监控集成**: Kafka UI + Prometheus Exporter
- **Docker化**: 容器化部署，易于扩展和维护

## 📋 系统要求

### 硬件要求
- **CPU**: 每节点至少2核心（推荐4核心）
- **内存**: 每节点至少4GB（推荐8GB）
- **存储**: 每节点至少50GB SSD
- **网络**: 千兆以太网

### 软件要求
- Docker 20.10+
- Docker Compose 2.0+
- 操作系统: Linux/macOS/Windows

## 🚀 快速开始

### 1. 克隆或下载配置文件
```bash
# 创建项目目录
mkdir kafka-cluster && cd kafka-cluster

# 下载配置文件（假设文件已在当前目录）
ls -la  # 确认文件存在
```

### 2. 构建和启动集群
```bash
# 给脚本执行权限
chmod +x kafka-cluster.sh

# 构建Docker镜像
./kafka-cluster.sh build

# 启动集群
./kafka-cluster.sh start
```

### 3. 验证集群状态
```bash
# 检查集群健康状态
./kafka-cluster.sh health

# 查看集群详细状态
./kafka-cluster.sh status
```

## 🔧 管理操作

### 集群管理
```bash
# 启动集群
./kafka-cluster.sh start

# 停止集群
./kafka-cluster.sh stop

# 重启集群
./kafka-cluster.sh restart

# 查看状态
./kafka-cluster.sh status

# 检查健康
./kafka-cluster.sh health
```

### Topic管理
```bash
# 创建topic（默认6分区，3副本）
./kafka-cluster.sh create-topic my-topic

# 创建指定分区和副本的topic
./kafka-cluster.sh create-topic my-topic 12 3

# 列出所有topics
./kafka-cluster.sh list-topics

# 查看topic详情
./kafka-cluster.sh describe-topic my-topic

# 删除topic
./kafka-cluster.sh delete-topic my-topic
```

### 测试和调试
```bash
# 启动生产者测试
./kafka-cluster.sh test-producer my-topic

# 启动消费者测试
./kafka-cluster.sh test-consumer my-topic

# 查看服务日志
./kafka-cluster.sh logs kafka1

# 查看所有服务状态
docker-compose ps
```

## 🌐 访问端点

### Kafka集群端点
- **kafka1**: `localhost:19092`
- **kafka2**: `localhost:29092`
- **kafka3**: `localhost:39092`
- **kafka4**: `localhost:49092`
- **kafka5**: `localhost:59092`

### 管理界面
- **Kafka UI**: http://localhost:8080
- **Kafka Exporter**: http://localhost:9308/metrics

### JMX监控端口
- **kafka1**: `localhost:19999`
- **kafka2**: `localhost:29999`
- **kafka3**: `localhost:39999`
- **kafka4**: `localhost:49999`
- **kafka5**: `localhost:59999`

## 📊 Java应用配置

### Spring Boot配置示例
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:19092,localhost:29092,localhost:39092,localhost:49092,localhost:59092
    producer:
      acks: all
      retries: 3
      batch-size: 16384
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: my-app-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
    admin:
      properties:
        request.timeout.ms: 30000
```

### Java原生客户端配置
```properties
bootstrap.servers=localhost:19092,localhost:29092,localhost:39092,localhost:49092,localhost:59092
acks=all
retries=3
batch.size=16384
linger.ms=5
buffer.memory=33554432
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

## 🔒 生产环境配置

### 安全配置
```yaml
# 启用SSL/SASL（在docker-compose.yml中配置）
environment:
  KAFKA_LISTENERS: "SASL_SSL://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093"
  KAFKA_SECURITY_PROTOCOL: SASL_SSL
  KAFKA_SASL_MECHANISM: PLAIN
  KAFKA_SSL_KEYSTORE_LOCATION: /path/to/keystore.jks
  KAFKA_SSL_TRUSTSTORE_LOCATION: /path/to/truststore.jks
```

### 性能优化
```yaml
# JVM优化参数
KAFKA_HEAP_OPTS: "-Xmx4g -Xms4g"
KAFKA_JVM_PERFORMANCE_OPTS: >-
  -server
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=20
  -XX:InitiatingHeapOccupancyPercent=35
  -XX:+ExplicitGCInvokesConcurrent
  -Djava.awt.headless=true
```

## 📈 监控和告警

### Prometheus配置
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'kafka-exporter'
    static_configs:
      - targets: ['localhost:9308']
```

### 关键指标
- **kafka_brokers**: broker数量
- **kafka_topic_partitions**: topic分区数
- **kafka_consumer_lag**: 消费者延迟
- **kafka_log_size**: 日志大小

## 🛠️ 故障排除

### 常见问题

#### 1. 容器启动失败
```bash
# 查看容器日志
docker-compose logs kafka1

# 检查端口占用
netstat -tulpn | grep :19092
```

#### 2. 集群连接超时
```bash
# 检查网络
docker network ls
docker network inspect kafka-network

# 检查防火墙
sudo ufw status
```

#### 3. 数据持久化问题
```bash
# 检查数据卷
docker volume ls | grep kafka

# 查看卷详情
docker volume inspect kafka1-data
```

### 性能调优

#### JVM调优
```bash
# 监控GC
docker exec kafka1 jstat -gc $(docker exec kafka1 pgrep java) 1s

# 查看JVM参数
docker exec kafka1 jcmd $(docker exec kafka1 pgrep java) VM.flags
```

#### 网络调优
```bash
# 调整网络缓冲区
echo 'net.core.rmem_max = 134217728' >> /etc/sysctl.conf
echo 'net.core.wmem_max = 134217728' >> /etc/sysctl.conf
sysctl -p
```

## 🔄 备份和恢复

### 数据备份
```bash
# 备份数据卷
docker run --rm -v kafka1-data:/data -v $(pwd):/backup alpine tar czf /backup/kafka1-backup.tar.gz /data

# 备份配置
cp docker-compose.yml kafka.env backup/
```

### 数据恢复
```bash
# 恢复数据卷
docker run --rm -v kafka1-data:/data -v $(pwd):/backup alpine tar xzf /backup/kafka1-backup.tar.gz -C /
```

## 📝 最佳实践

### 1. Topic设计
- 合理设置分区数（通常为broker数的倍数）
- 副本数设为3（在5节点集群中）
- 按业务域分离topics

### 2. 生产者配置
- 使用 `acks=all` 确保数据可靠性
- 启用幂等性 `enable.idempotence=true`
- 合理设置批处理大小

### 3. 消费者配置
- 使用有意义的group.id
- 设置合理的session.timeout.ms
- 启用自动提交或手动提交

### 4. 运维建议
- 定期监控磁盘使用率
- 监控消费者延迟
- 定期备份重要数据
- 制定故障恢复计划

## 📚 参考资料

- [Apache Kafka 3.6.1 文档](https://kafka.apache.org/36/documentation.html)
- [KRaft模式指南](https://kafka.apache.org/documentation/#kraft)
- [Docker官方文档](https://docs.docker.com/)
- [Spring Kafka参考](https://spring.io/projects/spring-kafka)

## 🤝 贡献

欢迎提交问题和改进建议！

## 📄 许可证

本项目遵循 Apache 2.0 许可证。 