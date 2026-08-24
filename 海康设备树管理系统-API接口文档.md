# 海康威视 HCNetSDK 设备树管理系统 — API 接口文档

## 项目说明

本项目基于 Spring Boot + 海康威视 HCNetSDK Java JNA 调用，实现 NVR/DVR 设备管理能力，包括设备登录、注销、设备信息查询、设备树获取以及通道列表查询。

当前代码包结构：

```
com.example.hknvr
├── controller
│   ├── DeviceController.java
│   ├── NvrController.java
│   └── GlobalExceptionHandler.java
├── service
├── sdk
├── dto
├── vo
├── model
└── common
```

核心 REST 控制器：
`com.example.hknvr.controller.DeviceController`

接口统一前缀：

```
/api/device
```

---

# 接口列表

| 方法 | 地址 | 说明 |
|---|---|---|
| POST | `/api/device/login` | 登录海康设备 |
| POST | `/api/device/logout` | 注销设备 |
| GET | `/api/device/tree/{userId}` | 获取设备树 |
| GET | `/api/device/channels/{userId}` | 获取通道列表 |
| GET | `/api/device/info/{userId}` | 获取设备详细信息 |

---

# 1. 登录设备

## 请求

```
POST /api/device/login
Content-Type: application/json
```

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|-|-|-|-|
| ip | String | 是 | 设备IP地址 |
| port | Integer | 否 | SDK端口，默认8000 |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

示例：

```json
{
  "ip":"192.168.1.100",
  "port":8000,
  "username":"admin",
  "password":"admin123"
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

其中 data 为设备登录 userId。

---

# 2. 注销设备

```
POST /api/device/logout
```

请求：

```json
{
 "userId":1
}
```

说明：释放 HCNetSDK 登录会话。

---

# 3. 获取设备树

```
GET /api/device/tree/{userId}
```

返回完整设备树，包括：

- 设备基础信息
- 模拟通道
- IP通道
- 零通道

示例：

```json
{
 "code":200,
 "message":"success",
 "data":{
   "deviceName":"DS-8600",
   "channels":[
     {
       "channelNo":1,
       "channelName":"Camera1",
       "type":"ANALOG"
     }
   ]
 }
}
```

---

# 4. 获取通道列表

```
GET /api/device/channels/{userId}
```

返回：

```json
[
 {
  "channelNo":33,
  "channelName":"IPCamera33",
  "type":"IP",
  "enabled":true
 }
]
```

---

# 5. 获取设备信息

```
GET /api/device/info/{userId}
```

返回设备详细配置：

```json
{
 "userId":1,
 "ipAddress":"192.168.1.100",
 "port":8000,
 "deviceName":"NVR",
 "serialNumber":"xxxx",
 "analogChanNum":16,
 "ipChanNum":32
}
```

---

# 统一响应 Result

所有接口返回：

```json
{
 "code":200,
 "message":"success",
 "data":{}
}
```

状态码：

| code | 说明 |
|-|-|
|200|成功|
|400|参数错误|
|404|设备会话不存在|
|500|服务器或设备错误|

---

# 通道类型

| 类型 | 说明 |
|-|-|
| ANALOG | 模拟摄像头 |
| IP | 网络摄像机 |
| ZERO | 零通道 |
| AUDIO | 音频通道 |
| EXTERNAL | 扩展通道 |

---

# 调用流程

```
前端
 |
 | POST /login
 v
DeviceController
 |
 v
DeviceTreeService
 |
 v
HCNetSDK
 |
 v
海康设备
```

典型流程：

1. 调用 `/api/device/login` 获取 userId
2. 使用 userId 查询设备信息
3. 调用 `/api/device/tree/{userId}` 展示设备树
4. 调用 `/api/device/channels/{userId}` 展示摄像头列表
5. 使用完成后调用 `/api/device/logout`

---

# 当前代码同步说明

文档已根据当前仓库代码同步：

- Controller 路径：`com.example.hknvr.controller.DeviceController`
- API 前缀：`/api/device`
- 当前接口数量：5个
- 请求 DTO：`LoginRequest`、`DeviceConfigRequest`
- 响应 VO：`DeviceTreeVO`、`DeviceInfoVO`、`ChannelVO`
- 统一返回：`Result<T>`
