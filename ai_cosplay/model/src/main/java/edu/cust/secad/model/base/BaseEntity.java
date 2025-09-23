
package edu.cust.secad.model.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @ClassDescription: 基础实体类
 * @Author: Nvgu
 * @Created: 2024/10/23 11:18
 * @Updated: 2024/10/23 11:18
 */
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Data
public class BaseEntity implements Serializable {


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "modify_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;


    @TableField(value = "create_usr",updateStrategy = FieldStrategy.NEVER)
    private Long createdUsrId;

    @JsonIgnore
    @TableField("modify_usr")
    private Long modifiedUsrId;

    @JsonIgnore
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
