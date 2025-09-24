#!/bin/bash

# Kafka 3.6.1 企业级集群管理脚本
# 作者: AI Assistant
# 版本: 1.0.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置文件路径
COMPOSE_FILE="docker-compose.yml"
ENV_FILE="kafka.env"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_info "检查依赖..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装或未在PATH中"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose 未安装或未在PATH中"
        exit 1
    fi
    
    log_success "依赖检查完成"
}

# 拉取镜像
pull_images() {
    log_info "拉取Kafka镜像..."
    docker-compose pull
    log_success "镜像拉取完成"
}

# 启动集群
start_cluster() {
    log_info "启动Kafka集群..."
    
    # 创建网络
    docker network ls | grep kafka-network || docker network create kafka-network
    
    # 启动服务
    docker-compose up -d
    
    log_info "等待集群启动..."
    sleep 30
    
    # 检查健康状态
    check_cluster_health
    
    log_success "Kafka集群启动完成"
    log_info "Kafka UI: http://localhost:8080"
    log_info "Kafka Exporter: http://localhost:9308"
}

# 停止集群
stop_cluster() {
    log_info "停止Kafka集群..."
    docker-compose down
    log_success "集群已停止"
}

# 重启集群
restart_cluster() {
    log_info "重启Kafka集群..."
    stop_cluster
    sleep 5
    start_cluster
}

# 检查集群健康状态
check_cluster_health() {
    log_info "检查集群健康状态..."
    
    local healthy_nodes=0
    local total_nodes=5
    
    # 检查容器内部连接
    local containers=("kafka-cluster-node1" "kafka-cluster-node2" "kafka-cluster-node3" "kafka-cluster-node4" "kafka-cluster-node5")
    local hostnames=("kafka1" "kafka2" "kafka3" "kafka4" "kafka5")
    local ports=(19092 29092 39092 49092 59092)
    
    for i in "${!containers[@]}"; do
        local container="${containers[$i]}"
        local hostname="${hostnames[$i]}"
        local port="${ports[$i]}"
        
        if docker exec "$container" /opt/bitnami/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server "${hostname}:9092" &> /dev/null; then
            log_success "节点 $hostname (localhost:$port) 健康"
            ((healthy_nodes++))
        else
            log_warning "节点 $hostname (localhost:$port) 不健康"
        fi
    done
    
    log_info "健康节点: $healthy_nodes/$total_nodes"
    
    if [ $healthy_nodes -eq $total_nodes ]; then
        log_success "所有节点健康"
        return 0
    else
        log_warning "部分节点不健康"
        return 1
    fi
}

# 查看集群状态
cluster_status() {
    log_info "查看集群状态..."
    
    echo -e "\n${BLUE}=== Docker 容器状态 ===${NC}"
    docker-compose ps
    
    echo -e "\n${BLUE}=== 集群元数据 ===${NC}"
    docker exec kafka-cluster-node1 /opt/bitnami/kafka/bin/kafka-metadata-shell.sh --snapshot /bitnami/kafka/data/__cluster_metadata-0/00000000000000000000.log --print cluster 2>/dev/null || echo "元数据文件不存在或路径错误"
    
    echo -e "\n${BLUE}=== Topic 列表 ===${NC}"
    list_topics
    
    echo -e "\n${BLUE}=== 集群健康检查 ===${NC}"
    check_cluster_health
}

# 创建topic
create_topic() {
    local topic_name=$1
    local partitions=${2:-6}
    local replication_factor=${3:-3}
    
    if [ -z "$topic_name" ]; then
        log_error "请提供topic名称"
        echo "用法: $0 create-topic <topic_name> [partitions] [replication_factor]"
        exit 1
    fi
    
    log_info "创建topic: $topic_name (分区: $partitions, 副本: $replication_factor)"
    
    docker exec kafka-cluster-node1 /opt/bitnami/kafka/bin/kafka-topics.sh \
        --bootstrap-server localhost:9092 \
        --create \
        --topic "$topic_name" \
        --partitions "$partitions" \
        --replication-factor "$replication_factor"
    
    log_success "Topic '$topic_name' 创建成功"
}

# 删除topic
delete_topic() {
    local topic_name=$1
    
    if [ -z "$topic_name" ]; then
        log_error "请提供topic名称"
        echo "用法: $0 delete-topic <topic_name>"
        exit 1
    fi
    
    log_warning "删除topic: $topic_name"
    read -p "确认删除? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker exec kafka-cluster-node1 /opt/bitnami/kafka/bin/kafka-topics.sh \
            --bootstrap-server localhost:9092 \
            --delete \
            --topic "$topic_name"
        log_success "Topic '$topic_name' 删除成功"
    else
        log_info "操作已取消"
    fi
}

# 列出topics
list_topics() {
    docker exec kafka-cluster-node1 /opt/bitnami/kafka/bin/kafka-topics.sh \
        --bootstrap-server localhost:9092 \
        --list
}

# 描述topic
describe_topic() {
    local topic_name=$1
    
    if [ -z "$topic_name" ]; then
        log_error "请提供topic名称"
        echo "用法: $0 describe-topic <topic_name>"
        exit 1
    fi
    
    docker exec kafka-cluster-node1 /opt/bitnami/kafka/bin/kafka-topics.sh \
        --bootstrap-server localhost:9092 \
        --describe \
        --topic "$topic_name"
}

# 测试生产者
test_producer() {
    local topic_name=${1:-test-topic}
    
    log_info "启动测试生产者 (topic: $topic_name)"
    log_info "输入消息，按 Ctrl+C 退出"
    
    docker exec -it kafka-cluster-node1 /opt/bitnami/kafka/bin/kafka-console-producer.sh \
        --bootstrap-server localhost:9092 \
        --topic "$topic_name"
}

# 测试消费者
test_consumer() {
    local topic_name=${1:-test-topic}
    
    log_info "启动测试消费者 (topic: $topic_name)"
    log_info "按 Ctrl+C 退出"
    
    docker exec -it kafka-cluster-node1 /opt/bitnami/kafka/bin/kafka-console-consumer.sh \
        --bootstrap-server localhost:9092 \
        --topic "$topic_name" \
        --from-beginning
}

# 查看日志
view_logs() {
    local service_name=${1:-kafka1}
    
    log_info "查看 $service_name 日志..."
    docker-compose logs -f "$service_name"
}

# 清理数据
clean_data() {
    log_warning "这将删除所有Kafka数据和日志"
    read -p "确认清理? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_info "停止集群..."
        docker-compose down
        
        log_info "删除数据卷..."
        docker volume rm $(docker volume ls -q | grep kafka) 2>/dev/null || true
        
        log_success "数据清理完成"
    else
        log_info "操作已取消"
    fi
}

# 显示帮助
show_help() {
    echo "Kafka 3.6.1 企业级集群管理脚本"
    echo ""
    echo "用法: $0 <命令> [参数]"
    echo ""
    echo "命令:"
    echo "  pull                      拉取Docker镜像"
    echo "  start                     启动集群"
    echo "  stop                      停止集群"
    echo "  restart                   重启集群"
    echo "  status                    查看集群状态"
    echo "  health                    检查集群健康"
    echo "  create-topic <name> [p] [r]  创建topic (p=分区数, r=副本数)"
    echo "  delete-topic <name>       删除topic"
    echo "  list-topics               列出所有topics"
    echo "  describe-topic <name>     描述topic详情"
    echo "  test-producer [topic]     启动测试生产者"
    echo "  test-consumer [topic]     启动测试消费者"
    echo "  logs [service]            查看服务日志"
    echo "  clean                     清理所有数据"
    echo "  help                      显示帮助"
    echo ""
    echo "示例:"
    echo "  $0 pull                   # 拉取镜像"
    echo "  $0 start                  # 启动集群"
    echo "  $0 create-topic my-topic 12 3  # 创建12分区3副本的topic"
    echo "  $0 test-producer my-topic # 测试生产消息"
}

# 主函数
main() {
    case "$1" in
        "pull")
            check_dependencies
            pull_images
            ;;
        "start")
            check_dependencies
            start_cluster
            ;;
        "stop")
            stop_cluster
            ;;
        "restart")
            restart_cluster
            ;;
        "status")
            cluster_status
            ;;
        "health")
            check_cluster_health
            ;;
        "create-topic")
            create_topic "$2" "$3" "$4"
            ;;
        "delete-topic")
            delete_topic "$2"
            ;;
        "list-topics")
            list_topics
            ;;
        "describe-topic")
            describe_topic "$2"
            ;;
        "test-producer")
            test_producer "$2"
            ;;
        "test-consumer")
            test_consumer "$2"
            ;;
        "logs")
            view_logs "$2"
            ;;
        "clean")
            clean_data
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        "")
            log_error "请提供命令"
            show_help
            exit 1
            ;;
        *)
            log_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@" 