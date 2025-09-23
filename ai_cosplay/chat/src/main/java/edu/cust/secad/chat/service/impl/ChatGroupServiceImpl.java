//package edu.cust.secad.chat.service.impl;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.protobuf.ByteString;
//import edu.cust.secad.model.chat.ChatGroup;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.Channel;
//import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.admin.AdminClient;
//import org.apache.kafka.clients.admin.NewTopic;
//import org.apache.kafka.common.errors.TopicExistsException;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.io.Resource;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.StringUtils;
//
//import java.io.File;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutionException;
//
///**
// * @ClassDescription:
// * @Author: Nvgu
// * @Created: 2024/10/13 18:53
// * @Updated: 2024/10/13 18:53
// */
//@Slf4j
//@Service
//@Transactional//事务
//public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupMapper, ChatGroup> implements ChatGroupService {
//
//    @Autowired
//    private AdminClient adminClient;
//    @Autowired
//    private KafkaConfiguration.KafkaTopicFactory kafkaTopicFactory;
//    @Autowired
//    private ConcurrentHashMap<String,Channel> taskchannl;
//    @Autowired
//    private ChatGroupMapper chatGroupMapper;
//
//    @Autowired
//    private GroupMembersMapper groupMembersMapper;
//
//    @Autowired
//    private ConcurrentHashMap<Long, String> groupTopicConcurrentHashMap;
//    @Autowired
//    private UserLoginInfoMapper userLoginInfoMapper;
//
//    @Autowired
//    private DoctorInfoMapper doctorInfoMapper;
//
//    @Autowired
//    private DoctorInChargeInfoMapper doctorInChargeInfoMapper;
//
//    @Autowired
//    private AuditorInfoMapper auditorInfoMapper;
//
//    @Autowired
//    private AmbulancesInfoMapper ambulancesInfoMapper;
//
////    @Resource
////    private ThreadPoolTaskExecutor mesProcessSaveThreadPoolTaskExecutor;
//
//
//    @Autowired//存放userid --- 对应的客户端websocket channel
//    private ConcurrentHashMap<String, Channel> addressChannelConcurrentHashMap;
//
//
//    @Autowired
//    private MyConsumerManager myConsumerManager;
//
//    @Autowired
//    private GroupChatMessagesMapper groupChatMessagesMapper;
//
//    @Autowired
//    private CpcUserLoginInfoMapper cpcUserLoginInfoMapper;
//
////    @Resource
////    private ThreadPoolTaskExecutor mesProcessSendThreadPoolTaskExecutor;
//
//    @Autowired
//    private RedisTemplate<String, Object> myRedisTemplate;
//
//    @Autowired
//    private TaskInfoMapper taskInfoMapper;
//
//    @Autowired
//    private UUIDGenerator uuidGenerator;
//
//
//    @Value("${file.basePath}")
//    private String fileBasePath;
//
//    @Override
//    public Result getMesRecordByTime2(GetChatMesRecordByTime getChatMesRecordByTime) {
//        String groupId = getChatMesRecordByTime.getGroupId();
//        Date time = getChatMesRecordByTime.getTime();
//        log.info("正在查询groupid：{} ， time：{}之后的所有数据", groupId, time);
//
//        QueryWrapper<GroupChatMessages> queryWrapper = new QueryWrapper<>();
//// 设置查询条件，查询时间点之后的记录
//        queryWrapper.eq("group_id", groupId)
//                .ge("send_time", time)  // 修改为 gt，表示查询时间点之后的记录 >=
//                .orderByAsc("send_time");  // 按时间升序排序
//
//        List<GroupChatMessages> groupChatMessagesList = groupChatMessagesMapper.selectList(queryWrapper);
//
//        if (groupChatMessagesList == null) {
//            throw new HuaTuoException(ResultCodeEnum.DATA_ERROR);
//        }
//        List<ChatMesVo> chatMesVosList = new ArrayList<>();
//        for (GroupChatMessages groupChatMessages : groupChatMessagesList) {
//
//            log.info("groupChatMessagesList的大小为：{}", groupChatMessagesList.size());
//            ChatMesVo chatMesVo = new ChatMesVo();
//            BeanUtils.copyProperties(groupChatMessages, chatMesVo);
//            chatMesVo.setSenderId(String.valueOf(groupChatMessages.getSenderId()));
//
//            chatMesVosList.add(chatMesVo);
//        }
//        log.info("chatMesVosList的大小为：{}", chatMesVosList.size());
//        return Result.ok(chatMesVosList);
//    }
//
//
//    @Override
//    @Transactional
//    public Result createGroup(ChatGroupMemberForm chatGroupMemberForm) {
//
//        Date date = new Date();//获得当前时间
//        String cpcUserIdString = chatGroupMemberForm.getCpcUserId();
//        Long cpcUserId = Long.parseLong(cpcUserIdString);
//        String taskIdString = chatGroupMemberForm.getTaskId();
//        Long taskId = Long.parseLong(taskIdString);
//
//        //创建群聊id和topicId
//        UUID topicId = UUID.randomUUID();
//        String topicIdString = topicId.toString();
//        long groupId = uuidGenerator.nextId();
//
//        //将群信息存入数据库
//        ChatGroup chatGroup = new ChatGroup();
//        chatGroup.setId(groupId);//插入组id
//        chatGroup.setModifiedUsrId(cpcUserId);
//        chatGroup.setCreatedUsrId(cpcUserId);
//        chatGroup.setTaskId(taskId);
//        chatGroupMapper.insert(chatGroup);
//        log.info("cpc成功创建群聊");
//
//
//        UpdateWrapper<TaskInfo> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.eq("task_id", taskId)  // 设置条件
//                .set("group_id", groupId);  // 设置更新的字段
//
//        int update = taskInfoMapper.update(null, updateWrapper);  // 传递 null，表示不需要传入实体类对象
//
////        这里不能存入组表因为不清楚用户是否同意进入群聊
//        //将组成员信息存入组表(这里其实只有cpc一个人)
//        Long memberId = cpcUserId;
//        GroupMembers groupMembers = new GroupMembers();
//        groupMembers.setGroupId(groupId);
//        groupMembers.setUserId(memberId);
//        groupMembers.setModifiedUsrId(1L);
//        groupMembers.setCreatedUsrId(1L);
//        groupMembersMapper.insert(groupMembers);
//        log.info("cpcId:" + memberId + "成功加入群聊，群聊id为：" + groupId);
//
//        //将群聊id和topic进行绑定,存入到map中
//        groupTopicConcurrentHashMap.put(groupId, topicIdString);
//        log.info("现放到map中的数据是：");
//        log.info("groupId" + groupId + "    topicIdString:" + groupTopicConcurrentHashMap.get(groupId));
//
//        //kafka中创建对应topic
//        //创建对应的消费者(监视器)
//        NewTopic newTopic = kafkaTopicFactory.createTopic(topicIdString);//通过topicid来创建topic
//        try {
//            // 同步创建主题
//            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
//            log.info("Topic:" + topicId + " created successfully!");
//
//            //创建对应的消费者(监视器)
//            myConsumerManager.startConsuming(topicIdString);
//            log.info("Topic:" + topicId + "对应的（消费者）监视器 created successfully!");
//        } catch (TopicExistsException e) {
//            // 处理主题已存在的情况
//            log.info("Topic:" + topicId + " already exists.");
//        } catch (InterruptedException | ExecutionException e) {
//            // 处理其他可能的异常
//            e.printStackTrace();
//            Thread.currentThread().interrupt(); // 恢复中断状态
//        }
//
//        //todo
//        //向对应的用户们发送，拉入群聊请求
//
//        ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
//        CpcUserLoginInfo userLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("cpc:session"), CpcUserLoginInfo.class);
//        if (userLoginInfo == null) {
//            //表示调度中心所选的这个人没有上线
//            //todo
//            log.error("表示调度中心所选的这个人没有上线");
//        } else {
//            //这个人已经上线了，通过socketaddress重新获得channel，发送消息
//            String userSocketAddressString = userLoginInfo.getAddress();
//            Channel channel = addressChannelConcurrentHashMap.get(userSocketAddressString);
//
//            if (channel == null) {
////                throw new HuaTuoException(ResultCodeEnum.MIS_NETTY_CHANNEL);
//                log.error(userLoginInfo.getUserId()+"WebSocket未连接");
//            } else {
//                //发送消息给客户端，通知群聊创建成功
//                ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//                ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                        .setSenderId("cpc中心用户")
//                        .setMessageContent(ByteString.copyFrom("调度中心创建了一个任务，请你赶快查看".getBytes()))
//                        .setGroupId("" + groupId)
//                        .setMessageType(1).build();
//                channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//                log.info("发送任务初始化成功消息向：" + userSocketAddressString + "成功");
//            }
//
//        }
//
//
//        return Result.ok(String.valueOf(groupId));
//    }
//
//    @Override
//    public Result deleteGroup(String groupId) {
//
//        //更新数据库
//        //组表
//        UpdateWrapper<ChatGroup> updateWrapper = new UpdateWrapper<>();
//        log.info("groupId:{}", groupId);
//        updateWrapper.eq("group_id", groupId) // 指定条件
//                .set("overed_time", new Date()); // 设置要更新的字段
//        chatGroupMapper.update(null, updateWrapper);
//
//        //成员表
//        QueryWrapper<GroupMembers> wrapper = new QueryWrapper<GroupMembers>().eq("group_id", groupId);
//        List<GroupMembers> groupMembersList = groupMembersMapper.selectList(wrapper);
//        groupMembersMapper.delete(wrapper);//删除群中对应的人员信息
//        //给群里面的人发送群聊关闭的消息
//        for (GroupMembers groupMembers : groupMembersList) {
//            Long memberId = groupMembers.getUserId();
//            ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
//            UserLoginInfo userLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("chat:userId:" + memberId), UserLoginInfo.class);
//            if (userLoginInfo == null) {
//                //表示调度中心所选的这个人没有上线
//                //todo
//                log.error("表示调度中心所选的这个人没有上线");
//                continue;
//            } else {
//                //这个人已经上线了，通过socketaddress重新获得channel，发送消息
//                String userSocketAddressString = userLoginInfo.getAddress();
//                Channel channel = addressChannelConcurrentHashMap.get(userSocketAddressString);
//
//                if (channel == null) {
////                    throw new HuaTuoException(ResultCodeEnum.MIS_NETTY_CHANNEL);
//                    log.error(userLoginInfo.getUserId()+"WebSocket未连接");
//                } else {
//                    //发送消息给客户端，通知群聊创建成功
//                    ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//                    ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                            .setSenderId("cpc中心用户")
//                            .setMessageContent(ByteString.copyFrom("调度中心删除了一个任务，请你赶快查看".getBytes()))
//                            .setGroupId("" + groupId)
//                            .setMessageType(6).build();
//                    channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//                    log.info("发送任务销毁成功消息向：" + userSocketAddressString + "成功");
//                }
//
//            }
//        }
//
//
//        //从map中移除组id和topicid的映射关系
//        String topicIdString = groupTopicConcurrentHashMap.remove(Long.parseLong(groupId));
//        log.info("需要删除的topicId为：{}", topicIdString);
//
//        //移除对应topicId的监视器
//        myConsumerManager.stopConsuming(topicIdString);
//
//        //删除对应的topic，从kafka中
//        kafkaTopicFactory.deleteTopic(adminClient, topicIdString);
//
//
//        return Result.ok("删除群聊成功");
//    }
//
//
//    @Override
//    public Result acceptRequest(ChatAcceptRequestForm chatAcceptRequestForm) {
//        //通过websocket 来通知群聊的发起者
//        Long userId = chatAcceptRequestForm.getUserId();
//
//        boolean exists = ambulancesInfoMapper.exists(new LambdaQueryWrapper<AmbulancesInfo>().eq(AmbulancesInfo::getAmbulanceId, userId));
//
//        if (exists) {
//            ambulancesInfoMapper.update(
//                    new AmbulancesInfo().setStatus(0),  // 设置要更新的字段和新值
//                    new LambdaQueryWrapper<AmbulancesInfo>().eq(AmbulancesInfo::getAmbulanceId, userId)  // 设置查询条件
//            );
//        }
//
//        log.info("userId" + userId);
//        String groupId = chatAcceptRequestForm.getGroupId();
//
//        //修改数据库中的组表，修改组信息，添加对应人员到组中
//        GroupMembers groupMembers = new GroupMembers();
//        groupMembers.setGroupId(Long.parseLong(groupId));
//        groupMembers.setUserId(userId);
//        groupMembers.setCreatedUsrId(1L);
//        groupMembers.setModifiedUsrId(1L);
//        groupMembersMapper.insert(groupMembers);
//        log.info(userId + "成功加入群聊:" + groupId);
//
//        return Result.ok("成功加入群聊");
//    }
//
//    @Override
//    public Result addUsers(ChatGroupMemberAddedForm chatGroupMemberAddedForm) {
//        String groupId = chatGroupMemberAddedForm.getGroupId();
//        String mes = chatGroupMemberAddedForm.getMes();
//        for(Long list:chatGroupMemberAddedForm.getMemberIdList()){
//            System.out.println(list.toString());
//        }
//        List<Long> memberIdList = chatGroupMemberAddedForm.getMemberIdList();
//        if (memberIdList == null || !StringUtils.hasText(groupId)) {
//            throw new HuaTuoException(ResultCodeEnum.DATA_ERROR);
//        }
//        //向对应的用户们发送，拉入群聊请求
//        for (Long memberId : memberIdList) {
//            log.info("向用户：{}发送拉群请求", memberId);
//            ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
//            UserLoginInfo userLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("chat:userId:" + memberId), UserLoginInfo.class);
//            if (userLoginInfo == null) {
//                //表示调度中心所选的这个人没有上线
//                log.error("表示调度中心所选的这个人没有上线");
//                continue;
//            } else {
//                //这个人已经上线了，通过socketaddress重新获得channel，发送消息
//                String userSocketAddressString = userLoginInfo.getAddress();
//                Channel channel = addressChannelConcurrentHashMap.get(userSocketAddressString);
//
//                if (channel == null) {
////                    throw new HuaTuoException(ResultCodeEnum.MIS_NETTY_CHANNEL);
//                    log.error(userLoginInfo.getUserId()+"WebSocket未连接");
//
//
//                } else {
//                    //发送消息给客户端，通知群聊创建成功
//                    ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//                    ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                            .setSenderId("cpc中心用户")
//                            .setMessageContent(ByteString.copyFrom(mes.getBytes()))
//                            .setGroupId("" + groupId)
//                            .setMessageType(1).build();
//                    channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//                    log.info("发送任务初始化成功消息向：" + userSocketAddressString + "成功");
//                }
//
//            }
//        }
//
//        return Result.ok();
//    }
//
//    @Override
//    public Result getGroupMembersInfoById(String groupId) {
//        List<GroupMembers> groupMembersList = groupMembersMapper.selectList(new QueryWrapper<GroupMembers>().eq("group_id", groupId));
//        List<UserInfoVo> userIdList = new ArrayList<>();
//        for (GroupMembers groupMembers : groupMembersList) {
//            Long userId = groupMembers.getUserId();
//            UserInfoVo userInfoVo = getUserInfoByUserId(userId);
//            if (userInfoVo == null) {
//                throw new HuaTuoException(ResultCodeEnum.DATA_ERROR);
//            }
//            userIdList.add(userInfoVo);
//        }
//        log.info("通过群聊id获取群聊用户信息成功，用户人数为：" + userIdList.size());
//        return Result.ok(userIdList);
//    }
//
//    public UserInfoVo getUserInfoByUserId(Long userId) {
//        log.info("正在获取userId:" + userId + "用户信息");
//        UserInfoVo userInfoVo = new UserInfoVo();
//        userInfoVo.setUserId(userId);
//        LambdaQueryWrapper<AmbulancesInfo> queryWrapper1 = new LambdaQueryWrapper<>();
//        queryWrapper1.eq(AmbulancesInfo::getAmbulanceId, userId);
//        AmbulancesInfo ambulancesInfo = ambulancesInfoMapper.selectOne(queryWrapper1);
//        if (ambulancesInfo != null) {
//            userInfoVo.setUserName(ambulancesInfo.getChargeName());
//            userInfoVo.setUserType(1);
//            return userInfoVo;
//        }
//
//        LambdaQueryWrapper<DoctorInChargeInfo> queryWrapper2 = new LambdaQueryWrapper<>();
//        queryWrapper2.eq(DoctorInChargeInfo::getDoctorId, userId);
//        DoctorInChargeInfo doctorInChargeInfo = doctorInChargeInfoMapper.selectOne(queryWrapper2);
//        if (doctorInChargeInfo != null) {
//            userInfoVo.setUserType(2);
//            userInfoVo.setUserName(doctorInChargeInfo.getDoctorName());
//            return userInfoVo;
//        }
//
//        LambdaQueryWrapper<DoctorInfo> queryWrapper3 = new LambdaQueryWrapper<>();
//        queryWrapper3.eq(DoctorInfo::getDoctorId, userId);
//        DoctorInfo doctorInfo = doctorInfoMapper.selectOne(queryWrapper3);
//        if (doctorInfo != null) {
//            userInfoVo.setUserType(3);
//            userInfoVo.setUserName(doctorInfo.getDoctorName());
//            return userInfoVo;
//        }
//
//        LambdaQueryWrapper<AuditorInfo> queryWrapper4 = new LambdaQueryWrapper<>();
//        queryWrapper4.eq(AuditorInfo::getAuditorId, userId);
//        AuditorInfo auditorInfo = auditorInfoMapper.selectOne(queryWrapper4);
//        if (auditorInfo != null) {
//            userInfoVo.setUserType(4);
//            userInfoVo.setUserName(auditorInfo.getAuditorName());
//            return userInfoVo;
//        }
//        log.info("获取userId:" + userId + "用户信息失败!");
//        return null;
//    }
//
//
//    @Override
//    public Result getTaskListByUserId(String userId) {
//        List<GroupMembers> groupMembersList = groupMembersMapper.selectList(new QueryWrapper<GroupMembers>().eq("user_id", Long.parseLong(userId)));
//        List<String> taskList = new ArrayList<>();
//        for (GroupMembers groupMembers : groupMembersList) {
//            taskList.add(groupMembers.getGroupId().toString());
//        }
////        log.info("请求任务列表成功！有多少个任务：" + taskList.size());
//        return Result.ok(taskList);
//    }
//
//    private String determineContentType(String filePath) {
//        if (filePath.endsWith(".mp4")) {
//            return "video/mp4";
//        } else if (filePath.endsWith(".mp3")) {
//            return "audio/mp3";
//        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
//            return "image/jpeg";
//        } else if (filePath.endsWith(".png")) {
//            return "image/png";
//        } else if (filePath.endsWith(".gif")) {
//            return "image/gif";
//        } else if (filePath.endsWith(".pdf")) {
//            return "application/pdf";
//        } else if (filePath.endsWith(".txt")) {
//            return "text/plain";
//        } else {
//            return "application/octet-stream"; // 默认二进制流
//        }
//    }
//
//    @Override
//    public ResponseEntity<Resource> getFile(String filePathString) {
//        String filePath = filePathString.replaceAll("&", "/");
//        String path = fileBasePath + filePath;
//        log.info("正在访问文件，文件地址为：{}", path);
//
//        try {
//            File file = new File(path);
//
//            if (!file.exists()) {
//                throw new HuaTuoException(ResultCodeEnum.DATA_ERROR);
//            }
//
//            String contentType = Files.probeContentType(file.toPath());
//
//            if (contentType == null || contentType.startsWith("text")) {
//                contentType = "text/plain;charset=UTF-8";
//            }
//
//            String encodedFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.toString()).replace("+", "%20");
//
//            Resource resource = new FileSystemResource(file);
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
//                    .body(resource);
//
//        } catch (Exception e) {
//            log.error("Error while retrieving file: {}", e.getMessage());
//            throw new HuaTuoException(ResultCodeEnum.DATA_ERROR);
//        }
//    }
//
//
//
//    @Override
//    public Result getMesRecordByTime(GetChatMesRecordByTime getChatMesRecordByTime) {
//        String groupId = getChatMesRecordByTime.getGroupId();
//        Date time = getChatMesRecordByTime.getTime();
//        Integer num = getChatMesRecordByTime.getNum();
//        log.info("正在查询groupid：{} ， time：{}之前的num：{}条数据", groupId, time, num);
//
////        List<GroupChatMessages> groupChatMessagesList = groupChatMessagesMapper.selectByNumAndTime(Long.parseLong(groupId), time, num);
//        QueryWrapper<GroupChatMessages> queryWrapper = new QueryWrapper<>();
//        // 设置查询条件
//        queryWrapper.eq("group_id", groupId)
//                .lt("send_time", time)
//                .orderByAsc("send_time");
//
//        // 使用 `last` 来添加 `LIMIT` 子句
//        queryWrapper.last("LIMIT " + num);
//        List<GroupChatMessages> groupChatMessagesList = groupChatMessagesMapper.selectList(queryWrapper);
//        if (groupChatMessagesList == null) {
//            throw new HuaTuoException(ResultCodeEnum.DATA_ERROR);
//        }
//        List<ChatMesVo> chatMesVosList = new ArrayList<>();
//        for (GroupChatMessages groupChatMessages : groupChatMessagesList) {
//
//            log.info("groupChatMessagesList的大小为：{}", groupChatMessagesList.size());
//            ChatMesVo chatMesVo = new ChatMesVo();
//            BeanUtils.copyProperties(groupChatMessages, chatMesVo);
//            chatMesVo.setSenderId(String.valueOf(groupChatMessages.getSenderId()));
//
//            chatMesVosList.add(chatMesVo);
//        }
//        log.info("chatMesVosList的大小为：{}", chatMesVosList.size());
//        return Result.ok(chatMesVosList);
//    }
//
//
//
//
//    @Override
//    public Result emergencyAlarmToCpc(EmergencyAlarmToCpcInfoForm healthRecords) {
//        System.out.println("emergencyAlarmToCpc "+healthRecords.toString());
//        System.out.println(healthRecords.getDiseaseTime());
//        System.out.println("医生id"+healthRecords.getUserId());
//        DoctorInfo doctorInfo = doctorInfoMapper.selectById(healthRecords.getUserId());
//        System.out.println("医生姓名"+doctorInfo.getDoctorName());
//        ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
//        CpcUserLoginInfo userLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("cpc:session"), CpcUserLoginInfo.class);
//        String healthRecordsJsonString;
//        // 将 Java 对象转换为 JSON 字符串
//        try {
//            healthRecordsJsonString = objectMapper.writeValueAsString(healthRecords);
//            if (healthRecordsJsonString == null) {
//                throw new HuaTuoException(ResultCodeEnum.DATA_ERROR);
//            }
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        if (userLoginInfo == null) {
//            log.error("表示调度中心所选的这个人没有上线");
//        } else {
//            //这个人已经上线了，通过socketaddress重新获得channel，发送消息
//            String userSocketAddressString = userLoginInfo.getAddress();
//            Channel channel = addressChannelConcurrentHashMap.get(userSocketAddressString);
//
//            if (channel == null) {
////                throw new HuaTuoException(ResultCodeEnum.MIS_NETTY_CHANNEL);
//                log.error(userLoginInfo.getUserId()+"WebSocket未连接");
//            } else {
//
//                //发送消息给客户端，通知群聊创建成功
//                ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//                ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                        .setSenderId(healthRecords.getUserId())
//                        .setMessageContent(ByteString.copyFrom((healthRecordsJsonString+"%%%%%"+doctorInfo.getDoctorName()+"%%%%%"+doctorInfo.getDoctorBelongingName()+"%%%%%"+doctorInfo.getDoctorContact()).getBytes()))
//                        .setGroupId("")
//                        .setMessageType(7).build();
//                channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//                log.info("发送紧急任务成功消息向：" + userSocketAddressString + "成功");
//            }
//        }
//        return Result.ok();
//    }
//
//    @Override
//    public Result callCpcHospitalLocationChanged(CallCpcHospitalLocationChangedForm callCpcHospitalLocationChangedForm) {
//        String changedLocationId = callCpcHospitalLocationChangedForm.getChangedLocationId();
//        String groupId = callCpcHospitalLocationChangedForm.getGroupId();
//        String userId = callCpcHospitalLocationChangedForm.getUserId();
//
//        ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
//        CpcUserLoginInfo userLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("cpc:session"), CpcUserLoginInfo.class);
//        // 将 Java 对象转换为 JSON 字符串
//        if (userLoginInfo == null) {
//            log.error("表示调度中心所选的这个人没有上线");
//        } else {
//            TaskInfo taskInfo = taskInfoMapper.selectOne(new LambdaQueryWrapper<TaskInfo>().eq(TaskInfo::getGroupId, groupId));
//            Channel channel =taskchannl.get(String.valueOf(taskInfo.getTaskId()));
//            if (channel == null) {
////                throw new HuaTuoException(ResultCodeEnum.MIS_NETTY_CHANNEL);
//                log.error(userLoginInfo.getUserId()+"WebSocket未连接");
//            } else {
//                //发送消息给客户端，通知群聊创建成功
//                ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//                ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                        .setSenderId(userId)
//                        .setMessageContent(ByteString.copyFrom(changedLocationId.getBytes()))
//                        .setGroupId(groupId)
//                        .setMessageType(8).build();
//                channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//                log.info("发送120向cpc患者目的医院发生变更消息向：" + taskInfo.getTaskId() + "成功");
//            }
//        }
//        return Result.ok();
//    }
//
//    @Override
//    public Result getGroupIdByTaskId(String taskId) {
//        if(taskId==null||taskId.equals("undefined")){
//            return Result.build(null ,ResultCodeEnum.ARGUMENT_VALID_ERROR.getCode(),ResultCodeEnum.ARGUMENT_VALID_ERROR.getMessage());
//        }
//        ChatGroup chatGroup = chatGroupMapper.selectOne(new QueryWrapper<ChatGroup>().eq("task_id", Long.parseLong(taskId)));
//        if (chatGroup == null) {
//            throw new HuaTuoException(ResultCodeEnum.DATA_ERROR);
//        }
//        return Result.ok(String.valueOf(chatGroup.getId()));
//    }
//
//    @Override
//    public Result sendTenPciMesToGroup(TenPciMesScheduleByDistanceFormList tenPciMesScheduleByDistanceFormList) {
//        List<TenPciMesScheduleByDistanceForm> list = tenPciMesScheduleByDistanceFormList.getList();
//        String groupId = null;
//        //通过患者地址获得调度的10条pci医院信息拼接成字符串
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("iPhone用户请务必关闭静音模式");
//        int count = 0;
//        for (TenPciMesScheduleByDistanceForm tenPciMesScheduleByDistanceForm : list) {
//            log.info("10条pci医院信息为{}",stringBuilder);
//            stringBuilder.append("【"+(count+1) + "." + tenPciMesScheduleByDistanceForm.getPciName() + "】:预计" + tenPciMesScheduleByDistanceForm.getTime()+"\n");
//            groupId = tenPciMesScheduleByDistanceForm.getGroupId();
//            count ++;
//        }
//        String mes = new String(stringBuilder);
//        log.info("10条pci医院信息为{}",mes);
//        if(mes == null || mes.isEmpty()){
//            log.error("10条pc医院的信息为空");
//        }
//        ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
//        CpcUserLoginInfo userLoginInfo2 = objectMapper.convertValue(myRedisTemplate.opsForValue().get("cpc:session"), CpcUserLoginInfo.class);
//        GroupChatMessages groupChatMessages = new GroupChatMessages();
//        groupChatMessages.setGroupId(Long.valueOf(list.get(0).getGroupId()));
//        groupChatMessages.setSenderId(userLoginInfo2.getUserId());
//        groupChatMessages.setMessage(mes.getBytes());
//        groupChatMessages.setMessageType(2);
//        groupChatMessages.setSendAt(new Date());
//        groupChatMessages.setCreatedUsrId(userLoginInfo2.getUserId());
//        groupChatMessages.setModifiedUsrId(userLoginInfo2.getUserId());
//        groupChatMessages.setFilePath("无地址");
//        try {
//            groupChatMessagesMapper.insert(groupChatMessages);
//            System.out.println("插入成功");
//
//        } catch (Exception e) {
//            e.printStackTrace();  // 打印堆栈信息
//            throw new RuntimeException("向数据库里插入信息失败");
//        }
//        List<GroupMembers> groupMembersList = groupMembersMapper.selectList(new LambdaQueryWrapper<GroupMembers>().eq(GroupMembers::getGroupId, groupId));
//
//        for (GroupMembers groupMembers : groupMembersList) {
//            Long membersUserId = groupMembers.getUserId();
//            UserLoginInfo userLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("chat:userId:" + membersUserId), UserLoginInfo.class);
//            if (userLoginInfo == null) {
//                log.error("表示调度中心所选的这个人没有上线");
//                continue;
//            }else if(userLoginInfo.getUserId().equals(userLoginInfo2.getUserId())){
//                TaskInfo taskInfo = taskInfoMapper.selectOne(new LambdaQueryWrapper<TaskInfo>().eq(TaskInfo::getGroupId, groupId));
//                Channel channel = taskchannl.get(String.valueOf(taskInfo.getTaskId()));
//                ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//                ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                        .setSenderId(Long.toString(userLoginInfo2.getUserId()))
//                        .setMessageContent(ByteString.copyFrom(mes.getBytes()))
//                        .setGroupId("" + groupId)
//                        .setMessageType(2).build();
//                channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//            }
//            else {
//                //这个人已经上线了，通过socketaddress重新获得channel，发送消息
//                String userSocketAddressString = userLoginInfo.getAddress();
//                Channel channel = addressChannelConcurrentHashMap.get(userSocketAddressString);
//
//                if (channel == null) {
////                    throw new HuaTuoException(ResultCodeEnum.MIS_NETTY_CHANNEL);
//                    log.error(userLoginInfo.getUserId()+"WebSocket未连接");
//                } else {
//                    //发送消息给客户端，通知群聊创建成功
//                    ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//                    ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                            .setSenderId(Long.toString(userLoginInfo2.getUserId()))
//                            .setMessageContent(ByteString.copyFrom(mes.getBytes()))
//                            .setGroupId("" + groupId)
//                            .setMessageType(2).build();
//                    channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//                    log.info("发送通过患者地址获得调度的10条pci医院信息初始化成功消息向：" + userSocketAddressString + "成功");
//                }
//            }
//        }
//        return Result.ok();
//    }
//
//    @Override
//    public Result ping(PingForm pingForm) {
//        ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
//        UserLoginInfo userLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("chat:userId:" + pingForm.getUserId()), UserLoginInfo.class);
////        CpcUserLoginInfo cpcUserLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("cpc:session"), CpcUserLoginInfo.class);
//        String userSocketAddressString = userLoginInfo.getAddress();
//        Channel channel = addressChannelConcurrentHashMap.get(userSocketAddressString);
//        log.info("已连接数量：" + addressChannelConcurrentHashMap.size());
//        if (channel == null) {
//            String mes = "disable";
//            log.info("向" + userSocketAddressString + "发送disable成功");
//            return Result.build(mes,ResultCodeEnum.FAIL);
//        } else {
//            String mes = "pong";
//            ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//            ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                    .setSenderId(pingForm.getUserId())
//                    .setMessageContent(ByteString.copyFrom(mes.getBytes()))
//                    .setGroupId("")
//                    .setMessageType(2).build();
//            channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//            log.info("http心跳向" + userSocketAddressString + "发送pong成功");
//            return Result.build(mes,ResultCodeEnum.SUCCESS);
//        }
//    }
//    @Override
//    public Result pingCPC(PingForm pingForm) {
//        ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
////        UserLoginInfo userLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("chat:userId:" + pingForm.getUserId()), UserLoginInfo.class);
//        CpcUserLoginInfo cpcUserLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("cpc:session"), CpcUserLoginInfo.class);
//        String userSocketAddressString = cpcUserLoginInfo.getAddress();
//        Channel channel = addressChannelConcurrentHashMap.get(userSocketAddressString);
//        log.info("已连接客户端数量：" + addressChannelConcurrentHashMap.size());
//        log.info("已连接任务数量："+taskchannl.size());
//        log.info("已连接总数量："+(addressChannelConcurrentHashMap.size()+taskchannl.size()));
//        if (channel == null) {
//            String mes = "disableCPC";
//            log.info("向" + userSocketAddressString + "发送disableCPC成功");
//            return Result.build(mes,ResultCodeEnum.FAIL);
//        } else {
//            String mes = "pongCPC";
//            ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//            ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                    .setSenderId(pingForm.getUserId())
//                    .setMessageContent(ByteString.copyFrom(mes.getBytes()))
//                    .setGroupId("")
//                    .setMessageType(11).build();
//            channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//            log.info("向" + userSocketAddressString + "发送pongCPC成功");
//            return Result.build(mes,ResultCodeEnum.SUCCESS);
//        }
//    }
//    @Override
//    public Result pingTask(TaskPingForm taskPingForm) {
//        if(taskPingForm.getTaskId()==null||taskPingForm.getUserId()==null){
//            throw new HuaTuoException(ResultCodeEnum.FAIL);
//        }
//        Channel channel = taskchannl.get(taskPingForm.getTaskId());
//        if (channel == null) {
//            String mes = "disableTask"+"%%%%%"+taskPingForm.getTaskId();
//            log.info("向"+taskPingForm.getTaskId()+"发送disableTask成功");
//            return Result.build(mes,ResultCodeEnum.FAIL);
//        } else {
//            String mes = "pongTask"+"%%%%%"+taskPingForm.getTaskId();
//            ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//            ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                    .setSenderId(taskPingForm.getUserId())
//                    .setMessageContent(ByteString.copyFrom(mes.getBytes()))
//                    .setGroupId("")
//                    .setMessageType(2).build();
//            channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//            log.info("向"+taskPingForm.getTaskId()+"发送pongTask成功");
//            return Result.build(mes,ResultCodeEnum.SUCCESS);
//        }
//    }
//    @Override
//    public Result hello(PingForm pingForm) {
//        ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
//        UserLoginInfo userLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("chat:userId:" + pingForm.getUserId()), UserLoginInfo.class);
////        CpcUserLoginInfo cpcUserLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("cpc:session"), CpcUserLoginInfo.class);
//        String userSocketAddressString =userLoginInfo.getAddress();
//        Channel channel = addressChannelConcurrentHashMap.get(userSocketAddressString);
//        if (channel == null) {
//            String mes = "hello_disable";
//            log.info("向" + userSocketAddressString + "发送hello_disable成功");
//            return Result.build(mes,ResultCodeEnum.FAIL);
//        } else {
//            String mes = "hello";
//            ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//            ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                    .setSenderId(pingForm.getUserId())
//                    .setMessageContent(ByteString.copyFrom(mes.getBytes()))
//                    .setGroupId("")
//                    .setMessageType(2).build();
//            channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//            return Result.build(mes,ResultCodeEnum.SUCCESS);
//        }
//    }
//    @Override
//    public Result helloCPC(PingForm pingForm) {
//        ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
////        UserLoginInfo userLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("chat:userId:" + pingForm.getUserId()), UserLoginInfo.class);
//        CpcUserLoginInfo cpcUserLoginInfo = objectMapper.convertValue(myRedisTemplate.opsForValue().get("cpc:session"), CpcUserLoginInfo.class);
//        String userSocketAddressString =cpcUserLoginInfo.getAddress();
//        Channel channel = addressChannelConcurrentHashMap.get(userSocketAddressString);
//        if (channel == null) {
//            String mes = "hello_disableCPC";
//            log.info("向" + userSocketAddressString + "发送hello_disableCPC成功");
//            return Result.build(mes,ResultCodeEnum.FAIL);
//        } else {
//            String mes = "helloCPC";
//            ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//            ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
//                    .setSenderId(pingForm.getUserId())
//                    .setMessageContent(ByteString.copyFrom(mes.getBytes()))
//                    .setGroupId("")
//                    .setMessageType(11).build();
//            channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//            log.info("向" + userSocketAddressString + "发送helloCPC成功");
//            return Result.build(mes,ResultCodeEnum.SUCCESS);
//        }
//    }
//
//    @Override
//    public Result helloTask(TaskPingForm taskPingForm) {
//        Channel channel = taskchannl.get(taskPingForm.getTaskId());
//        System.out.println(channel);
//        if (channel == null) {
//            String mes = "hello_disableTask%%%%%"+taskPingForm.getTaskId();
//            log.info("向"+taskPingForm.getTaskId()+"发送hello_disableTask成功");
//            return Result.build(mes,ResultCodeEnum.FAIL);
//        } else {
//            String mes = "helloTask%%%%%"+taskPingForm.getTaskId();
////            String mes = "419667400816861184"+taskPingForm.getTaskId();
//            ChatMessageProtocol.ChatMessageProtocolForm.Builder builder = ChatMessageProtocol.ChatMessageProtocolForm.newBuilder();
//            ChatMessageProtocol.ChatMessageProtocolForm chatMessageProtocolForm = builder.setSendTime(new Date().getTime())
////                    .setSenderId(taskPingForm.getTaskId())
//                    .setSenderId(taskPingForm.getUserId())
//                    .setMessageContent(ByteString.copyFrom(mes.getBytes()))
//                    .setGroupId("")
//                    .setMessageType(2).build();
//            channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(chatMessageProtocolForm.toByteArray())));
//            log.info("向"+taskPingForm.getTaskId()+"发送helloTask成功");
//            return Result.build(mes,ResultCodeEnum.SUCCESS);
//        }
//    }
//}
//
