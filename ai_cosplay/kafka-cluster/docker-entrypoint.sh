#!/bin/bash
set -e

# 等待其他节点启动
sleep 10

# 设置默认值
KAFKA_NODE_ID=${KAFKA_NODE_ID:-1}
KAFKA_CLUSTER_ID=${KAFKA_CLUSTER_ID:-"kafka-cluster-001"}
KAFKA_LISTENERS=${KAFKA_LISTENERS:-"PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093"}
KAFKA_ADVERTISED_LISTENERS=${KAFKA_ADVERTISED_LISTENERS:-"PLAINTEXT://localhost:9092"}
KAFKA_CONTROLLER_QUORUM_VOTERS=${KAFKA_CONTROLLER_QUORUM_VOTERS:-"1@kafka1:9093,2@kafka2:9093,3@kafka3:9093,4@kafka4:9093,5@kafka5:9093"}
KAFKA_PROCESS_ROLES=${KAFKA_PROCESS_ROLES:-"broker,controller"}

echo "Starting Kafka node ${KAFKA_NODE_ID} with cluster ID ${KAFKA_CLUSTER_ID}"

# 生成配置文件
cat > ${KAFKA_HOME}/config/kraft/server.properties << EOF
# Server基础配置
node.id=${KAFKA_NODE_ID}
process.roles=${KAFKA_PROCESS_ROLES}
controller.quorum.voters=${KAFKA_CONTROLLER_QUORUM_VOTERS}

# 监听器配置
listeners=${KAFKA_LISTENERS}
advertised.listeners=${KAFKA_ADVERTISED_LISTENERS}
listener.security.protocol.map=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
controller.listener.names=CONTROLLER
inter.broker.listener.name=PLAINTEXT

# 日志配置
log.dirs=${KAFKA_HOME}/data/kafka-logs-${KAFKA_NODE_ID}
num.network.threads=8
num.io.threads=16
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600

# 日志保留配置
num.partitions=6
num.recovery.threads.per.data.dir=2
offsets.topic.replication.factor=3
transaction.state.log.replication.factor=3
transaction.state.log.min.isr=2
log.retention.hours=168
log.retention.bytes=1073741824
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000

# 集群配置
default.replication.factor=3
min.insync.replicas=2
unclean.leader.election.enable=false
auto.create.topics.enable=false

# 性能优化
compression.type=producer
message.max.bytes=10485760
replica.fetch.max.bytes=10485760
group.initial.rebalance.delay.ms=3000

# 监控配置
jmx.port=9999
EOF

# 创建数据目录
mkdir -p ${KAFKA_HOME}/data/kafka-logs-${KAFKA_NODE_ID}

# 格式化存储（仅在数据目录为空时）
if [ ! -f "${KAFKA_HOME}/data/kafka-logs-${KAFKA_NODE_ID}/meta.properties" ]; then
    echo "Formatting storage for node ${KAFKA_NODE_ID}..."
    ${KAFKA_HOME}/bin/kafka-storage.sh format \
        -t ${KAFKA_CLUSTER_ID} \
        -c ${KAFKA_HOME}/config/kraft/server.properties
fi

echo "Starting Kafka server..."
exec ${KAFKA_HOME}/bin/kafka-server-start.sh ${KAFKA_HOME}/config/kraft/server.properties 