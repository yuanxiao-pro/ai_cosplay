package edu.cust.secad.chat.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Kafka连接测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/kafka-test")
public class KafkaTestController {

    @GetMapping("/test-ports")
    public Map<String, Object> testKafkaPorts() {
        Map<String, Object> result = new HashMap<>();
        List<String> ports = Arrays.asList("19092", "29092", "39092", "49092", "59092", "9092");
        Map<String, Boolean> portStatus = new HashMap<>();
        List<String> availablePorts = new ArrayList<>();

        for (String port : ports) {
            boolean isConnectable = testPortConnection("localhost", Integer.parseInt(port));
            portStatus.put(port, isConnectable);
            if (isConnectable) {
                availablePorts.add("localhost:" + port);
            }
        }

        result.put("portStatus", portStatus);
        result.put("availablePorts", availablePorts);
        result.put("totalAvailable", availablePorts.size());

        return result;
    }

    @GetMapping("/test-kafka-connection")
    public Map<String, Object> testKafkaConnection(@RequestParam(defaultValue = "localhost:19092") String bootstrapServers) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试Admin客户端连接
            Map<String, Object> adminProps = new HashMap<>();
            adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            adminProps.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
            adminProps.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 10000);

            try (AdminClient adminClient = AdminClient.create(adminProps)) {
                ListTopicsResult listTopics = adminClient.listTopics();
                Set<String> topics = listTopics.names().get(15, TimeUnit.SECONDS);
                
                result.put("adminClientSuccess", true);
                result.put("topics", topics);
                result.put("topicCount", topics.size());
            }

        } catch (Exception e) {
            result.put("adminClientSuccess", false);
            result.put("adminClientError", e.getMessage());
            log.error("Admin client connection failed", e);
        }

        try {
            // 测试生产者连接
            Properties producerProps = new Properties();
            producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producerProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
            producerProps.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 10000);

            try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
                // 获取元数据来测试连接，使用已知存在的topic
                producer.partitionsFor("disTopic");
                result.put("producerSuccess", true);
            }

        } catch (Exception e) {
            result.put("producerSuccess", false);
            result.put("producerError", e.getMessage());
            log.error("Producer connection failed", e);
        }

        try {
            // 测试消费者连接
            Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
            consumerProps.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);

            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
                consumer.listTopics();
                result.put("consumerSuccess", true);
            }

        } catch (Exception e) {
            result.put("consumerSuccess", false);
            result.put("consumerError", e.getMessage());
            log.error("Consumer connection failed", e);
        }

        return result;
    }

    @GetMapping("/test-all-combinations")
    public Map<String, Object> testAllPortCombinations() {
        Map<String, Object> result = new HashMap<>();
        List<String> ports = Arrays.asList("19092", "29092", "39092", "49092", "59092");
        Map<String, Map<String, Object>> testResults = new HashMap<>();

        for (String port : ports) {
            String server = "172.20.0.1:" + port;
            testResults.put(server, testKafkaConnection(server));
        }

        result.put("testResults", testResults);
        return result;
    }
    
    @GetMapping("/test-docker-cluster")
    public Map<String, Object> testDockerCluster() {
        String clusterServers = "172.20.0.1:19092,172.20.0.1:29092,172.20.0.1:39092,172.20.0.1:49092,172.20.0.1:59092";
        return testKafkaConnection(clusterServers);
    }

    private boolean testPortConnection(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 5000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
} 