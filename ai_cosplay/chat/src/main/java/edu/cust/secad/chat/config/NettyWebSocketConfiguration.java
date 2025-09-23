package edu.cust.secad.chat.config;


import edu.cust.secad.chat.server.ChatServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassDescription: netty进行websocket连接时一些必要组件的装载
 * @Author: Nvgu
 * @Created: 2024/10/12 19:46
 * @Updated: 2024/10/12 19:46
 */

@Configuration
public class  NettyWebSocketConfiguration {

    @Bean
    public NioEventLoopGroup bossGroup() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        return bossGroup;
    }

    @Bean
    public NioEventLoopGroup workerGroup() {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();  //NioEventLoop 这里默认是cpu核心数的两倍
        return workerGroup;
    }

    @Bean
    public ServerBootstrap serverBootstrap(){
        return new ServerBootstrap();
    }

    @Bean
    public ChatServer nettyWebSocketServer(){
        return new ChatServer();
    }

    @Bean
    public ConcurrentHashMap<String , Channel> taskchannl() {
        return new ConcurrentHashMap<>();
    }
}
