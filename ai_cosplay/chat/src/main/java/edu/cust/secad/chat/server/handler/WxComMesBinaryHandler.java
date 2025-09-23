package edu.cust.secad.chat.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import edu.cust.secad.chat.proto.AiChatMessageProtocol;
import edu.cust.secad.chat.service.GroupChatMessagesService;
import edu.cust.secad.model.base.HuaTuoException;
import edu.cust.secad.model.base.ResultCodeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassDescription:
 * @Author: Nvgu
 * @Created: 2024/10/29 9:37
 * @Updated: 2024/10/29 9:37
 */
@Slf4j
@Component
@ChannelHandler.Sharable//保证被共享
@Transactional
public class WxComMesBinaryHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Autowired
    private ConcurrentHashMap<String,Channel> taskchannl;
    private static final AttributeKey<String> TASK_ID_ATTR = AttributeKey.valueOf("taskId");
    @Autowired
    private ObjectMapper objectMapper;
//    @Autowired
//    private TaskInfoMapper taskInfoMapper;
    @Autowired
    @Qualifier("addressChannelConcurrentHashMap")
    private ConcurrentHashMap<String, Channel> addressChannelConcurrentHashMap;

    @Qualifier("myRedisTemplate")
    @Autowired
    private RedisTemplate<String, Object> myRedisTemplate;
    @Autowired
    private GroupChatMessagesService groupChatMessagesService;

//    @Autowired
//    private UserLoginInfoMapper userLoginInfoMapper;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf bytes) throws Exception {
        bytes.retain();
        byte[] bytesArray = new byte[bytes.readableBytes()];
        bytes.readBytes(bytesArray);
        AiChatMessageProtocol.ChatMessageProtocolForm message = null;
        //转化为proto对象
        try {
            message = AiChatMessageProtocol.ChatMessageProtocolForm.parseFrom(bytesArray);
        } catch (InvalidProtocolBufferException e) {
            throw new HuaTuoException(ResultCodeEnum.PROTO_MES_CHANGE_FAIL);
        }
        // 先处理心跳和问候消息
        String returnMsg = "";
        if(message.getMsgType() == 2 || message.getMsgType() == 3) {
            if(message.getMsgType() == 2) returnMsg = "pong";
            else if(message.getMsgType() == 3) returnMsg = "hello-cli";
            AiChatMessageProtocol.ChatMessageProtocolForm.Builder builder = AiChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
            AiChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm
                    = builder.setLastUid(message.getLastUid())
                    .setSendTime(String.valueOf(Instant.now().toEpochMilli()))
                    .setPkgId(message.getPkgId()+1)
                    .setSenderId(message.getSenderId())
                    .setRcvId("ChatServer")
                    .setUnionId(message.getUnionId())
                    .setLen(returnMsg.getBytes().length)
                    .setMessageContent(ByteString.copyFrom(returnMsg.getBytes()))
                    .setMsgType(3).build();
            channelHandlerContext.channel().writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
        } else groupChatMessagesService.dealWithMes(message , channelHandlerContext.channel());


    }

    //当web客户端连接后，触发方法
//    @Override
//    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
////        String clientIp = (String) ctx.channel().attr(AttributeKey.valueOf(HttpHeadersHandler.REAL_IP_KEY.name())).get();
//        System.out.println("ctx.channel().attr(HttpHeadersHandler.REAL_IP_KEY)"+ctx.channel().attr(HttpHeadersHandler.REAL_IP_KEY));
//        String clientIp = ctx.channel().attr(HttpHeadersHandler.REAL_IP_KEY).get();
//        System.out.println("clientIp:"+clientIp);
//        if (clientIp == null || clientIp.isEmpty()) {
//            String remoteAddress = ctx.channel().remoteAddress().toString();
//            if (remoteAddress.startsWith("/")) {
//                remoteAddress = remoteAddress.substring(1);
//            }
//            clientIp = remoteAddress.split(":")[0];
//        }
//        log.info("成功与" + clientIp + "建立连接");
//        //加入map中进行管理
//        addressChannelConcurrentHashMap.put(clientIp, ctx.channel());
//        log.info("已连接数量：" + addressChannelConcurrentHashMap.size());
//        super.handlerAdded(ctx);
//    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }
    public String subId(String mes){
        String[] parts = mes.split("%%%%%");

        // 如果分割后的数组长度大于 0，取最后一个部分
        if (parts.length > 0) {
            String id = parts[parts.length - 1];
            return id;
        } else {
            System.err.println("未找到 ID");
            return null;
        }
    }
    public Boolean isTask(String mes){
        String[] parts = mes.split("%%%%%");
        if(parts[1].equals("TASK")){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }

    //当web客户端断开连接后，触发方法
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }
    public void removeAllChannels(){
        for (Channel channel:taskchannl.values()){
            if(channel!=null&&channel.isActive()){
                channel.close();
            }
       }
    }
    //在连接过程中出现异常，触发方法
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("发生异常，关闭连接", cause);
        String taskId = ctx.channel().attr(TASK_ID_ATTR).get();
        String clientIp = ctx.channel().attr(HttpHeadersHandler.REAL_IP_KEY).get();
        if (clientIp == null || clientIp.isEmpty()) {
            String remoteAddress = ctx.channel().remoteAddress().toString();
            if (remoteAddress.startsWith("/")) {
                remoteAddress = remoteAddress.substring(1);
            }
            clientIp = remoteAddress.split(":")[0];
        }
        //连接关闭
        ctx.close();
        //将连接从map中移除
        if(taskId!=null){
            taskchannl.remove(taskId);
        }
        if (clientIp != null) {
            addressChannelConcurrentHashMap.remove(clientIp);
        } else {
            log.error("客户端 IP 地址为空，无法从 map 中移除连接");
        }
    }
}
