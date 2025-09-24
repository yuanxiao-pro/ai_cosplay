import { ChatMessageProtocolForm } from './ai_chat_message_protocol.js';

// 消息类型常量
export const MESSAGE_TYPES = {
  TEXT: 0,      // 文本消息
  AUDIO: 1,     // mp3音频
  HEARTBEAT: 2, // 心跳
  GREETING: 3   // 问候
};

/**
 * 聊天消息协议工具类
 */
export class ChatProtocol {
  /**
   * 创建聊天消息
   * @param {Object} options - 消息选项
   * @param {string} options.lastUid - 上一条消息的unionId
   * @param {string} options.sendTime - 发送时间（时间戳）
   * @param {string} options.pkgId - 自增id
   * @param {string} options.senderId - 发送者id
   * @param {string} options.rcvId - 接收者id
   * @param {string} options.unionId - 配对id
   * @param {number} options.len - 消息长度
   * @param {number} options.msgType - 消息类型 (0-文本/1-mp3/2-心跳/3-问候)
   * @param {string|Uint8Array} options.messageContent - 消息内容
   * @returns {Object} ChatMessageProtocolForm实例
   */
  static createMessage(options = {}) {
    const {
      lastUid = '',
      sendTime = Date.now().toString(),
      pkgId = '',
      senderId = '',
      rcvId = '',
      unionId = '',
      len = 0,
      msgType = MESSAGE_TYPES.TEXT,
      messageContent = ''
    } = options;

    // 处理消息内容
    let content;
    if (typeof messageContent === 'string') {
      // 字符串转为Uint8Array
      content = new TextEncoder().encode(messageContent);
    } else if (messageContent instanceof Uint8Array) {
      content = messageContent;
    } else {
      content = new Uint8Array();
    }

    return ChatMessageProtocolForm.create({
      lastUid,
      sendTime,
      pkgId,
      senderId,
      rcvId,
      unionId,
      len: len || content.length,
      msgType,
      messageContent: content
    });
  }

  /**
   * 创建文本消息
   * @param {string} text - 文本内容
   * @param {string} senderId - 发送者ID
   * @param {string} rcvId - 接收者ID
   * @param {string} unionId - 配对ID
   * @returns {Object} 消息实例
   */
  static createTextMessage(text, senderId, rcvId, unionId) {
    return this.createMessage({
      senderId,
      rcvId,
      unionId,
      msgType: MESSAGE_TYPES.TEXT,
      messageContent: text
    });
  }

  /**
   * 创建心跳消息
   * @param {string} senderId - 发送者ID
   * @param {string} rcvId - 接收者ID
   * @returns {Object} 心跳消息实例
   */
  static createHeartbeatMessage(senderId, rcvId) {
    return this.createMessage({
      senderId,
      rcvId,
      msgType: MESSAGE_TYPES.HEARTBEAT,
      messageContent: 'ping'
    });
  }

  /**
   * 创建问候消息
   * @param {string} senderId - 发送者ID
   * @param {string} rcvId - 接收者ID
   * @returns {Object} 问候消息实例
   */
  static createGreetingMessage(senderId, rcvId) {
    return this.createMessage({
      senderId,
      rcvId,
      msgType: MESSAGE_TYPES.GREETING,
      messageContent: 'hello'
    });
  }

  /**
   * 序列化消息为二进制数据
   * @param {Object} message - 消息实例
   * @returns {Uint8Array} 序列化后的二进制数据
   */
  static encode(message) {
    return ChatMessageProtocolForm.encode(message).finish();
  }

  /**
   * 从二进制数据反序列化消息
   * @param {Uint8Array} buffer - 二进制数据
   * @returns {Object} 反序列化后的消息对象
   */
  static decode(buffer) {
    return ChatMessageProtocolForm.decode(buffer);
  }

  /**
   * 验证消息格式
   * @param {Object} message - 消息对象
   * @returns {string|null} 验证结果，null表示验证通过
   */
  static verify(message) {
    return ChatMessageProtocolForm.verify(message);
  }

  /**
   * 将消息转换为普通对象
   * @param {Object} message - 消息实例
   * @returns {Object} 普通JavaScript对象
   */
  static toObject(message) {
    return ChatMessageProtocolForm.toObject(message, {
      // 将bytes字段转换为base64字符串便于传输
      bytes: String,
      // 包含默认值
      defaults: true
    });
  }

  /**
   * 从普通对象创建消息实例
   * @param {Object} object - 普通JavaScript对象
   * @returns {Object} 消息实例
   */
  static fromObject(object) {
    return ChatMessageProtocolForm.fromObject(object);
  }

  /**
   * 将消息转换为JSON
   * @param {Object} message - 消息实例
   * @returns {Object} JSON对象
   */
  static toJSON(message) {
    return message.toJSON();
  }

  /**
   * 获取消息内容的文本形式
   * @param {Object} message - 消息实例
   * @returns {string} 文本内容
   */
  static getMessageText(message) {
    if (!message.messageContent) return '';
    
    try {
      // 如果是Uint8Array，转换为字符串
      if (message.messageContent instanceof Uint8Array) {
        return new TextDecoder().decode(message.messageContent);
      }
      // 如果已经是字符串，直接返回
      return message.messageContent.toString();
    } catch (error) {
      console.error('解析消息内容失败:', error);
      return '';
    }
  }

  /**
   * 生成唯一ID
   * @returns {string} 唯一ID
   */
  static generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }
}

// 默认导出
export default ChatProtocol; 