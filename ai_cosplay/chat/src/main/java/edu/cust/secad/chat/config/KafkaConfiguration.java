package edu.cust.secad.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @ClassDescription: 加载一些kafka组件
 * @Author: Nvgu
 * @Created: 2024/10/13 21:15
 * @Updated: 2024/10/13 21:15
 */

@Slf4j
@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Value("${spring.kafka.common.partitions}")
    private int partitions; // 分区数

    @Value("${spring.kafka.common.replicas}")
    private short replicas;  // 副本数 (注意类型为short)

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;


    //topic工厂
    @Bean
    public KafkaTopicFactory kafkaTopicFactory() {
        return new KafkaTopicFactory(partitions, replicas);
    }

    //管理者bean，便于初始化kafka
    @Bean
    public AdminClient adminClient() {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        return AdminClient.create(config);
    }


    //生产者配置
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    //创建一个 KafkaTemplate，用于发送消息到 Kafka
    @Bean
    public KafkaTemplate<String, Object> myKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Kafka消费者配置
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // 直接在这里设置信任的包
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "edu.cust.mccat.huatuo.entity.chat");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    //消费者（监听器）容器工厂
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    //topicId---对应的消费者
    // 消费者容器
    @Bean
    public ConcurrentHashMap<String, ConcurrentMessageListenerContainer<String, Object>> consumers() {
        return new ConcurrentHashMap<>();
    }

    // topic工厂
    public static class KafkaTopicFactory {
        private final int partitions;
        private final short replicas;

        public KafkaTopicFactory(int partitions, short replicas) {
            this.partitions = partitions;
            this.replicas = replicas;
        }

        public NewTopic createTopic(String name, int partitions, short replicas) {
            return TopicBuilder.name(name)
                    .partitions(partitions)
                    .replicas(replicas)
                    .build();
        }

        public NewTopic createTopic(String name) {
            return TopicBuilder.name(name)
                    .partitions(partitions)
                    .replicas(replicas)
                    .build();
        }

        // 删除Topic
        public boolean deleteTopic(AdminClient adminClient, String topicName) {
            try {
                // 删除指定的Topic
                DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singletonList(topicName));
                KafkaFuture<Void> future = deleteTopicsResult.values().get(topicName);

                // 阻塞等待删除操作完成
                future.get(30, TimeUnit.SECONDS);  // 等待最多30秒

                System.out.println("Topic '" + topicName + "' deleted successfully.");
                return true;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                    System.err.println("Topic '" + topicName + "' does not exist.");
                } else {
                    System.err.println("Failed to delete topic '" + topicName + "': " + e.getMessage());
                }
            } catch (InterruptedException | java.util.concurrent.TimeoutException e) {
                Thread.currentThread().interrupt();
                System.err.println("Topic deletion was interrupted or timed out.");
            }
            return false;
        }
    }
}
