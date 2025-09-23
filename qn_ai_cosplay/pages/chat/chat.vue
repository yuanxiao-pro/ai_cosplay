<template>
  <view class="chat-container">
    <!-- 标题栏 -->
    <view class="chat-header">
      <text class="header-title">语音聊天</text>
    </view>
    
    <!-- 聊天消息列表 -->
    <scroll-view 
      class="chat-messages" 
      scroll-y 
      :scroll-top="scrollTop"
      scroll-with-animation
      @scroll="onScroll"
    >
      <view 
        v-for="(message, index) in messages" 
        :key="index" 
        class="message-item"
        :class="message.isSelf ? 'message-self' : 'message-other'"
      >
        <!-- 头像 -->
        <view 
          class="avatar" 
          :class="message.isSelf ? 'avatar-self' : 'avatar-other'"
        ></view>
        
        <!-- 消息内容 -->
        <view class="message-content">
          <!-- 文本消息 -->
          <view 
            v-if="message.type === 'text'" 
            class="message-bubble text-message"
            :class="message.isSelf ? 'bubble-self' : 'bubble-other'"
          >
            {{ message.content }}
          </view>
          
          <!-- 语音消息 -->
          <view 
            v-else-if="message.type === 'voice'" 
            class="message-bubble voice-message"
            :class="message.isSelf ? 'bubble-self' : 'bubble-other'"
            @tap="playVoice(message)"
          >
            <view class="voice-content">
              <view class="voice-icon">
                <text class="voice-wave" :class="{ 'playing': message.isPlaying }">🎵</text>
              </view>
              <text class="voice-duration">{{ message.duration }}″</text>
            </view>
          </view>
          
          <!-- 时间戳 -->
          <text class="message-time">{{ message.time }}</text>
        </view>
      </view>
      
      <!-- 占位元素，确保最新消息可见 -->
      <view id="bottom-anchor" class="bottom-anchor"></view>
    </scroll-view>
    
    <!-- 输入区域 -->
    <view class="input-area">
      <!-- 文本输入模式 -->
      <view v-if="inputMode === 'text'" class="text-input-container">
        <input 
          v-model="textInput" 
          class="text-input" 
          placeholder="输入消息..."
          @confirm="sendTextMessage"
          maxlength="500"
        />
        <button 
          class="send-btn" 
          :disabled="!textInput.trim()"
          @tap="sendTextMessage"
        >
          发送
        </button>
        <button class="switch-btn" @tap="switchToVoiceMode">
          🎤
        </button>
      </view>
      
      <!-- 语音输入模式 -->
      <view v-else class="voice-input-container">
        <button class="switch-btn" @tap="switchToTextMode">
          💬
        </button>
        
        <view class="voice-record-area">
          <!-- 录音按钮 -->
          <view 
            class="record-btn"
            :class="{ 'recording': isRecording }"
            @touchstart="startRecording"
            @touchend="stopRecording"
            @touchcancel="cancelRecording"
          >
            <text class="record-text">
              {{ isRecording ? `录音中... ${recordTime}s` : '按住说话' }}
            </text>
          </view>
          
          <!-- 录音进度条 -->
          <view v-if="isRecording" class="record-progress">
            <view class="progress-bar">
              <view 
                class="progress-fill" 
                :style="{ width: (recordTime / 60) * 100 + '%' }"
              ></view>
            </view>
            <text class="progress-text">{{ recordTime }}/60s</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 录音提示 -->
    <view v-if="isRecording" class="recording-tip">
      <text>{{ recordTime >= 60 ? '录音时间已达上限' : '松开发送，上滑取消' }}</text>
    </view>
    
    <!-- 跳转到底部按钮 -->
    <view 
      v-if="showScrollToBottom" 
      class="scroll-to-bottom-btn"
      @tap="scrollToBottom"
    >
      <text class="scroll-icon">⬇</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'ChatPage',
  data() {
    return {
      // 聊天消息
      messages: [
        {
          type: 'text',
          content: '你好！这是一条文本消息',
          isSelf: false,
          time: '14:30',
          id: 1
        },
        {
          type: 'voice',
          content: 'voice_url_1.mp3', // 语音文件URL
          duration: 3,
          isSelf: true,
          time: '14:31',
          id: 2,
          isPlaying: false
        },
        {
          type: 'text',
          content: '这是我发送的消息',
          isSelf: true,
          time: '14:32',
          id: 3
        },
        {
          type: 'text',
          content: '这是更多的测试消息1',
          isSelf: false,
          time: '14:33',
          id: 4
        },
        {
          type: 'voice',
          content: 'voice_url_2.mp3',
          duration: 5,
          isSelf: true,
          time: '14:34',
          id: 5,
          isPlaying: false
        },
        {
          type: 'text',
          content: '这是更多的测试消息2',
          isSelf: false,
          time: '14:35',
          id: 6
        },
        {
          type: 'text',
          content: '这是更多的测试消息3',
          isSelf: true,
          time: '14:36',
          id: 7
        },
        {
          type: 'text',
          content: '这是最新的消息',
          isSelf: false,
          time: '14:37',
          id: 8
        }
      ],
      
      // 输入相关
      inputMode: 'text', // 'text' 或 'voice'
      textInput: '',
      
      // 录音相关
      isRecording: false,
      recordTime: 0,
      recordTimer: null,
      recorderManager: null,
      
      // 播放相关
      audioContext: null,
      currentPlayingId: null,
      
      // 滚动相关
      scrollTop: 0,
      showScrollToBottom: false
    }
  },
  
  onLoad() {
    this.initAudio();
    this.scrollToBottom();
  },
  
  onUnload() {
    this.cleanupAudio();
  },
  
  methods: {
    // 初始化音频相关
    initAudio() {
      // 初始化录音管理器
      this.recorderManager = uni.getRecorderManager();
      
      this.recorderManager.onStart(() => {
        console.log('录音开始');
      });
      
      this.recorderManager.onStop((res) => {
        console.log('录音结束', res);
        this.handleRecordingStop(res);
      });
      
      this.recorderManager.onError((err) => {
        console.error('录音错误', err);
        this.isRecording = false;
        this.clearRecordTimer();
        uni.showToast({
          title: '录音失败',
          icon: 'none'
        });
      });
      
      // 初始化音频播放
      this.audioContext = uni.createInnerAudioContext();
      
      this.audioContext.onEnded(() => {
        this.stopAllVoicePlaying();
      });
      
      this.audioContext.onError((err) => {
        console.error('音频播放错误', err);
        this.stopAllVoicePlaying();
      });
    },
    
    // 清理音频资源
    cleanupAudio() {
      if (this.audioContext) {
        this.audioContext.destroy();
      }
      this.clearRecordTimer();
    },
    
    // 切换到文本输入模式
    switchToTextMode() {
      this.inputMode = 'text';
    },
    
    // 切换到语音输入模式
    switchToVoiceMode() {
      this.inputMode = 'voice';
    },
    
    // 发送文本消息
    sendTextMessage() {
      if (!this.textInput.trim()) return;
      
      const message = {
        type: 'text',
        content: this.textInput.trim(),
        isSelf: true,
        time: this.getCurrentTime(),
        id: Date.now()
      };
      
      this.messages.push(message);
      this.textInput = '';
      this.scrollToBottom();
    },
    
    // 开始录音
    startRecording() {
      if (this.isRecording) return;
      
      this.isRecording = true;
      this.recordTime = 0;
      
      // 开始录音
      this.recorderManager.start({
        duration: 60000, // 最长60秒
        sampleRate: 16000,
        numberOfChannels: 1,
        encodeBitRate: 96000,
        format: 'mp3'
      });
      
      // 开始计时
      this.recordTimer = setInterval(() => {
        this.recordTime++;
        if (this.recordTime >= 60) {
          this.stopRecording();
        }
      }, 1000);
    },
    
    // 停止录音
    stopRecording() {
      if (!this.isRecording) return;
      
      if (this.recordTime < 1) {
        // 录音时间太短
        this.cancelRecording();
        uni.showToast({
          title: '录音时间太短',
          icon: 'none'
        });
        return;
      }
      
      this.recorderManager.stop();
    },
    
    // 取消录音
    cancelRecording() {
      this.isRecording = false;
      this.clearRecordTimer();
      
      if (this.recorderManager) {
        this.recorderManager.stop();
      }
    },
    
    // 处理录音结束
    handleRecordingStop(res) {
      this.isRecording = false;
      this.clearRecordTimer();
      
      if (res.tempFilePath && this.recordTime >= 1) {
        // 创建语音消息
        const message = {
          type: 'voice',
          content: res.tempFilePath,
          duration: this.recordTime,
          isSelf: true,
          time: this.getCurrentTime(),
          id: Date.now(),
          isPlaying: false
        };
        
        this.messages.push(message);
        this.scrollToBottom();
      }
    },
    
    // 清除录音计时器
    clearRecordTimer() {
      if (this.recordTimer) {
        clearInterval(this.recordTimer);
        this.recordTimer = null;
      }
      this.recordTime = 0;
    },
    
    // 播放语音
    playVoice(message) {
      if (message.isPlaying) {
        // 如果正在播放，则停止
        this.stopAllVoicePlaying();
        return;
      }
      
      // 停止其他语音播放
      this.stopAllVoicePlaying();
      
      // 开始播放当前语音
      message.isPlaying = true;
      this.currentPlayingId = message.id;
      
      this.audioContext.src = message.content;
      this.audioContext.play();
    },
    
    // 停止所有语音播放
    stopAllVoicePlaying() {
      this.messages.forEach(msg => {
        if (msg.type === 'voice') {
          msg.isPlaying = false;
        }
      });
      
      if (this.audioContext) {
        this.audioContext.stop();
      }
      
      this.currentPlayingId = null;
    },
    
    // 获取当前时间
    getCurrentTime() {
      const now = new Date();
      const hours = now.getHours().toString().padStart(2, '0');
      const minutes = now.getMinutes().toString().padStart(2, '0');
      return `${hours}:${minutes}`;
    },
    
    // 滚动监听
    onScroll(e) {
      const { scrollTop, scrollHeight, clientHeight } = e.detail;
      // 判断是否滚动到接近底部（100px内）
      const isNearBottom = scrollHeight - scrollTop - clientHeight < 100;
      this.showScrollToBottom = !isNearBottom && this.messages.length > 3;
    },
    
    // 滚动到底部
    scrollToBottom() {
      this.$nextTick(() => {
        this.scrollTop = 999999;
        this.showScrollToBottom = false;
      });
    }
  }
}
</script>

<style scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

/* 标题栏 */
.chat-header {
  height: 44px;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e5e5e5;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

/* 聊天消息区域 */
.chat-messages {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
}

.message-self {
  flex-direction: row-reverse;
}

.message-other {
  flex-direction: row;
}

/* 头像 */
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  margin: 0 10px;
  flex-shrink: 0;
}

.avatar-self {
  background-color: #4CAF50; /* 绿色 */
}

.avatar-other {
  background-color: #333; /* 黑色 */
}

/* 消息内容 */
.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-self .message-content {
  align-items: flex-end;
}

.message-other .message-content {
  align-items: flex-start;
}

/* 消息气泡 */
.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  margin-bottom: 5px;
  word-wrap: break-word;
}

.bubble-self {
  background-color: #4CAF50;
  color: white;
}

.bubble-other {
  background-color: white;
  color: #333;
  border: 1px solid #e5e5e5;
}

/* 文本消息 */
.text-message {
  font-size: 16px;
  line-height: 1.4;
}

/* 语音消息 */
.voice-message {
  min-width: 100px;
  cursor: pointer;
}

.voice-content {
  display: flex;
  align-items: center;
}

.voice-icon {
  margin-right: 8px;
}

.voice-wave {
  font-size: 18px;
}

.voice-wave.playing {
  animation: wave 1s infinite;
}

@keyframes wave {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.voice-duration {
  font-size: 14px;
  opacity: 0.8;
}

/* 时间戳 */
.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

/* 底部占位 */
.bottom-anchor {
  height: 1px;
}

/* 输入区域 */
.input-area {
  background-color: white;
  border-top: 1px solid #e5e5e5;
  padding: 10px;
}

/* 文本输入 */
.text-input-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.text-input {
  flex: 1;
  height: 40px;
  background-color: #f8f8f8;
  border: 1px solid #ddd;
  border-radius: 20px;
  padding: 0 15px;
  font-size: 16px;
}

.send-btn {
  height: 40px;
  padding: 0 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
}

.send-btn:disabled {
  background-color: #ccc;
}

.switch-btn {
  width: 40px;
  height: 40px;
  background-color: #f0f0f0;
  border: none;
  border-radius: 20px;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 语音输入 */
.voice-input-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.voice-record-area {
  flex: 1;
}

.record-btn {
  width: 100%;
  height: 50px;
  background-color: #f8f8f8;
  border: 2px solid #ddd;
  border-radius: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
}

.record-btn.recording {
  background-color: #ffebee;
  border-color: #f44336;
}

.record-text {
  font-size: 16px;
  color: #333;
}

.record-btn.recording .record-text {
  color: #f44336;
}

/* 录音进度 */
.record-progress {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-bar {
  flex: 1;
  height: 4px;
  background-color: #e0e0e0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #f44336;
  transition: width 0.1s;
}

.progress-text {
  font-size: 12px;
  color: #666;
  min-width: 40px;
}

/* 录音提示 */
.recording-tip {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(0,0,0,0.8);
  color: white;
  padding: 10px 20px;
  border-radius: 20px;
  font-size: 14px;
  z-index: 1000;
}

/* 跳转到底部按钮 */
.scroll-to-bottom-btn {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 50px;
  height: 50px;
  background-color: #4CAF50;
  border-radius: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  cursor: pointer;
  z-index: 999;
  opacity: 0.9;
  animation: fadeIn 0.3s ease-in-out;
}

.scroll-to-bottom-btn:active {
  opacity: 0.7;
  transform: scale(0.95);
}

.scroll-icon {
  color: white;
  font-size: 20px;
  font-weight: bold;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 0.9;
    transform: scale(1);
  }
}
</style>