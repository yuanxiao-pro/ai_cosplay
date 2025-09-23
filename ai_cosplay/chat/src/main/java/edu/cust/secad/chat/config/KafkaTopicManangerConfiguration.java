package edu.cust.secad.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassDescription:
 * @Author: Nvgu
 * @Created: 2024/10/13 20:16
 * @Updated: 2024/10/13 20:16
 */
@Configuration
public class KafkaTopicManangerConfiguration {

    @Bean
    public ConcurrentHashMap<Long, String> GroupTopicConcurrentHashMap(){
        return new ConcurrentHashMap<>();
    }
}
