
package edu.cust.secad.model.chat;

/**
 * @ClassDescription:
 * @Author: Nvgu
 * @Created: 2024/10/13 15:12
 * @Updated: 2024/10/13 15:12
 */

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.cust.secad.model.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 表名：groupchats
 */
@Data
@TableName("chat_group_info")
@Schema(description = "群聊")
public class ChatGroup extends BaseEntity {

    @Schema(description = "群聊id")
    @TableId(value = "group_id")
    private Long id;

    @Schema(description = "任务id")
    @TableField(value = "task_id")
    private Long taskId;

    @Schema(description = "群聊创建时间")
    @TableField("created_time")
    private Date createdAt;

    @Schema(description = "群聊over时间")
    @TableField("overed_time")
    private Date overedAt;

}