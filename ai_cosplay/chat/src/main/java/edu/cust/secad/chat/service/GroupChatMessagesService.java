package edu.cust.secad.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.cust.secad.chat.proto.AiChatMessageProtocol;
import edu.cust.secad.model.chat.GroupChatMessages;
import io.netty.channel.Channel;

/**
 * @ClassDescription:
 * @Author: Nvgu
 * @Created: 2024/10/14 16:06
 * @Updated: 2024/10/14 16:06
 */
public interface GroupChatMessagesService extends IService<GroupChatMessages> {

    //保存每一条信息
//    void dealWithMes(ChatMessageProtocol.ChatMessageProtocolForm mes , Channel channel);

//    void saveMesText(String mes);

    void dealWithMes(AiChatMessageProtocol.ChatMessageProtocolForm mes, Channel channel);

    //kafka检测器，检测到有消息，取出，发送给对应的websocket客户端
    void sendMes(Object mes);
}
