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
			var messageType = receivedMessage.messageType;
			var senderId = receivedMessage.senderId;
			var groupId = receivedMessage.groupId;
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
		if (this.isWebSocketConnected()) {
			// isConnecting = false;
			uni.closeSocket({
				success: () => {
					this.socket = null;
					console.log("socket 置为空");
					// 关闭定时器
					clearTimeout(this.heartbeatTimer);
					this.heartbeatTimer = null;
					console.log("关闭定时器 heartbeatTimer");
					clearTimeout(this.reconnectTimer);
					this.reconnectTimer = null;
					console.log("关闭定时器 reconnectTimer");
					this.isConnected = false;
					reconnectAttempts = 0;
					// store.commit("setIsLogout"); // 设置为登出，方便小程序切屏和切回时判断是否需要重连ws（App.vue）
					uni.reLaunch({ //跳转到主页，并携带账号参数
						url: '/components/login/login'
					})
					console.log("WebSocket 连接已被客户端主动关闭");
				},
				fail: (err) => {
					console.error("关闭 WebSocket 失败", err);
				}
			});
		} else {
			clearTimeout(this.heartbeatTimer);
			this.heartbeatTimer = null;
			console.log("关闭定时器 heartbeatTimer", this.heartbeatTimer);
			clearTimeout(this.reconnectTimer);
			this.reconnectTimer = null;
			console.log("关闭定时器 reconnectTimer", this.reconnectTimer);
			this.isConnected = false;
			reconnectAttempts = 0;
			console.log("WebSocket已关闭！");
			// store.commit("setIsLogout"); // 设置为登出，方便小程序切屏和切回时判断是否需要重连ws（App.vue）
			uni.reLaunch({ //跳转到主页，并携带账号参数
				// url: '/components/login/login',
				url: '/pages/login/login'
			})
		}
	}
	reconnectWebSocket() {
		console.log("重连前检查WS是否已连接", this.isWebSocketConnected());
		if(this.isWebSocketConnected() === true) {
			console.log("WS已连接,不允许重连");
			return;
		}
		console.log("检查可否重连", reconnectAttempts, store.state.MAX_RECONNECT_ATTEMPTS, this.isWebSocketConnected());
		if(reconnectAttempts > store.state.MAX_RECONNECT_ATTEMPTS && this.isWebSocketConnected() === false){
		// if(reconnectAttempts >= store.state.MAX_RECONNECT_ATTEMPTS){
			console.log("重连次数耗尽", reconnectAttempts, store.state.MAX_RECONNECT_ATTEMPTS);
			uni.showToast({
				duration: 5000,
				title: "重新连接失败，请重新登录",
				icon: 'error'
			})
			this.closeWebSocket();
			return
		}
		reconnectAttempts++;
		console.log("开始第" + reconnectAttempts + "次重连");
		uni.showToast({
			duration: 3000,
			title: "第" + reconnectAttempts + "次尝试重连",
			icon: "loading"
		})
		console.log("第" + reconnectAttempts + "次清除重连定时器");
		clearTimeout(this.reconnectTimer);
		
		this.reconnectTimer  = setTimeout(() => {
			console.log("进入第" + reconnectAttempts + "次清除重连定时器");
			this.connect();
			this.relogin();
		}, store.state.RECONNECT_GAP_TIME * reconnectAttempts);
	}
	isWebSocketConnected() {
		if (this.socket != null && this.socket.readyState === 1) {
			return true; // readyState === 1 表示 WebSocket 已连接
		} else {
			return false;
		}
	}
	
	sendHello() {
		console.log("store.state.userInfo.userId", store.state.userInfo.userId);
		var hello_content = ChatMessageProtocolForm.create({
			senderId: store.state.userInfo.userId,
			// groupId: 0,
			messageType: store.state.MSGTYPE_HELLO,
			// messageContent: new Uint8Array(store.state.HELLO),
			messageContent: new TextEncoder().encode(store.state.HELLO), // 二进制表示
			sendTime: Date.now(),
		});
		
		var binaryData = ChatMessageProtocolForm.encode(hello_content).finish();
		binaryData = binaryData.slice().buffer;
		this.sendSocketMessage(binaryData,
			() => {
				console.log('消息发送成功');
				// store.commit("setIsHello");
				store.commit("resetClose"); // 连接成功，说明设置ws为”非主动关闭“
				store.commit('setIsLogin'); // setIsLogin统一在发送hello消息成功后执行，防止出现登陆成功但是websocket连接失败的情况
				this.isConnected = true;
				// 启动定时器，定时向服务器发送ping
				this.startPingTimer()
				store.dispatch('updateUserLocation'); // 执行更新位置
				const pages = getCurrentPages();
				const currentPage = pages[pages.length - 1].route;
				console.log(currentPage);
				if(currentPage === 'pages/login/login' || currentPage === 'pages/sign/sign'){ // 跳转到小程序主页
					uni.reLaunch({
						url: '/pages/index/index'
					})
				}
			},
			(err) => {
				console.error('CPC主页问候消息发送失败', err);
				// 退回到登录页面，不允许进入应用
				uni.showToast({
					duration: 5000,
					title: "确认连接失败，请返回重新登录",
					icon: 'error'
				})
				this.closeWebSocket();
		});
	}
	sendPing() {
		console.log("store.state.userInfo.userId", store.state.userInfo.userId);
		console.log(store.state.PING+"%%%%%MP%%%%%"+store.state.userInfo.userId);
		var ping_content = ChatMessageProtocolForm.create({
			senderId: store.state.userInfo.userId,
			// groupId: 0,
			messageType: store.state.MSGTYPE_PING,
			// messageContent: new Uint8Array(store.state.PING+"%%%%%MP%%%%%"+store.state.userInfo.userId),
			messageContent: new TextEncoder().encode(store.state.PING+"%%%%%MP%%%%%"+store.state.userInfo.userId), // 二进制表示
			sendTime: Date.now(),
		});
		
		var binaryData = ChatMessageProtocolForm.encode(ping_content).finish();
		binaryData = binaryData.slice().buffer;
		if(this.isWebSocketConnected()){
			this.sendSocketMessage(binaryData,
				() => {
					console.log('Ping消息发送成功');
				},
				(err) => {
					console.error('CPC主页Ping消息发送失败', err);
					// /* 等待onClose触发后再重连 */
					
					/* 说明channel断了，直接执行重连 */
					this.reconnectWebSocket(); // reconnectWebSocket会重连3次，都失败的话就回到登陆页面
			});
		} else {
			this.reconnectWebSocket(); // reconnectWebSocket会重连3次，都失败的话就回到登陆页面
		}
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