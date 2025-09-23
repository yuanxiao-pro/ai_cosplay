//package edu.cust.secad.chat.service;
//
//import com.baomidou.mybatisplus.extension.service.IService;
//import edu.cust.mccat.huatuo.common.result.Result;
//import edu.cust.mccat.huatuo.entity.chat.ChatGroup;
//import edu.cust.mccat.huatuo.form.*;
//import org.springframework.core.io.Resource;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.Transactional;
//
///**
// * @ClassDescription:
// * @Author: Nvgu
// * @Created: 2024/10/13 18:52
// * @Updated: 2024/10/13 18:52
// */
//public interface ChatGroupService extends IService<ChatGroup> {
//
//    Result getMesRecordByTime2(GetChatMesRecordByTime getChatMesRecordByTime);
//
//
//
//    /**
//     * 通过人员id创建群聊
//     * @param chatGroupMemberForm
//     * @return 结果类
//     */
//    Result createGroup(ChatGroupMemberForm chatGroupMemberForm);
//
//    Result deleteGroup(String groupId);
//
//    Result acceptRequest(ChatAcceptRequestForm chatAcceptRequestForm);
//
//    Result addUsers(ChatGroupMemberAddedForm chatGroupMemberAddedForm);
//
//    Result getGroupMembersInfoById(String groupId);
//
//    Result getTaskListByUserId(String userId);
//
//    ResponseEntity<Resource> getFile(String filePathString);
//
//    Result getMesRecordByTime(GetChatMesRecordByTime getChatMesRecordByTime);
//
//    Result emergencyAlarmToCpc( EmergencyAlarmToCpcInfoForm emergencyAlarmToCpcInfoForm);
//
//    Result callCpcHospitalLocationChanged(CallCpcHospitalLocationChangedForm callCpcHospitalLocationChangedForm);
//
//    Result getGroupIdByTaskId(String taskId);
//    @Transactional
//    Result sendTenPciMesToGroup(TenPciMesScheduleByDistanceFormList tenPciMesScheduleByDistanceFormList);
//
//    Result ping(PingForm pingForm);
//
//    Result pingCPC(PingForm pingForm);
//
//    Result pingTask(TaskPingForm taskPingForm);
//
//    Result hello(PingForm pingForm);
//
//    Result helloCPC(PingForm pingForm);
//
//    Result helloTask(TaskPingForm taskPingForm);
//}
