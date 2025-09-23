//package edu.cust.secad.chat.service.chat;
//
///**
// * @ClassDescription:
// * @Author: Nvgu
// * @Created: 2024/10/14 16:02
// * @Updated: 2024/10/14 16:02
// */
//
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
//import edu.cust.secad.model.base.BaseEntity;
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.Data;
//
//import java.io.Serializable;
//import java.util.Date;
//
///**
// * 表名：GroupChatMessages
// */
//@Data
//@Schema(description = "群聊消息表")
//@TableName("chat_group_mes_info")
//public class GroupChatMessages extends BaseEntity implements Serializable {
//
//    @Schema(description = "消息ID--主键")
//    @TableId(value = "mes_id")
//    private Long id;
//
//    @Schema(description = "群聊ID")
//    @TableField("group_id")
//    private Long groupId;
//
//    @Schema(description = "发消息的用户ID")
//    @TableField("user_id")
//    private Long senderId;
//
//    @Schema(description = "消息内容（字节数组）")
//    @TableField("message_content")
//    private byte[] message;  // 修改为字节数组
//
//    @Schema(description = "消息类型")
//    @TableField("message_type")
//    private Integer messageType;
//
//    @Schema(description = "发送时间")
//    @TableField("sent_time")
//    private Date sendAt;
//
//    @Schema(description = "文件路径")
//    @TableField("file_path")
//    private String file_path;
//}
