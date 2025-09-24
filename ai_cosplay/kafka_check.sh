#!/bin/bash

echo "=== Kafka连接状态检查 ==="
echo "时间: $(date)"
echo

ports=(19092 29092 39092 49092 59092)

echo "1. 检查端口连接状态:"
for port in "${ports[@]}"; do
    if nc -z localhost $port 2>/dev/null; then
        echo "  ✅ localhost:$port - 端口开放"
    else
        echo "  ❌ localhost:$port - 端口关闭"
    fi
done

echo
echo "2. 检查进程:"
echo "  Kafka相关进程:"
ps aux | grep -i kafka | grep -v grep | head -10

echo
echo "3. 检查Kafka集群信息 (使用kafka-topics命令):"
for port in "${ports[@]}"; do
    echo "  测试端口 $port:"
    timeout 10 kafka-topics --bootstrap-server localhost:$port --list 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "    ✅ Kafka服务正常响应"
    else
        echo "    ❌ Kafka服务无响应或超时"
    fi
    echo
done

echo "4. 检查网络连接详情:"
for port in "${ports[@]}"; do
    echo "  端口 $port 详情:"
    lsof -i :$port 2>/dev/null || echo "    无进程监听此端口"
done 