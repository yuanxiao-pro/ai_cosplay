package edu.cust.secad.chat.server;
import edu.cust.secad.chat.proto.AiChatMessageProtocol;
import edu.cust.secad.chat.server.handler.HttpHeadersHandler;
import edu.cust.secad.chat.server.handler.WxComMesBinaryHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
/*
* 1v1聊天
* */
@Slf4j
@Component
public class ChatServer {
    @Value("${netty.websocket.server.port}")
    private int port;
    @Resource
    private NioEventLoopGroup bossGroup;
    @Resource
    private NioEventLoopGroup workerGroup;
    @Resource
    private ServerBootstrap serverBootstrap;
    @Autowired
    private WxComMesBinaryHandler wxComMesBinaryHandler;
    public void init() throws Exception {
        run();
    }
    public void run() throws Exception {
        // 创建事件循环组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 设置线程组
            serverBootstrap.group(bossGroup, workerGroup);

            // 指定通道类型
            serverBootstrap.channel(NioServerSocketChannel.class);

            // 添加日志处理器
            serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));

            // 添加子处理器
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();

//                //打印日志,可以看到websocket帧数据
//                pipeline.addFirst(new LoggingHandler(LogLevel.INFO));
                    // HTTP请求的解码和编码
                    System.out.println("1.HTTP请求的解码和编码");
                    pipeline.addLast(new HttpServerCodec());
                    // 把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse，
                    // 原因是HTTP解码器会在每个HTTP消息中生成多个消息对象HttpRequest/HttpResponse,HttpContent,LastHttpContent
                    System.out.println("2.把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse");
                    pipeline.addLast(new HttpObjectAggregator(65536));
                    // 获取真实IP
                    System.out.println("3.获取真实IP");
                    pipeline.addLast(new HttpHeadersHandler());

                    // 主要用于处理大数据流，比如一个1G大小的文件如果你直接传输肯定会撑暴jvm内存的; 增加之后就不用考虑这个问题了
                    System.out.println("4.处理大数据流");
                    pipeline.addLast(new ChunkedWriteHandler());
                    // WebSocket数据压缩
                    System.out.println("5.WebSocket数据压缩");
                    pipeline.addLast(new WebSocketServerCompressionHandler());

//                设置路由；协议包长度限制 ；里面加入的WebSocketServerProtocolHandshakeHandler对路由下FullHttpRequest解码成WebSocketFrame
                    System.out.println("6.设置路由");
//                pipeline.addLast(new WebSocketServerProtocolHandler("/server", null, true));
                    pipeline.addLast(new WebSocketServerProtocolHandler(
                            "/server",  // websocketPath
                            null,       // subprotocols
                            true,       // allowExtensions
                            65536,      // maxFrameSize
                            false,      // allowMaskMismatch
                            true        // checkStartsWith
                    ));
                    // 协议包解码
                    System.out.println("7.设置路由");
                    pipeline.addLast(new ProtobufVarint32FrameDecoder());
                    pipeline.addLast(new ProtobufDecoder(AiChatMessageProtocol.ChatMessageProtocolForm.getDefaultInstance()));
                    pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());

                    pipeline.addLast(new MessageToMessageDecoder<WebSocketFrame>() {
                        @Override
                        protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
                            //文本帧处理(收到的消息广播到前台客户端)
                            if (frame instanceof TextWebSocketFrame) {
                                log.info("接收到来自" + ctx.channel().remoteAddress() + "文本帧消息：" + ((TextWebSocketFrame) frame).text());
                            }
                            //二进制帧处理,将帧的内容往下传
                            else if (frame instanceof BinaryWebSocketFrame) {
                                log.info("接收到来自" + ctx.channel().remoteAddress() + "二进制帧消息");
                                BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) frame;
                                byte[] by = new byte[frame.content().readableBytes()];
                                binaryWebSocketFrame.content().readBytes(by);
                                ByteBuf bytebuf = Unpooled.buffer();
                                bytebuf.writeBytes(by);
                                out.add(bytebuf);
                            }
                        }
                    });
                    pipeline.addLast(wxComMesBinaryHandler);//bytebuf
//                    System.out.println("wxComMesBinaryHandler暂时跳过");

                }
            });

            // 启动服务器并绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            log.info("Netty WebSocket server 启动成功，监听端口: " + port);

            // 等待通道关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            System.out.println("关闭netty");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
