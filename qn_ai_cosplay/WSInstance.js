import {
	ChatMessageProtocolForm
} from '@/proto.js';
import store from '@/store.js'; // 引入 Vuex store
// const store.state.MAX_RECONNECT_ATTEMPTS = 2; // 最大重连次数,设置为3次
let reconnectAttempts = 0; // 当前重连次数

class WebSocketManager {
	/* 单例模式确保全局只有一个websocket接收新任务通知 */
	constructor(url) {
	    if (!WebSocketManager.instance) {
	      this.url = url;
	      this.socket = null;
	      this.isConnected = false;
	      this.reconnectTimer = null;
	      this.heartbeatTimer = null;
	      WebSocketManager.instance = this;
	    }
	    return WebSocketManager.instance;
	}
	connect() {
		if (this.isWebSocketConnected()) {
		    console.log("WebSocket 已连接");
		    return;
		}
		this.socket = uni.connectSocket({
			url: this.url,
			success: (res) => {
				console.log("服务器同意连接请求", res);
			},
			fail: (err) => {
				console.error("服务器拒绝连接请求", err);
				uni.showToast({
					duration: 5000,
					icon: "error",
					title: "服务器拒绝连接..."
				})
			}
		});
		this.socket.onOpen((res) => {
		    console.log("WebSocket完全连接", res);
			/* 发送hello问候消息，确认是否真的连接成功了（connectSocket的success回调是在握手没结束的时候回调的） */
			this.sendHello(); 
		});
		// 监听 WebSocket 消息
		this.socket.onMessage((res) => {
			var binaryData = new Uint8Array(res.data); // 将 ArrayBuffer 转换为 Uint8Array
			var receivedMessage = ChatMessageProtocolForm.decode(binaryData); // receivedMessage为实际消息
			
			// console.log('接受消息', receivedMessage);
			var messageType = receivedMessage.msgType;
			var senderId = receivedMessage.senderId;
			var groupId = receivedMessage.unionId; // 使用unionId作为groupId
			var sendTime = receivedMessage.sendTime;
			
			var decoder = new TextDecoder();
			var message = decoder.decode(receivedMessage.messageContent);
			
			var newTask = {
				groupId: groupId,
				createrId: senderId,
				patientPositon: message
			};
			var that = this;
			if (messageType === 1) { // 新任务请求消息
				store.commit('addNewTask', newTask);
			} else if ([3, 4, 5].includes(messageType)) {
				var GetUserInfoForm = {
					userId: senderId
				};
			
				uni.request({
					url: store.state.HTTPS + '/api/common/wxapi/applet/getUserInfo',
					method: 'POST',
					data: GetUserInfoForm,
					success: function(res) {
						console.log(res);
						if (res.data.code == 200) {
							var newMessage = {
								id: sendTime,
								content: store.state.HTTPS +
									`/api/common/callchatmodule/file/${message}`,
								senderId: senderId,
								messageType: messageType,
								senderType: res.data.data.userType,
								senderName: res.data.data.userName,
							};
							console.log("接收消息", newMessage);
							store.commit('addChatMessage', {
								groupId: groupId,
								message: newMessage
							});
							if (senderId != store.state.userInfo.userId) {
								const text = `${res.data.data.userName}发来文件消息`;
								that.getPatientAddress(groupId, text);
								// store.commit('setNoticeMsg', `任务${groupId}-${res.data.data.userName}发来文件消息`);
							}
						}
					},
					fail: function(error) {
						console.error('消息内容用户信息请求失败:', error);
					}
				});
			
			} else if (messageType === 2) {
				var GetUserInfoForm = {
					userId: senderId
				};
			
				uni.request({
					url: store.state.HTTPS + '/api/common/wxapi/applet/getUserInfo',
					method: 'POST',
					data: GetUserInfoForm,
					success: function(res) {
						if (res.data.code == 200) {
							var newMessage = {
								id: sendTime,
								content: message,
								senderId: senderId,
								messageType: messageType,
								senderType: res.data.data.userType,
								senderName: res.data.data.userName,
							};
							console.log("接收消息", newMessage);
							store.commit('addChatMessage', {
								groupId: groupId,
								message: newMessage
							});
							if (senderId != store.state.userInfo.userId) {
								if (newMessage.content !== "pong") {
									const text = `${res.data.data.userName}发来消息:${message}`;
									that.getPatientAddress(groupId, text);
									// store.commit('setNoticeMsg', `任务${groupId}-${res.data.data.userName}发来消息:${message}`);
								}
							}
						}
					},
					fail: function(error) {
						console.error('消息内容用户信息请求失败:', error);
					}
				});
			}
		});
		// 监听 WebSocket 关闭事件
		this.socket.onClose((res) => {
			console.error("WebSocket 关闭:", res);
			if(store.state.close === false){ // 如果用户上主动关闭的，就禁止用户重连
				this.reconnectWebSocket();
			}
		});
		// 监听 WebSocket 错误事件
		this.socket.onError((error) => {
			console.error("WebSocket 错误:", error);
			this.reconnectWebSocket();
		});
	}
	
	sendSocketMessage(binaryData, successCallback, failCallback){
		this.socket.send({
		    data: binaryData,
		    success: () => {
		        if (successCallback && typeof successCallback === 'function') {
		            successCallback();
		        }
		    },
		    fail: (err) => {
		        if (failCallback && typeof failCallback === 'function') {
		            failCallback(err);
		        }
		    },
		});
	}
	closeWebSocket() {
		store.commit('setClose'); // 标记为主动关闭
		if (this.socket) {
			this.socket.close();
			this.socket = null;
		}
		this.isConnected = false;
		// 清除重连定时器
		if (this.reconnectTimer) {
			clearTimeout(this.reconnectTimer);
			this.reconnectTimer = null;
		}
		// 清除心跳定时器
		if (this.heartbeatTimer) {
			clearInterval(this.heartbeatTimer);
			this.heartbeatTimer = null;
		}
	}
	
	reconnectWebSocket() {
		if (reconnectAttempts >= store.state.MAX_RECONNECT_ATTEMPTS) {
			console.log("已达到最大重连次数，停止重连");
			uni.showToast({
				duration: 5000,
				icon: "error",
				title: "连接失败，请检查网络"
			});
			return;
		}
		
		console.log(`第 ${reconnectAttempts + 1} 次重连...`);
		reconnectAttempts++;
		
		this.reconnectTimer = setTimeout(() => {
			this.connect();
		}, store.state.RECONNECT_GAP_TIME);
	}
	
	isWebSocketConnected() {
		return this.socket && this.isConnected;
	}
	
	sendHello() {
		const helloMessage = ChatMessageProtocolForm.createGreetingMessage(
			store.state.userInfo.userId,
			'server'
		);
		const binaryData = ChatMessageProtocolForm.encode(helloMessage);
		
		this.sendSocketMessage(binaryData, () => {
			console.log("Hello消息发送成功");
			this.isConnected = true;
			reconnectAttempts = 0; // 重置重连次数
			store.commit('resetClose'); // 重置关闭标志
			store.commit('setIsLogin'); // 设置登录状态
			this.startPingTimer(); // 开始心跳
		}, (err) => {
			console.error("Hello消息发送失败:", err);
		});
	}
	
	sendPing() {
		if (!this.isConnected) return;
		
		const pingMessage = ChatMessageProtocolForm.createHeartbeatMessage(
			store.state.userInfo.userId,
			'server'
		);
		const binaryData = ChatMessageProtocolForm.encode(pingMessage);
		
		this.sendSocketMessage(binaryData, () => {
			console.log("Ping消息发送成功");
		}, (err) => {
			console.error("Ping消息发送失败:", err);
			store.commit('incrementPingMissing');
		});
	}
	// 启动Ping定时器
	startPingTimer() {
		console.log("开启ping定时器");
		// var isLogin = store.state.isLogin;
		clearTimeout(this.heartbeatTimer);
		this.heartbeatTimer = setInterval(() => {
		    if (this.isConnected) {
				this.sendPing();
		    }
		}, store.state.PING_GAP_TIME); // 每 10 秒发送一次心跳包
	}
}
// 确保WebSocketManager 是单例模式
console.log("WebSocketManager", store.state.WSS + "/server");
const wsInstance = new WebSocketManager(store.state.WSS + "/server");
export default wsInstance;