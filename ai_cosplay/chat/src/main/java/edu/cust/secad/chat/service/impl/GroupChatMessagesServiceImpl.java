package edu.cust.secad.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cust.secad.chat.component.CacheCleaner;
import edu.cust.secad.chat.mapper.GroupChatMessagesMapper;
import edu.cust.secad.chat.mapper.GroupMembersMapper;
import edu.cust.secad.chat.proto.AiChatMessageProtocol;
import edu.cust.secad.chat.service.GroupChatMessagesService;
import edu.cust.secad.model.base.HuaTuoException;
import edu.cust.secad.model.base.ResultCodeEnum;
import edu.cust.secad.model.chat.GroupChatMessages;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassDescription:
 * @Author: Nvgu
 * @Created: 2024/10/14 16:07
 * @Updated: 2024/10/14 16:07
 */

@Slf4j
@Service
@Transactional//事务
public class GroupChatMessagesServiceImpl extends ServiceImpl<GroupChatMessagesMapper, GroupChatMessages> implements GroupChatMessagesService {

    @Autowired
    private GroupChatMessagesMapper groupChatMessagesMapper;
//    @Autowired
//    private TaskInfoMapper taskInfoMapper;
//    @Resource
//    private ThreadPoolTaskExecutor mesProcessSaveThreadPoolTaskExecutor;

    @Autowired
    private KafkaTemplate<String, Object> myKafkaTemplate;
    @Autowired
    private ConcurrentHashMap<String,Channel> taskchannl;
    @Autowired
    private ConcurrentHashMap<Long, String> groupTopicConcurrentHashMap;

    @Autowired
    private GroupMembersMapper groupMembersMapper;

//    @Autowired
//    private UserLoginInfoMapper userLoginInfoMapper;

    @Autowired
    @Qualifier("addressChannelConcurrentHashMap")
    private ConcurrentHashMap<String, Channel> addressChannelConcurrentHashMap;

    @Qualifier("myRedisTemplate")
    @Autowired
    private RedisTemplate<String, Object> myRedisTemplate;

    @Autowired
    private CacheCleaner cacheCleaner; // 注入CacheCleaner

//    @Autowired
//    private ConcurrentHashMap<String, byte[][]> uuidBigFileContentArrayContentConcurrentHashMap;

    @Value("${file.basePath}")
    private String fileBasePath;



    @Override
    public void dealWithMes(AiChatMessageProtocol.ChatMessageProtocolForm mes, Channel channel) {
        int messageType = mes.getMsgType();
        String senderId = mes.getSenderId();
        String groupId = null;
        String uuidString = null;
        String indexString = null;
        System.out.println("执行了存储");
        //判断文件是什么类型的
        log.info("这个mes类型为：" + messageType);
        if (messageType == 6) {
            // 这个消息是通知后端的
            try {
                byte[] contentByteArray = mes.getMessageContent().toByteArray();
                String numString = new String(contentByteArray , "UTF-8");
                int num = Integer.parseInt(numString);
//                String tmp = mes.getGroupId();
                String tmp = mes.getUnionId();
                String[] split = tmp.split("#");
                groupId = split[0];
                uuidString = split[1];
                indexString = split[2];
                log.info("消息内容6：接下来有{}个分散文件传过来，针对的groupId为：{} ,文件临时名字为：{}，文件序号为:{}", num, groupId, uuidString, indexString);
                // 初始化临时存储map
                byte[][] bytes = new byte[num][];
                cacheCleaner.put(uuidString , bytes);
//                uuidBigFileContentArrayContentConcurrentHashMap.put(uuidString, bytes);
                channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(mes.toByteArray())));
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new HuaTuoException(ResultCodeEnum.DATA_ERROR);
            }
            return;
        }
        if (messageType == 3 || messageType == 4 || messageType == 5) {
            // 这是传输过来的大文件数据
//            String tmp = mes.getGroupId();
            String tmp = mes.getUnionId();
            String[] split = tmp.split("#");
            groupId = split[0];
            if (split.length == 1) {
                //这是完整大文件
                saveMes(mes.getMessageContent().toByteArray(), messageType, groupId, senderId, new Date(mes.getSendTime()));
                return;
            } else {
                //这是碎片大文件
                uuidString = split[1];
                indexString = split[2];
                byte[][] bytess = cacheCleaner.get(uuidString);
//                byte[][] bytess = uuidBigFileContentArrayContentConcurrentHashMap.get(uuidString);
                int index = Integer.parseInt(indexString);
                bytess[index - 1] = mes.getMessageContent().toByteArray();
                //判断有没有接受完了
                if (areAllAssigned(bytess)) {
                    //接受完了
                    //拼接字节数组
                    //保存消息
                    saveMes(mergeByteArrays(bytess), messageType, groupId, senderId, new Date(mes.getSendTime()));
//                    uuidBigFileContentArrayContentConcurrentHashMap.remove(uuidString);
                    cacheCleaner.delete(uuidString);
                } else {
                    //还没有接受完
                }
                return;
            }
        }
//        saveMes(mes.getMessageContent().toByteArray(), messageType, mes.getGroupId(), senderId, new Date(mes.getSendTime()));
        saveMes(mes.getMessageContent().toByteArray(), messageType, mes.getUnionId(), senderId, new Date(mes.getSendTime()));
    }

    // 合并二维字节数组的所有一维数组
    private byte[] mergeByteArrays(byte[][] byteArrays) {
        // 计算合并后字节数组的总长度
        int totalLength = 0;
        for (byte[] array : byteArrays) {
            totalLength += array.length;
        }

        // 创建一个新的字节数组来存放合并后的数据
        byte[] mergedArray = new byte[totalLength];
        int currentPosition = 0;

        // 将每个一维字节数组复制到新的字节数组中
        for (byte[] array : byteArrays) {
            System.arraycopy(array, 0, mergedArray, currentPosition, array.length);
            currentPosition += array.length;
        }

        return mergedArray;
    }


    private void saveMes(byte[] mesContent, int mesType, String groupId, String senderId, Date time) {

        GroupChatMessages groupChatMessages = new GroupChatMessages();
        log.info("开始保存聊天数据：senderId:" + senderId + "  groupId:" + groupId);

        // 1/请求拉群人员名单  2/文本  3/图片  4/语音  5/视频  6/通知后端有大文件  7/通知前端
        // 将图片、语音、视频存放到本地，返回访问地址
        String fileAccessUrl = null;
        if (mesType == 3) { // 图片
            fileAccessUrl = saveFileToLocal(mesContent, "images", senderId, groupId);
        } else if (mesType == 4) { // 语音
            fileAccessUrl = saveFileToLocal(mesContent, "audio", senderId, groupId);
        } else if (mesType == 5) { // 视频
            fileAccessUrl = saveFileToLocal(mesContent, "video", senderId, groupId);
        }

        // 存放到数据库中
        groupChatMessages.setGroupId(Long.parseLong(groupId));
        groupChatMessages.setMessage(mesContent);
        groupChatMessages.setMessageType(mesType);
        groupChatMessages.setSenderId(Long.parseLong(senderId));
        groupChatMessages.setSendAt(time);
        groupChatMessages.setCreatedUsrId(1L);
        groupChatMessages.setModifiedUsrId(1L);
        if(!StringUtils.hasText(fileAccessUrl)){
            groupChatMessages.setFilePath("无地址");
        }else {
            groupChatMessages.setFilePath(fileAccessUrl);
        }
        groupChatMessagesMapper.insert(groupChatMessages);

        if (fileAccessUrl != null) {
            //如果是图片，语音，视频的文件，将消息内容替换为本地存储的内容的地址
            groupChatMessages.setMessage(fileAccessUrl.getBytes());
        }else {
            groupChatMessages.setFilePath("无地址");
        }

        String topicId = groupTopicConcurrentHashMap.get(Long.parseLong(groupId));
        log.info("groupTopicConcurrentHashMap中存在数据量:" + groupTopicConcurrentHashMap.size());
        log.info("当前群里的topicId:" + topicId);

        if (!StringUtils.hasText(topicId)) {
            throw new HuaTuoException(ResultCodeEnum.MIS_TOPICID);
        }
        // 投递进Kafka
        myKafkaTemplate.send(topicId, groupChatMessages);
    }


    // 保存文件到本地，并返回文件访问路径
    private String saveFileToLocal(byte[] content, String folderName, String senderId, String groupId) {
        try {
            String basePath = fileBasePath + "uploads" + "/" + groupId + "/" + folderName + "/";
            File directory = new File(basePath);
            if (!directory.exists()) {
                directory.mkdirs(); // 确保目录存在
            }

            // 生成文件名，基于群聊id和发送者 ID 和时间戳来生成唯一文件名
            String fileName = senderId  + System.currentTimeMillis() + "." + getFileExtension(folderName);
            File file = new File(directory, fileName);

            // 将文件内容写入本地
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content);
            }

            // 返回文件访问的 URL
            return (basePath + fileName).substring(fileBasePath.length()).replaceAll("/", "&");

        } catch (Exception e) {
            log.error("Failed to save file: ", e);
            throw new RuntimeException("Failed to save file", e);
        }
    }

    private static boolean isArrayFull(int[][] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j] == 0) {  // 判断是否存在0，表示未赋值
                    return false;  // 如果发现未赋值的位置，返回false
                }
            }
        }
        return true;  // 所有位置都已赋值
    }

    // 获取文件扩展名，根据文件类型决定
    private String getFileExtension(String folderName) {
        switch (folderName) {
            case "images":
                return "jpg"; // 默认扩展名
            case "audio":
                return "mp3"; // 默认扩展名
            case "video":
                return "mp4"; // 默认扩展名
            default:
                return "bin"; // 默认扩展名
        }
    }

    // 判断二维数组的每个位置是否都已被赋值
    private boolean areAllAssigned(byte[][] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {  // 如果某个位置为 null，表示未赋值
                return false;
            }
        }
        return true;  // 如果所有位置都已赋值，返回 true
    }

    @Override
    public void sendMes(Object mes) {
        /*
        * 把消息发给群聊里的其他人
        * */

    }

}
