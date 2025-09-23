package edu.cust.secad.chat.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HttpHeadersHandler extends SimpleChannelInboundHandler<FullHttpRequest>  {

    // 定义一个 AttributeKey 用于存储真实 IP 地址
    public static final AttributeKey<String> REAL_IP_KEY = AttributeKey.valueOf("REAL_IP");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        HttpHeaders headers = request.headers();
        String realIp = headers.get("X-Forwarded-For");

        if (realIp != null) {
            realIp = realIp.split(",")[0];
        } else {
            realIp = headers.get("X-Real-IP");
        }

        if (realIp != null) {
            ctx.channel().attr(REAL_IP_KEY).set(realIp);
        }
        System.out.println("realIp:"+realIp);

        ctx.fireChannelRead(request.retain());
    }
}
