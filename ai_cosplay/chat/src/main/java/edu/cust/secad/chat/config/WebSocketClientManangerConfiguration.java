package edu.cust.secad.chat.config;

import io.netty.channel.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassDescription: 加载管理client的组件
 * @Author: Nvgu
 * @Created: 2024/10/13 10:10
 * @Updated: 2024/10/13 10:10
 */

@Configuration
public class WebSocketClientManangerConfiguration {
    @Bean
    public ConcurrentHashMap<String , Channel> addressChannelConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }
}
