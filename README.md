# 海康 NVR Spring Boot Demo

基于 **Spring Boot 2.1.0.RELEASE** + **Java 8** 的海康网络录像机集成示例，支持：

- **实时预览**：直接使用 NVR RTSP 流地址，前端播放器按 RTSP 模式播放
- **录像下载**：通过海康 Device Network SDK 按时间段下载多路摄像头录像到本地磁盘，并自动在需要时转为 MP4
- **录像录制会话**：支持开始/结束录制，支持多个不同组合并发录制，并使用唯一 sessionId 标识每个录制任务

## 环境要求

| 组件 | 版本/说明 |
|------|-----------|
| JDK | 1.8 |
| Maven | 3.6+ |
| FFmpeg | 仅用于可选的 MP4 转码，实时预览不再依赖 MJPEG 转码 |
| 海康 SDK | [Device Network SDK](https://www.hikvision.com/cn/support/download/sdk/) Windows 64-bit |

## API 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/status` | SDK / 登录状态 |
| GET | `/api/cameras` | 摄像头列表（优先从 NVR 获取，失败时回退本地配置） |
| GET | `/api/live/{cameraId}/rtsp` | 单路 RTSP 实时流地址 |
| GET | `/api/live/{cameraId}/mjpeg` | 兼容旧接口，实际返回 RTSP 地址 |
| POST | `/api/live/play` | **多路同时播放**（传入 cameraIds 列表，返回 RTSP 地址） |
| GET | `/api/live/play?cameraIds=cam-1,cam-2` | 多路播放（GET 方式） |
| POST | `/api/live/stop` | 批量停止指定通道 |
| POST | `/api/live/stop/all` | 停止全部播放 |
| GET | `/api/live/active` | 当前活跃推流列表 |
| POST | `/api/live/{cameraId}/stop` | 停止单路预览 |
| POST | `/api/recordings/start` | 开始一个录制会话，返回唯一 sessionId |
| POST | `/api/recordings/stop` | 结束录制会话，并按 NVR 实际时间段下载录像 |
| GET | `/api/recordings/active` | 查询所有正在进行的录制会话 |

### 实时播放示例

```json
POST /api/live/play
{
  "cameraIds": ["cam-1", "cam-2", "cam-3"],
  "substream": true
}
```

响应：

```json
{
  "success": true,
  "data": [
    {
      "cameraId": "cam-1",
      "cameraName": "大门",
      "channel": 1,
      "rtspUrl": "rtsp://admin:password@192.168.1.64:554/Streaming/Channels/10102",
      "streamUrl": "rtsp://admin:password@192.168.1.64:554/Streaming/Channels/10102"
    }
  ]
}
```

前端使用该 `rtspUrl`/`streamUrl` 直接交给支持 RTSP 的播放器即可。
