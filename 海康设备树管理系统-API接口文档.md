# 海康威视 NVR 管理系统 API 接口文档

版本：main 分支同步版

## 1. 项目说明

本项目基于：

- Spring Boot 2.1.0.RELEASE
- Java 8
- 海康威视 HCNetSDK Java JNA
- Hikvision NVR（目标设备：DS-8864N-R16）

实现能力：

- NVR设备登录/注销
- 设备状态查询
- 设备树管理
- 摄像机通道查询
- RTSP实时预览
- 多路视频播放管理
- 录像会话管理
- 为后续 Unity 数字孪生提供摄像机数据接口

---

# 2. 项目结构

```
com.example.hknvr

├── controller
│   ├── DeviceController
│   ├── CameraController
│   ├── LivePreviewController
│   └── StreamTestController
│
├── service
│   ├── device
│   ├── camera
│   └── stream
│
├── sdk
├── dto
├── vo
├── model
└── config
```

---

# 3. 接口列表

|请求方式|路径|说明|
|-|-|-|
|GET|/api/status|查询SDK状态|
|GET|/api/cameras|查询摄像机列表|
|GET|/api/live/{cameraId}/rtsp|获取RTSP地址|
|GET|/api/live/{cameraId}/mjpeg|兼容旧预览接口|
|POST|/api/live/play|多路播放|
|GET|/api/live/play|GET方式多路播放|
|POST|/api/live/stop|批量停止播放|
|POST|/api/live/stop/all|停止全部播放|
|GET|/api/live/active|查询活动播放|
|POST|/api/live/{cameraId}/stop|停止单路播放|
|POST|/api/recordings/start|开始录像|
|POST|/api/recordings/stop|停止录像|
|GET|/api/recordings/active|查询录像任务|
|POST|/api/device/login|登录设备|
|POST|/api/device/logout|注销设备|
|GET|/api/device/tree/{userId}|获取设备树|
|GET|/api/device/channels/{userId}|获取通道|
|GET|/api/device/info/{userId}|获取设备信息|
|POST|/api/test/stream/{cameraId}|RTSP测试|

---

# 4. 摄像机接口

## 4.1 查询摄像机列表

### 请求

```
GET /api/cameras
```

### 请求参数

无

### 响应体

```json
[
 {
  "cameraId":"cam-1",
  "cameraName":"大门",
  "channel":1,
  "online":true
 }
]
```

---

# 5. 实时播放接口

## 5.1 多路播放

### 请求

```
POST /api/live/play
Content-Type: application/json
```

### 请求参数

```json
{
 "cameraIds":["cam-1","cam-2"],
 "substream":true
}
```

参数说明：

|参数|类型|说明|
|-|-|-|
|cameraIds|Array|摄像机ID列表|
|substream|Boolean|是否使用子码流|

### 响应

```json
{
 "success":true,
 "data":[
  {
   "cameraId":"cam-1",
   "channel":1,
   "rtspUrl":"rtsp://xxx",
   "streamUrl":"rtsp://xxx"
  }
 ]
}
```

---

## 5.2 获取单路RTSP

### 请求

```
GET /api/live/{cameraId}/rtsp
```

### Path参数

|参数|类型|说明|
|-|-|-|
|cameraId|String|摄像机ID|

### 响应

```json
{
 "cameraId":"cam-1",
 "rtspUrl":"rtsp://admin:password@ip:554/Streaming/Channels/101"
}
```

---

# 6. Phase 2 实时预览接口

## 6.1 启动实时会话

请求：

```
POST /api/live/start
```

参数：

```json
{
 "cameraId":"DS8864_CH33"
}
```

响应：

```json
{
 "sessionId":"live-xxx",
 "cameraId":"DS8864_CH33",
 "rtspUrl":"rtsp://xxx",
 "streamId":"DS8864_CH33",
 "webrtcUrl":"webrtc://server/live/xxx",
 "status":"RUNNING"
}
```

---

## 6.2 停止实时会话

请求：

```
DELETE /api/live/{sessionId}
```

响应：

```json
true
```

---

# 7. RTSP测试接口

## 请求

```
POST /api/test/stream/{cameraId}
```

用途：

验证：

```
DS-8864N-R16
 ↓
RTSP
 ↓
FFmpeg
```

### Path参数

|参数|类型|说明|
|-|-|-|
|cameraId|String|摄像机ID|

### 响应

```json
{
 "sessionId":"test-cam-1",
 "cameraId":"cam-1",
 "rtspUrl":"rtsp://xxx",
 "status":"STARTED"
}
```

---

# 8. 设备管理接口

## 登录设备

```
POST /api/device/login
```

请求：

```json
{
 "ip":"192.168.1.100",
 "port":8000,
 "username":"admin",
 "password":"******"
}
```

响应：

```json
{
 "code":200,
 "message":"success",
 "data":1
}
```

---

## 获取设备树

```
GET /api/device/tree/{userId}
```

响应包含：

- NVR信息
- 模拟通道
- IP通道
- 零通道

---

# 9. 统一响应格式

```json
{
 "code":200,
 "message":"success",
 "data":{}
}
```

状态码：

|code|说明|
|-|-|
|200|成功|
|400|参数错误|
|404|资源不存在|
|500|服务器异常|

---

# 10. 当前开发状态

## 已完成

- HCNetSDK设备接入
- 设备树管理
- 摄像机管理
- RTSP实时预览接口
- 多路播放接口
- 录像会话接口
- Phase 2视频链路框架

## 后续

- ZLMediaKit WebRTC正式接入
- Unity数字孪生Camera绑定
- 建筑/楼层/房间与摄像机映射
