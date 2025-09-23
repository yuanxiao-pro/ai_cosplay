//
//
//package edu.cust.secad.chat.service.chat;
//
///**
// * @ClassDescription:
// * @Author: Nvgu
// * @Created: 2024/10/14 8:29
// * @Updated: 2024/10/14 8:29
// */
//
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.Data;
//
//import java.util.Date;
//
///**
// * 表名：userclient
// */
//@Data
//@Schema(description = "用户登录信息表")
//@TableName("usr_login_info")
//public class UserLoginInfo {
//
//    @Schema(description = "用户登录信息ID--主键")
//    @TableId(value = "id")
//    private Long id;
//
//    @Schema(description = "用户ID")
//    @TableField("user_id")
//    private Long userId;
//
//    @Schema(description = "用户openId")
//    @TableField("openId")
//    private Long openId;
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
//}
