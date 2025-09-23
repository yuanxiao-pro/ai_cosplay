package edu.cust.secad.chat.component;

import edu.cust.secad.chat.service.GroupChatMessagesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassDescription:
 * @Author: Nvgu
 * @Created: 2024/10/15 15:59
 * @Updated: 2024/10/15 15:59
 */
//管理消费者容器的管理器（提供接口访问）
@Slf4j
@Component
public class

MyConsumerManager {

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory;

    @Autowired
    private ConcurrentHashMap<String, ConcurrentMessageListenerContainer<String, Object>> consumers;

    @Autowired
    private GroupChatMessagesService groupChatMessagesService;

    // 创建消费者
    public synchronized void startConsuming(String topicId) {
        if (consumers.containsKey(topicId)) {
            log.warn("MessageListenerContainer for topic {} is already running.", topicId);
            return;
        }

        MessageListener<String, Object> listener = record -> {

            //todo 监视器检测到消息后处理
            log.info("MessageListenerContainer received");
            groupChatMessagesService.sendMes(record.value());
        };

        ConcurrentMessageListenerContainer<String, Object> container =
                kafkaListenerContainerFactory.createContainer(topicId);
        ContainerProperties containerProperties = container.getContainerProperties();
        containerProperties.setMessageListener(listener);

        try {
            container.start();
            consumers.put(topicId, container);
            log.info("MessageListenerContainer started for topic: {}", topicId);
        } catch (Exception e) {
            log.error("Failed to start MessageListenerContainer for topic: {}", topicId, e);
        }
    }

    // 销毁消费者
    public synchronized void stopConsuming(String topicId) {
        ConcurrentMessageListenerContainer<String, Object> container = consumers.remove(topicId);
        if (container != null) {
            container.stop();
            log.info("MessageListenerContainer stopped for topic: {}", topicId);
        } else {
            log.info("No MessageListenerContainer found for topic: {}", topicId);
        }
    }

    // 销毁所有消费者
    //您正在为每个主题停止消费者并清空映射，可能还需要确保容器已释放所有资源。在适当的地方调用 container.stop() 后，
    // 可以考虑调用 container.getContainerProperties().setMessageListener(null); 来解除对监听器的引用。
    public synchronized void stopAllConsuming() {
        for (String topicId : consumers.keySet()) {
            stopConsuming(topicId);
        }
        consumers.clear();
        log.info("All MessageListenerContainers stopped.");
    }
}

