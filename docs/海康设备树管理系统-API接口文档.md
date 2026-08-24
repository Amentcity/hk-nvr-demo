# 海康 NVR 管理系统 API 接口文档

## 1. 项目说明

`hk-nvr-demo` 是基于 Spring Boot 2.1.0.RELEASE + Java 8 的海康 NVR 集成服务。

当前支持：

- 海康 Device Network SDK 接入
- NVR 登录与状态管理
- 摄像头列表查询
- RTSP 实时预览地址获取
- 多摄像头实时播放管理
- 录像会话管理
- Phase 2 实时预览链路扩展（Live Preview / Stream Test）

目标设备：

- 海康威视 DS-8864N-R16

---

# 2. 基础信息

Base URL:

```
http://{server}:{port}
```

默认端口：

```
8080
```

---

# 3. API接口

## 3.1 查询系统状态

### 请求

GET

```
/api/status
```

### 参数

无

### 响应

```json
{
  "success": true,
  "message": "SDK initialized"
}
```

---

# 3.2 获取摄像头列表

### 请求

GET

```
/api/cameras
```

### 参数

无

### 响应

```json
{
  "success": true,
  "data": [
    {
      "cameraId":"cam-1",
      "channel":1,
      "name":"camera"
    }
  ]
}
```

---

# 3.3 获取单路RTSP地址

### 请求

GET

```
/api/live/{cameraId}/rtsp
```

### Path参数

|参数|类型|说明|
|-|-|-|
|cameraId|string|摄像头ID|

### 响应

```json
{
 "cameraId":"cam-1",
 "rtspUrl":"rtsp://xxx/Streaming/Channels/101"
}
```

---

# 3.4 多路播放

### 请求

POST

```
/api/live/play
```

### 请求体

```json
{
 "cameraIds":["cam-1","cam-2"],
 "substream":true
}
```

### 参数

|参数|类型|说明|
|-|-|-|
|cameraIds|array|摄像机ID列表|
|substream|boolean|是否使用子码流|

### 响应

```json
{
 "success":true,
 "data":[]
}
```

---

# 3.5 停止播放

POST

```
/api/live/stop
```

请求体：

```json
{
 "cameraIds":["cam-1"]
}
```

---

# 3.6 查询活跃播放

GET

```
/api/live/active
```

---

# 3.7 开始实时预览 Session

POST

```
/api/live/start
```

请求参数：

```json
{
 "cameraId":"cam-1"
}
```

响应：

```json
{
 "sessionId":"live-xxx",
 "cameraId":"cam-1",
 "status":"RUNNING",
 "webrtcUrl":"webrtc://server/live/cam-1"
}
```

---

# 3.8 RTSP链路测试

POST

```
/api/test/stream/{cameraId}
```

Path参数：

|参数|类型|说明|
|-|-|-|
|cameraId|string|摄像机ID|

响应：

```json
{
 "sessionId":"test-cam-1",
 "cameraId":"cam-1",
 "rtspUrl":"rtsp://xxx",
 "status":"STARTED"
}
```

---

# 3.9 录像接口

## 开始录像

POST

```
/api/recordings/start
```

## 停止录像

POST

```
/api/recordings/stop
```

## 查询录像任务

GET

```
/api/recordings/active
```

---

# 4. 视频链路

```
DS-8864N-R16
      |
      v
HCNetSDK
      |
      v
RTSP
      |
      v
FFmpeg
      |
      v
Media Server
      |
      v
WebRTC / Player
```

---

# 5. 后续规划

Phase 3:

- Unity 数字孪生绑定
- Building/Floor/Room/Camera 映射
- WebRTC Texture显示
