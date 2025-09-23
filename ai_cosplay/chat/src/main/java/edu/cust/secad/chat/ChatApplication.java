package edu.cust.secad.chat;

import edu.cust.secad.chat.server.ChatServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling // 启用定时任务
@ComponentScan("edu.cust.secad.*")
public class ChatApplication implements CommandLineRunner {
    @Autowired
    private ChatServer chatServer; // 注入你的组件

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("ServiceNetty 成功启动");

        new Thread(() -> {
            try {
                chatServer.init(); // 使用注入的组件
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start(); // 启动线程
    }
}
