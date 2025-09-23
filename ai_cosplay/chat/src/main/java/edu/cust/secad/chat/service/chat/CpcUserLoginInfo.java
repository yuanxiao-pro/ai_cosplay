//package edu.cust.secad.chat.service.chat;
//
//
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
//import edu.cust.secad.model.base.BaseEntity;
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.experimental.Accessors;
//
//import java.util.Date;
//
///**
// * @ClassDescription:
// * @Author: Nvgu
// * @Created: 2024/11/20 13:37
// * @Updated: 2024/11/20 13:37
// */
//@Data
//@EqualsAndHashCode(callSuper = false)
//@Accessors(chain = true)
//@Schema(description = "用户登录信息表")
//@TableName("cpc_usr_login_info")
//public class CpcUserLoginInfo extends BaseEntity {
//
//    private static final long serialVersionUID = 1L;
//
//    @Schema(description = "用户登录信息ID--主键")
//    @TableId(value = "id")
//    private Long id;
//
//    @Schema(description = "用户ID")
//    @TableField("user_id")
//    private Long userId;
//
//    @Schema(description = "用户登录地址")
//    @TableField("remote_address")
//    private String address;
//
//    @Schema(description = "登录时间")
//    @TableField("login_time")
//    private Date loginAt;
//
//    @Schema(description = "登出时间")
//    @TableField("logout_time")
//    private Date logoutAt;
//
//    @Schema(description = "登录账号")
//    @TableField("account_number")
//    private String accountNumber;
//
//    @Schema(description = "登录密码")
//    @TableField("account_password")
//    private String accountPassword;
//
//}