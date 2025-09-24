# 企业级 Kafka 3.6.1 集群 Dockerfile
#FROM openjdk:17-jdk-slim
FROM java17:latest

# 设置环境变量
ENV KAFKA_VERSION=3.6.1
ENV SCALA_VERSION=2.13
ENV KAFKA_HOME=/opt/kafka
ENV PATH=$PATH:$KAFKA_HOME/bin

# 安装必要的工具
RUN apt-get update && \
    apt-get install -y wget curl procps net-tools && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 创建kafka用户
RUN groupadd -r kafka && \
    useradd -r -g kafka kafka

# 下载并安装Kafka（如果本地有包文件则使用本地，否则下载）
COPY kafka_2.13-3.6.1.tgz* /tmp/
RUN if [ -f /tmp/kafka_2.13-3.6.1.tgz ]; then \
        echo "使用本地Kafka包"; \
        cp /tmp/kafka_2.13-3.6.1.tgz /tmp/kafka.tgz; \
    else \
        echo "下载Kafka包"; \
        wget -O /tmp/kafka.tgz "https://archive.apache.org/dist/kafka/3.6.1/kafka_2.13-3.6.1.tgz"; \
    fi && \
    tar -xzf /tmp/kafka.tgz -C /opt && \
    mv /opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} ${KAFKA_HOME} && \
    rm /tmp/kafka*

# 创建必要的目录
RUN mkdir -p ${KAFKA_HOME}/logs && \
    mkdir -p ${KAFKA_HOME}/data && \
    mkdir -p ${KAFKA_HOME}/config/kraft && \
    chown -R kafka:kafka ${KAFKA_HOME}

# 复制配置文件
COPY log4j.properties ${KAFKA_HOME}/config/
COPY docker-entrypoint.sh ${KAFKA_HOME}/bin/
RUN chmod +x ${KAFKA_HOME}/bin/docker-entrypoint.sh

# 切换到kafka用户
USER kafka

# 暴露端口
EXPOSE 9092 9093 9094

# 设置工作目录
WORKDIR ${KAFKA_HOME}

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD ${KAFKA_HOME}/bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092 || exit 1

# 启动脚本
ENTRYPOINT ["/opt/kafka/bin/docker-entrypoint.sh"] 