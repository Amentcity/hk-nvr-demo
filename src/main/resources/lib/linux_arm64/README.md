# 海康 Device Network SDK 本地库目录

请将官网下载的 SDK 文件复制到对应平台子目录：

- Windows 64-bit: `win64/`
- Linux 64-bit: `linux64/`

## win64 最低文件清单

```
HCNetSDK.dll
PlayCtrl.dll
SuperRender.dll
AudioRender.dll
libssl-1_1-x64.dll
libcrypto-1_1-x64.dll
HCNetSDKCom/   （整个目录）
```

下载地址：https://www.hikvision.com/cn/support/download/sdk/

选择 **设备网络 SDK -> Windows 64-bit**。

## 注意

- DLL 位数必须与 JVM 一致（64-bit JDK 对应 64-bit SDK）
- 建议用官方 Demo 中的 `HCNetSDK.java` 替换项目 `com.example.hknvr.sdk.HCNetSDK`
