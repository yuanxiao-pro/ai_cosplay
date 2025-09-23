
package edu.cust.secad.model.chat;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.cust.secad.model.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @ClassDescription:
 * @Author: Nvgu
 * @Created: 2024/10/13 19:07
 * @Updated: 2024/10/13 19:07
 */
@Data
@TableName("group_member_info")
@Schema(description = "群成员表")
public class GroupMembers extends BaseEntity {
    @Schema(description = "主键")
    @TableId("id")
    private Long id;

    @Schema(description = "群聊ID")
    @TableField("group_id")
    private Long groupId;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "加入时间")
    @TableField("joined_time")
    private Date joinedAt;

}


