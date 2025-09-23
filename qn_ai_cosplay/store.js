import {
	createStore
} from 'vuex';
import {
	AMapWX
} from './amap-wx.130.js';
import {
	reconnectWebSocket,
	isWebSocketConnected,
	initWebSocket,
	closeWebSocket
} from './websocket.js';
import createPersistedState from 'vuex-persistedstate';
import storageAdapter from './storageAdapter'; // 引入自定义适配器
import SparkMD5 from 'spark-md5'; // hash工具
// import storages from 'mp-storage';
const store = createStore({
	state: {
		/* websocket相关的常量 */
		MAX_RECONNECT_ATTEMPTS : 2, //最大重连次数,设置为3次
		RECONNECT_GAP_TIME : 2000,
		MSGTYPE_HELLO : 11,
		MSGTYPE_PING : 12,
		HELLO : "hello",
		PING : "ping",
		PING_GAP_TIME: 10000,
		
		HTTPS: 'https://www.rtxgzngl.cn:',
		WSS: 'wss://www.rtxgzngl.cn:',
		CITY: '',
		cpcUsrContact: '',
		//调起摄像头或相册的标志位，用于防止调起摄像头或图片时触发App.vue的onHide事件，避免退出登录
		isOpeningCameraOrAlbum: false,
		pingTimer: null,
		newTasks: [], // 当前接收到的任务数组
		currentTask: null, // 当前弹窗中显示的任务
		showTaskDialog: false, // 是否显示任务弹窗

		// chatMessages 结构改为数组
		chatMessages: [],
		userInfo: {
			userName: null,
			userType: null,
			userId: null,
			userCode: null
		},
		isHello: false,
		ping_missing: 0,
		isLogin: false, // 判断用户是否登录
		close: false, // 判断websocket是否为用户主动关闭的(在微信小程序上就是“用户是否切屏”)
		groups: [], // 聊天室 ID 列表
		mapShow: [], // 是否有位置变动提示
		mapPosition: [], // 聊天室对应的经纬度数据

		fileIds: [], // 待确认文件
		sureFiles: [], // 确认通过的文件

		noticeMsg: '', //最新notice信息
		isConfirm: true // 判断是否确认notice
	},
	// plugins: [createPersistedState()],
	plugins: [
	    createPersistedState({
	      storage: {
	        getItem: (key) => storageAdapter.getItem(key),
	        setItem: (key, value) => storageAdapter.setItem(key, value),
	        removeItem: (key) => storageAdapter.removeItem(key),
	      },
	    }),
	],
	mutations: {
		setCPCContact(state, contact){
			state.cpcUsrContact = contact;
		},
		setHTTPS(state, url) {
			state.HTTPS = url;
			console.log(state.HTTPS);
		},
		setWSS(state, url) {
			state.WSS = url;
			console.log(state.WSS);
		},
		setCity(state, city) {
			state.CITY = city;
		},
		prependChatMessages(state, {
			groupId,
			messages
		}) {
			// 查找对应的 group
			const group = state.chatMessages.find((item) => item.groupId === groupId);

			if (group) {
				// 如果 group 存在，将新消息添加到对应的 messages 数组的开头
				group.messages = [...messages, ...group.messages];
			} else {
				// 如果 group 不存在，创建新的 group 并添加消息
				state.chatMessages.push({
					groupId,
					messages: [...messages]
				});
			}
		},
		
		// 新增：更改消息
		commitChatMessages(state, {
			groupId,
			messages
		}) {
			// 查找对应的 group
			const group = state.chatMessages.find((item) => item.groupId === groupId);
		
			if (group) {
				// 如果 group 存在，替换为到对应的 messages 数组
				group.messages = [...messages];
			} else {
				// 如果 group 不存在，创建新的 group 并添加消息
				state.chatMessages.push({
					groupId,
					messages: [...messages]
				});
			}
		},
		
		addFileId(state, groupId) {
			if (!state.fileIds.includes(groupId)) {
				state.fileIds.push(groupId);
			}
		},
		removeFileId(state, groupId) {
			state.fileIds = state.fileIds.filter((id) => id !== groupId);
		},
		addSureFile(state, groupId) {
			if (!state.sureFiles.includes(groupId)) {
				state.sureFiles.push(groupId);
			}
		},
		removeSureFile(state, groupId) {
			state.SureFile = state.SureFile.filter((id) => id !== groupId);
		},

		setIsLogin(state) {
			state.isLogin = true;
		},
		setIsLogout(state) {
			state.isLogin = false;
		},
		resetPingMissing(state) {
			state.ping_missing = 0;
		},
		resetIsHello(state) {
			state.isHello = false;
		},
		setIsOpeningCameraOrAlbum(state){
			state.isOpeningCameraOrAlbum = true;
		},
		resetIsOpeningCameraOrAlbum(state){
			state.isOpeningCameraOrAlbum = false;
		},
		setNoticeMsg(state, msg) {
			state.noticeMsg = msg;
			state.isConfirm = false; // 设置notice
		},

		beConfirmed(state) {
			state.isConfirm = true; // 确认消息
		},

		addNewTask(state, task) {
			console.log("addNewTask");
			/* 丢弃因数据转发软件的UDP协议导致的重复任务 */
			// 先对传入的任务进行hash
			const task_str = JSON.stringify(task, Object.keys(task).sort());
			const task_hash = SparkMD5.hash(task_str);
			console.log(task_hash);
			const hasTask = state.newTasks.some(item => SparkMD5.hash(JSON.stringify(item, Object.keys(item).sort())) === task_hash);
			if(hasTask === true) console.log("有重复任务");
			if(hasTask === false){
				state.newTasks.push(task);
				state.currentTask = task;
				if (!state.showTaskDialog) {
					// 如果弹窗未显示，立即设置任务并显示弹窗
					state.showTaskDialog = true;
				}
			} else {
				console.log("任务列表中已存在该任务！");
			}
			// state.newTasks.push(task);
			// state.currentTask = task;
			// if (!state.showTaskDialog) {
			// 	// 如果弹窗未显示，立即设置任务并显示弹窗
			// 	state.showTaskDialog = true;
			// }
		},
		removeCurrentTask(state) {
			// 移除当前任务
			state.newTasks = state.newTasks.filter(task => task !== state.currentTask);

			// 设置下一个任务为当前任务（如果存在）
			if (state.newTasks.length > 0) {
				state.currentTask = state.newTasks[0];
				state.showTaskDialog = true;
			} else {
				state.showTaskDialog = false; // 没有任务时关闭弹窗
			}
		},

		// 根据 groupId 添加消息到对应的组
		addChatMessage(state, {
			groupId,
			message
		}) {
			// 查找对应的 group
			const group = state.chatMessages.find((item) => item.groupId === groupId);

			if (group) {
				// 如果 group 存在，添加消息到对应的 messages 数组
				group.messages.push(message);
			} else {
				// 如果 group 不存在，创建新的 group 并添加消息
				state.chatMessages.push({
					groupId,
					messages: [message]
				});
			}
		},

		setUserName(state, name) {
			state.userInfo.userName = name;
		},
		setUserId(state, id) {
			state.userInfo.userId = id;
		},
		setUserType(state, type) {
			state.userInfo.userType = type;
		},
		setUserCode(state, code) {
			state.userInfo.userCode = code;
		},

		setGroups(state, groups) {
			state.groups = groups;
		},
		setClose(state) {
			state.close = true;
		},
		resetClose(state) {
			state.close = false;
		},
		updateMapShow(state, {
			groupId,
			isChange = false
		}) {
			const group = state.mapShow.find((item) => item.groupId === groupId);
			if (group) {
				group.isChange = isChange;
			} else {
				state.mapShow.push({
					groupId,
					isChange
				});
			}
		},
		updateMapPosition(state, {
			groupId,
			latitude,
			longitude
		}) {
			const index = state.mapPosition.findIndex((item) => item.groupId === groupId);
			if (index !== -1) {
				state.mapPosition.splice(index, 1, {
					groupId,
					latitude,
					longitude
				});
			} else {
				state.mapPosition.push({
					groupId,
					latitude,
					longitude
				});
			}
		},
		setPingTimer(state, timer) {
			state.pingTimer = timer;
		},
		clearPingTimer(state) {
			if (state.pingTimer) {
				clearInterval(state.pingTimer);
				state.pingTimer = null;
			}
		}
	},

	getters: {
		getUserName: (state) => state.userInfo.userName,
		getUserId: (state) => state.userInfo.userId,
		getUserType: (state) => state.userInfo.userType,
		getUserCode: (state) => state.userInfo.userCode,

		// 根据 groupId 获取对应的消息列表
		getChatMessagesByGroupId: (state) => (groupId) => {
			const group = state.chatMessages.find((item) => item.groupId === groupId);
			return group ? group.messages : [];
		},
	},

	actions: {
		async fetchGroups({
			commit,
			state
		}) {
			const userId = state.userInfo.userId;
			try {
				const res = await uni.request({
					url: store.state.HTTPS +
						`/api/common/callchatmodule/getTaskListByUserId?userId=${userId}`, // 获取群聊列表接口
					method: 'GET',
					data: {
						userId: state.userInfo.userId
					},
				});
				if (res.data.code == 200) {
					commit('setGroups', res.data.data);
				}
			} catch (error) {
				console.error('获取群组列表失败:', error);
			}
		},

		async updateUserLocation({
			commit,
			state
		}) {
			try {
				const amapFun = new AMapWX({
					key: '59eab083d3c10c8a57f91a99567bd8d0'
				});
				amapFun.getRegeo({
					success: async (data) => {
						const latitude = data[0].latitude;
						const longitude = data[0].longitude;

						try {
							const app = getApp();
							const res = await uni.request({
								url: store.state.HTTPS +
									'/api/common/ambulances-gps-info/updateAmbulancesGpsInfo', //发送120定位接口
								method: 'POST',
								dataType: 'json',
								data: {
									user_id: state.userInfo.userId,
									longitude: longitude,
									latitude: latitude
								},
								header: {
									'Content-Type': 'application/x-www-form-urlencoded'
								}
							});
							if (res.data.code == 200) {
								console.log('发送120定位成功');
							}
						} catch (error) {
							console.log(error);
						}
					},
					fail: (info) => {
						console.error('获取定位失败:', info);
					},
				});
			} catch (error) {
				console.error('定位更新失败:', error);
			}
		},

		async fetchGroupLocation({
			commit,
			state
		}, groupId) {
			try {
				const app = getApp();
				const res = await uni.request({
					url: store.state.HTTPS +
						'/api/common/ambulances-gps-info/getAmbulanceGpsByGroupId', // 获取120定位接口
					method: 'POST',
					data: {
						group_id: groupId
					},
					header: {
						'Content-Type': 'application/x-www-form-urlencoded'
					},
					dataType: 'json'
				});
				if (res.data.code == 200) {
					const latitude = res.data.data.latitude;
					const longitude = res.data.data.longitude;
					if (latitude != null && longitude != null) {
						commit('updateMapPosition', {
							groupId,
							latitude,
							longitude
						});
					}
				} else {
					commit('updateMapShow', {
						groupId,
						isChange: true
					});
				}
			} catch (error) {
				commit('updateMapShow', {
					groupId,
					isChange: true
				});
				console.error(`获取群组 ${groupId} 位置失败:`, error);
			}
		},

		async periodicUpdate({
			state,
			dispatch
		}) {
			const userType = state.userInfo.userType;
			if (state.groups.length > 0) {
				for (const groupId of state.groups) {
					await dispatch('fetchGroupLocation', groupId);
				}
			}
		},

		// async sendPing({
		// 	commit,
		// 	state
		// }) {
		// 	try {
		// 		const app = getApp();
		// 		uni.request({
		// 			url: store.state.HTTPS + '/api/common/callchatmodule/ping',
		// 			method: 'post', // 根据接口要求选择请求方式
		// 			dataType: 'json',
		// 			data: {
		// 				userId: store.state.userInfo.userId,
		// 				ping: "ping",
		// 			},
		// 			success: (res) => {
		// 				if (res.data.code === 201 && res.data.data === "disable") {
		// 					store.state.ping_missing++;
		// 					console.log("success store监控：当前缺失心跳数", store.state.ping_missing);
		// 				}
		// 			},
		// 			fail: (err) => {
		// 				store.state.ping_missing++;
		// 				console.log("fail store监控：当前缺失心跳数", store.state.ping_missing);
		// 				uni.showToast({
		// 					duration: 2000,
		// 					title: "网络不稳定",
		// 					icon: "none"
		// 				})
		// 			}
		// 		});
		// 	} catch (error) {
		// 		console.error(error);
		// 	}
		// },
		
		// sendHello({
		// 	commit,
		// 	state
		// }) {
		// 	const app = getApp();
		// 	return new Promise((resolve, reject) => {
		// 		uni.request({
		// 			url: store.state.HTTPS + '/api/common/callchatmodule/hello',
		// 			method: 'post', // 根据接口要求选择请求方式
		// 			dataType: 'json',
		// 			data: {
		// 				userId: store.state.userInfo.userId,
		// 				ping: "hello",
		// 			},
		// 			success: (res) => {
		// 				if (res.data.code === 201 && res.data.data === "hello_disable") {
		// 					store.state.isHello = false;
		// 					if (isWebSocketConnected() === false) {
		// 						uni.showToast({
		// 							duration: 5000,
		// 							title: "确认连接失败，请返回重新登录",
		// 							icon: 'error'
		// 						})
		// 						closeWebSocket();
		// 						store.commit("resetIsHello")
		// 						store.commit("resetPingMissing")
		// 						// store.commit("setIsLogout");
		// 						uni.reLaunch({ //跳转到主页，并携带账号参数
		// 							url: '/pages/login/login'
		// 						})
		// 					}
		// 				} else {
		// 					store.state.isHello = true;
		// 					store.commit("resetPingMissing")
		// 				}
		// 				resolve(res); // 请求成功，调用 resolve
		// 			},
		// 			fail: (err) => {
		// 				store.state.isHello = false;
		// 				if (isWebSocketConnected() === false) {
		// 					uni.showToast({
		// 						duration: 5000,
		// 						title: "确认连接失败，请返回重新登录",
		// 						icon: 'error'
		// 					})
		// 					closeWebSocket();
		// 					store.commit("resetIsHello")
		// 					store.commit("resetPingMissing")
		// 					store.commit("setIsLogout")
		// 					uni.reLaunch({ //跳转到主页，并携带账号参数
		// 						url: '/pages/login/login'
		// 					})
		// 				}
		// 				console.log("请求失败");
		// 				reject(err); // 请求失败，调用 reject
		// 			}
		// 		});
		// 	});
		// },

		// // 启动Ping定时器
		// async startPingTimer({
		// 	commit
		// }) {
		// 	console.log("开启ping定时器");
		// 	const timer = setInterval(() => {
		// 		if (store.state.isLogin) {
		// 			// console.log("watch 用户已登录");
		// 			// console.log("发送ping请求");
		// 			store.dispatch('sendPing'); // 执行更新位置
		// 			if (store.state.ping_missing == 5 || store.state.ping_missing == 10) {
		// 				uni.showToast({
		// 					duration: 5000,
		// 					title: "网络不稳定",
		// 					icon: 'none'
		// 				})
		// 			}
		// 			if (store.state.ping_missing >= 11 && store.state.isLogin === true) {
		// 				uni.showToast({
		// 					duration: 5000,
		// 					title: "连接失败，请重新登录",
		// 					icon: 'error'
		// 				})
		// 				closeWebSocket();
		// 				store.commit("resetPingMissing")
		// 				store.commit("setIsLogout");
		// 				uni.reLaunch({
		// 					url: '/pages/login/login'
		// 				})
		// 			}
		// 		} else {
		// 			console.log("watch 用户未登录");
		// 		}
		// 	}, 10000);
		// 	store.commit('setPingTimer', timer); // 将定时器引用存储到 Vuex
		// }
	},
})
export default store

store.watch(
	(state) => state.isLogin,
	async (isLogin) => {
		if (isLogin == true) {
			setInterval(async () => {
				await store.dispatch('fetchGroups'); // 获取群聊列表
				await store.dispatch('periodicUpdate'); // 更新地图信息
			}, 180000); // 每隔 180 秒执行一次
		}
	}
);

store.watch(
	(state) => ({
		isLogin: state.isLogin,
		userType: state.userInfo.userType
	}), // 监听 isLogin 和 userType
	async ({
		isLogin,
		userType
	}) => {
		if (isLogin && userType === 1) {
			setInterval(async () => {
				await store.dispatch('updateUserLocation'); // 执行更新位置
			}, 180000); // 每隔 180 秒执行一次
		}
	}
);

// store.watch(
// 	(state) => ({
// 		isLogin: state.isLogin,
// 		ping_missing: state.ping_missing,
// 	}),
// 	async ({
// 		isLogin,
// 		ping_missing
// 	}) => {
// 		if (isLogin) {
// 			console.log("watch 用户已登录");
// 			if (state.pingTimer) {
// 				clearInterval(state.pingTimer);
// 			}
// 			state.pingTimer = setInterval(async () => {
// 				await store.dispatch('sendPing'); // 执行更新位置
// 				if (ping_missing == 5 || ping_missing == 10) {
// 					uni.showToast({
// 						duration: 5000,
// 						title: "网络不稳定",
// 						icon: 'none'
// 					})
// 				}
// 				if (ping_missing >= 11 && store.state.isLogin === true) {
// 					uni.showToast({
// 						duration: 5000,
// 						title: "连接失败，请重新登录",
// 						icon: 'error'
// 					})
// 					closeWebSocket();
// 					store.commit("resetPingMissing")
// 					store.commit("setIsLogout");
// 					uni.reLaunch({
// 						url: '/pages/login/login'
// 					})
// 				}
// 			}, 5000);
// 		} else {
// 			console.log("watch 用户未登录");
// 		}
// 	}
// );