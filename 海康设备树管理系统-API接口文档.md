
# 海康威视 HCNetSDK 设备树管理系统 — API 接口文档

## 项目概述

本项目基于海康威视 HCNetSDK Java 版本（JNA 调用），实现设备树管理功能，包括设备登录、注销、获取设备树（通道列表）等核心操作。项目采用 Controller / Service / SDK 三层架构，DTO/VO 分离请求与响应数据。

---

## 文件结构

```
com.hikvision.demo
├── controller/
│   └── DeviceController.java          # REST 接口层（4 个接口）
├── service/
│   └── DeviceTreeService.java         # 业务逻辑层
├── sdk/
│   ├── HCNetSDK.java                  # 海康 SDK 原生方法/结构体/常量声明
│   └── DeviceTreeManager.java         # SDK 封装层（设备树核心逻辑）
├── dto/
│   ├── LoginRequest.java              # 登录请求 DTO
│   └── DeviceConfigRequest.java       # 配置请求 DTO
├── vo/
│   ├── DeviceTreeVO.java              # 设备树响应 VO
│   ├── DeviceInfoVO.java              # 设备信息响应 VO
│   └── ChannelVO.java                 # 通道信息响应 VO
└── common/
    └── Result.java                    # 统一响应包装类
```

---

## 接口列表

### 1. 登录设备

**POST** `/api/device/login`

登录海康设备，建立 SDK 会话。

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ip | String | 是 | 设备 IP 地址 |
| port | int | 否 | 设备端口，默认 8000 |
| username | String | 是 | 登录用户名 |
| password | String | 是 | 登录密码 |

**请求示例：**

```json
{
  "ip": "192.168.1.100",
  "port": 8000,
  "username": "admin",
  "password": "admin123"
}
```

**响应示例（成功）：**

```json
{
  "code": 200,
  "message": "success",
  "data": 1
}
```

**响应示例（失败）：**

```json
{
  "code": 500,
  "message": "设备登录失败，请检查设备地址和账号密码",
  "data": null
}
```

---

### 2. 注销设备

**POST** `/api/device/logout`

注销已登录的设备会话。

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | int | 是 | 登录 ID（登录接口返回） |

**请求示例：**

```json
{
  "userId": 1
}
```

**响应示例（成功）：**

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 3. 获取设备树

**GET** `/api/device/tree/{userId}`

获取设备的完整树形结构，包含所有通道信息（模拟通道 + IP 通道 + 零通道）。

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | int | 是 | 登录 ID（路径参数） |

**响应示例（成功）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "deviceName": "DS-8600N-I8",
    "deviceType": "NVR",
    "serialNumber": "DS-8600N-I820000000001",
    "analogChannelCount": 16,
    "ipChannelCount": 32,
    "startDChan": 33,
    "zeroChanCount": 1,
    "audioChanCount": 0,
    "channels": [
      {
        "channelNo": 1,
        "channelName": "Camera1",
        "enabled": true,
        "type": "ANALOG",
        "ipAddress": "",
        "port": 0
      },
      {
        "channelNo": 2,
        "channelName": "Camera2",
        "enabled": true,
        "type": "ANALOG",
        "ipAddress": "",
        "port": 0
      },
      {
        "channelNo": 33,
        "channelName": "IPCamera33",
        "enabled": true,
        "type": "IP",
        "ipAddress": "192.168.1.201",
        "port": 8000
      },
      {
        "channelNo": 0,
        "channelName": "ZeroChan0",
        "enabled": true,
        "type": "ZERO",
        "ipAddress": "",
        "port": 0
      }
    ]
  }
}
```

---

### 4. 获取通道列表

**GET** `/api/device/channels/{userId}`

仅获取通道列表（不含设备元信息），适合需要轻量响应的场景。

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | int | 是 | 登录 ID（路径参数） |

**响应示例（成功）：**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "channelNo": 1,
      "channelName": "Camera1",
      "enabled": true,
      "type": "ANALOG",
      "ipAddress": "",
      "port": 0
    },
    {
      "channelNo": 33,
      "channelName": "IPCamera33",
      "enabled": true,
      "type": "IP",
      "ipAddress": "192.168.1.201",
      "port": 8000
    }
  ]
}
```

---

### 5. 获取设备信息

**GET** `/api/device/info/{userId}`

获取设备的详细配置信息。

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | int | 是 | 登录 ID（路径参数） |

**响应示例（成功）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "ipAddress": "192.168.1.100",
    "port": 8000,
    "serialNumber": "DS-8600N-I820000000001",
    "deviceName": "DS-8600N-I8",
    "deviceType": 5,
    "analogChanNum": 16,
    "ipChanNum": 32,
    "startChan": 1,
    "startDChan": 33,
    "zeroChanNum": 1,
    "audioChanNum": 0
  }
}
```

---

## 统一响应格式

所有接口均使用 `Result<T>` 统一包装：

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码（200=成功，400=参数错误，404=会话不存在，500=服务端错误） |
| message | String | 响应描述 |
| data | T | 响应数据（成功时返回，失败时为 null） |
| errors | List\<String\> | 错误详情列表（可选） |

---

## 通道类型枚举

| 值 | 说明 |
|----|------|
| ANALOG | 模拟通道 |
| IP | IP 通道（网络摄像机） |
| ZERO | 零通道（智能分析合成通道） |
| AUDIO | 语音通道 |
| EXTERNAL | 扩展通道 |

---

## 前端调用示例

### JavaScript (Fetch API)

```javascript
// 1. 登录设备
const loginRes = await fetch('/api/device/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    ip: '192.168.1.100',
    port: 8000,
    username: 'admin',
    password: 'admin123'
  })
});
const { data: userId } = await loginRes.json();

// 2. 获取设备树
const treeRes = await fetch(`/api/device/tree/${userId}`);
const treeData = await treeRes.json();
console.log('设备名称:', treeData.data.deviceName);
console.log('通道数:', treeData.data.channels.length);

// 3. 获取通道列表
const chanRes = await fetch(`/api/device/channels/${userId}`);
const channels = await chanRes.json();

// 4. 注销设备
await fetch('/api/device/logout', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ userId })
});
```

### Axios

```javascript
// 登录
const { data } = await axios.post('/api/device/login', {
  ip: '192.168.1.100',
  port: 8000,
  username: 'admin',
  password: 'admin123'
});
const userId = data.data;

// 获取设备树
const { data: tree } = await axios.get(`/api/device/tree/${userId}`);
```

---

## 架构说明

### 分层设计

| 层级 | 类名 | 职责 |
|------|------|------|
| Controller | DeviceController | 接收 HTTP 请求，参数校验，调用 Service，返回 Result 包装 |
| Service | DeviceTreeService | 业务逻辑编排，SDK 调用封装，DTO→VO 转换 |
| SDK | DeviceTreeManager | 直接操作 HCNetSDK 结构体和方法，管理设备会话生命周期 |

### 数据流向

```
前端请求 → DeviceController → DeviceTreeService → DeviceTreeManager → HCNetSDK (JNA)
     ↑                                                              |
     └──────────── Result<VO> ←───────────── VO ←───────────────────┘
```

### 关键设计决策

1. **不添加新的实体类**：SDK 层（DeviceTreeManager）直接使用 HCNetSDK 已有的结构体（NET_DVR_DEVICEINFO_V40、NET_DVR_IPPARACFG_V40 等），避免重复定义
2. **DTO/VO 分离**：请求参数使用 DTO（LoginRequest），响应数据使用 VO（DeviceTreeVO），不直接暴露内部结构体给前端
3. **统一响应包装**：所有接口返回 Result<T>，前端统一判断 code == 200 即可
4. **会话管理**：Service 层维护 userId → DeviceSession 的映射，支持多设备并发登录
5. **V40 结构体**：IP 通道配置使用 NET_DVR_IPPARACFG_V40，通过 struStreamMode → uGetStream.struIPChan 访问通道信息

---

## 依赖说明

本项目依赖以下 Java 库：

| 依赖 | 说明 |
|------|------|
| JNA (com.sun.jna) | Java Native Access，用于调用 HCNetSDK.dll / libhcnetsdk.so |
| Spring Boot (可选) | 若需作为 Web 服务运行，需引入 spring-web 等依赖 |

HCNetSDK.java 文件包含完整的 310 个原生方法声明、655 个结构体定义和 852 个常量，需与海康 SDK 安装包保持一致。