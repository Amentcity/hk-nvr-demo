package com.example.hknvr.sdk;

import com.example.hknvr.config.HikvisionProperties;
import com.example.hknvr.util.OsUtils;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 海康 SDK 全局初始化与单例管理。
 */
@Slf4j
@Component
public class HikSdkManager {

    @Getter
    private static HCNetSDK hcNetSDK;

    @Getter
    private volatile boolean initialized;

    private final HikvisionProperties properties;

    private final Map<Integer, DeviceSession> deviceSessions = new ConcurrentHashMap<>();

    /**
     * 字符集
     */
    private static final Charset GBK = Charset.forName("GBK");

    private final Logger logger = LoggerFactory.getLogger(HikSdkManager.class.getName());


    public HikSdkManager(HikvisionProperties properties) {
        this.properties = properties;
    }

    /**
     * 设备会话（直接使用SDK结构体，不创建新的实体类）
     */
    public static class DeviceSession {
        public int loginID;
        public HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo;
        public HCNetSDK.NET_DVR_DEVICECFG_V40 deviceCfg;
        public HCNetSDK.NET_DVR_IPPARACFG_V40 ipParaCfg;
        public String ipAddress;    // 设备IP地址
        public int port;            // 设备端口
    }

    /**
     * 通道信息（直接使用SDK结构体）
     */
    public static class ChannelInfo {
        public int channelNo;           // 通道号
        public String channelName;      // 通道名称
        public boolean enabled;         // 是否启用
        public ChannelType type;        // 通道类型
        public String ipAddress;        // IP通道的IP地址
        public short port;              // IP通道的端口
    }

    /**
     * 通道类型枚举
     */
    public enum ChannelType {
        ANALOG,       // 模拟通道
        IP,           // IP通道
        ZERO,         // 零通道
        AUDIO,        // 语音通道
        EXTERNAL      // 扩展通道
    }

    /**
     * 设备信息（直接使用SDK结构体）
     */
    public static class DeviceInfo {
        public int loginID;
        public String ipAddress;
        public int port;
        public String serialNumber;
        public String deviceName;
        public int deviceType;
        public int analogChanNum;
        public int ipChanNum;
        public int startChan;
        public int startDChan;
        public int zeroChanNum;
        public int audioChanNum;
    }

    @PostConstruct
    public void init() {
        try {
            File libDir = OsUtils.resolveSdkLibDir(properties.getSdk().getLibPath());
            String libPath = libDir.getAbsolutePath();
            NativeLibrary.addSearchPath(OsUtils.getSdkLibraryName(), libPath);
            System.setProperty("jna.library.path", libPath);
            hcNetSDK = Native.load(OsUtils.getSdkLibraryName(), HCNetSDK.class);
            log.info("Loaded Hikvision SDK from {}", libDir.getAbsolutePath());

            if (!hcNetSDK.NET_DVR_Init()) {
                throw new IllegalStateException("NET_DVR_Init failed, error=" + hcNetSDK.NET_DVR_GetLastError());
            }

            HikvisionProperties.Sdk sdk = properties.getSdk();
            hcNetSDK.NET_DVR_SetConnectTime(sdk.getConnectTimeoutMs(), 2);
            hcNetSDK.NET_DVR_SetReconnect(sdk.getReconnectIntervalMs(), true);

            File logDir = new File(sdk.getLogPath());
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            hcNetSDK.NET_DVR_SetLogToFile(3, logDir.getAbsolutePath(), true);

            hcNetSDK.NET_DVR_SetExceptionCallBack_V30(0, 0, (dwType, lUserID, lHandle, pUser) ->
                    log.warn("Hikvision SDK exception: type={}, userId={}, handle={}", dwType, lUserID, lHandle),
                    Pointer.NULL);

            initialized = true;
            log.info("Hikvision SDK initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Hikvision SDK: {}. Clip download will be unavailable until SDK is configured.",
                    e.getMessage());
            initialized = false;
        }
    }

    @PreDestroy
    public void cleanup() {
        if (hcNetSDK != null && initialized) {
            hcNetSDK.NET_DVR_Cleanup();
            log.info("Hikvision SDK cleaned up");
        }
    }

    public void ensureInitialized() {
        if (!initialized || hcNetSDK == null) {
            throw new IllegalStateException(
                    "Hikvision SDK is not initialized. Please copy SDK libraries to "
                            + properties.getSdk().getLibPath());
        }
    }

    /**
     * 将字符串写入 SDK 定长 byte 数组（GBK 编码，与海康 SDK 一致）。
     */
    public static void writeString(byte[] dest, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(bytes.length, dest.length - 1);
        System.arraycopy(bytes, 0, dest, 0, len);
        dest[len] = 0;
    }

    /**
     * 清理SDK
     * @return 成功返回true
     */
    public boolean cleanupSDK() {
        // 先注销所有设备
        for (Map.Entry<Integer, DeviceSession> entry : deviceSessions.entrySet()) {
            hcNetSDK.NET_DVR_Logout(entry.getKey());
        }
        deviceSessions.clear();
        return hcNetSDK.NET_DVR_Cleanup();
    }

    // ==================== 设备登录/注销 ====================

    /**
     * 登录设备（使用NET_DVR_USER_LOGIN_INFO结构体设置设备地址）
     * @param ipAddress 设备IP地址
     * @param port 设备端口（默认8000）
     * @param username 用户名
     * @param password 密码
     * @return 设备会话，失败返回null
     */
    public DeviceSession login(String ipAddress, int port, String username, String password) {
        // 创建登录信息结构体
        HCNetSDK.NET_DVR_USER_LOGIN_INFO struLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
        // 设置设备地址（使用GBK编码，不足129字节自动补0）
        byte[] addressBytes = ipAddress.getBytes(GBK);
        System.arraycopy(addressBytes, 0, struLoginInfo.sDeviceAddress, 0, addressBytes.length);
        // 设置端口
        struLoginInfo.wPort = (short) port;
        // 设置用户名
        byte[] userBytes = username.getBytes(GBK);
        System.arraycopy(userBytes, 0, struLoginInfo.sUserName, 0, userBytes.length);
        // 设置密码
        byte[] passBytes = password.getBytes(GBK);
        System.arraycopy(passBytes, 0, struLoginInfo.sPassword, 0, passBytes.length);
        // 登录模式：0-Private登录
        struLoginInfo.byLoginMode = (byte) 0;

        // 创建设备信息结构体
        HCNetSDK.NET_DVR_DEVICEINFO_V40 struDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();

        // 调用登录（使用SDK已有的方法签名）
        int loginID = hcNetSDK.NET_DVR_Login_V40(struLoginInfo, struDeviceInfo);

        if (loginID < 0) {
            System.err.println("设备登录失败: " + ipAddress + ":" + port +
                    ", 错误码: " + hcNetSDK.NET_DVR_GetLastError());
            return null;
        }

        // 获取设备配置（模拟通道信息）
        HCNetSDK.NET_DVR_DEVICECFG_V40 deviceCfg = getDeviceConfig(loginID);
        // 获取IP通道配置
        HCNetSDK.NET_DVR_IPPARACFG_V40 ipParaCfg = getIPParaConfig(loginID);

        // 创建设备会话
        DeviceSession session = new DeviceSession();
        session.loginID = loginID;
        session.deviceInfo = struDeviceInfo;
        session.deviceCfg = deviceCfg;
        session.ipParaCfg = ipParaCfg;
        session.ipAddress = ipAddress;
        session.port = port;

        deviceSessions.put(loginID, session);

        // 打印设备信息
        String serial = new String(struDeviceInfo.struDeviceV30.sSerialNumber).trim();
        assert session.deviceCfg != null;
        String name = new String(session.deviceCfg.sDVRName).trim();
        byte chanNum = struDeviceInfo.struDeviceV30.byChanNum;
        // V40: 使用ipParaCfg中的V40字段
        int ipChanNum = session.ipParaCfg != null ? session.ipParaCfg.dwDChanNum : 0;
        int startDChan = session.ipParaCfg != null ? session.ipParaCfg.dwStartDChan : 0;

        logger.info("设备登录成功: {}:{}, 序列号: {}, 设备名称: {}, 模拟通道数: {}, IP通道数: {}, 数字通道起始号: {}", ipAddress, port, serial, name, chanNum, ipChanNum, startDChan);

        return session;
    }

    /**
     * 注销设备
     * @param loginID 登录ID
     * @return 成功返回true
     */
    public boolean logout(int loginID) {
        DeviceSession session = deviceSessions.remove(loginID);
        if (session != null) {
            boolean result = hcNetSDK.NET_DVR_Logout(loginID);
            System.out.println("设备注销: loginID=" + loginID + ", 结果: " + result);
            return result;
        }
        return false;
    }

    /**
     * 注销所有设备
     */
    public void logoutAll() {
        for (Map.Entry<Integer, DeviceSession> entry : deviceSessions.entrySet()) {
            hcNetSDK.NET_DVR_Logout(entry.getKey());
        }
        deviceSessions.clear();
    }

    // ==================== 设备配置获取 ====================

    /**
     * 获取设备配置（模拟通道信息）
     * 使用SDK已有的NET_DVR_GetDVRConfig方法（Pointer参数版本）
     * @param loginID 登录ID
     * @return 设备配置结构体
     */
    private HCNetSDK.NET_DVR_DEVICECFG_V40 getDeviceConfig(int loginID) {
        HCNetSDK.NET_DVR_DEVICECFG_V40 deviceCfg = new HCNetSDK.NET_DVR_DEVICECFG_V40();
        deviceCfg.write();  // 将Java对象写入原生内存
        IntByReference lBytesReturned = new IntByReference(0);

        // 使用SDK已有的方法签名：Pointer lpOutBuffer
        boolean result = hcNetSDK.NET_DVR_GetDVRConfig(
                loginID,
                HCNetSDK.NET_DVR_GET_DEVICECFG_V40,
                0,
                deviceCfg.getPointer(),  // 获取结构体的原生内存指针
                deviceCfg.size(),        // 结构体大小
                lBytesReturned
        );

        if (result) {
            deviceCfg.read();  // 从原生内存读取到Java对象
            return deviceCfg;
        } else {
            System.err.println("获取设备配置失败，错误码: " + hcNetSDK.NET_DVR_GetLastError());
            return null;
        }
    }

    /**
     * 获取IP通道配置
     * 使用SDK已有的NET_DVR_GetDVRConfig方法（Pointer参数版本）
     * @param loginID 登录ID
     * @return IP通道配置结构体
     */
    private HCNetSDK.NET_DVR_IPPARACFG_V40 getIPParaConfig(int loginID) {
        HCNetSDK.NET_DVR_IPPARACFG_V40 ipParaCfg = new HCNetSDK.NET_DVR_IPPARACFG_V40();
        ipParaCfg.write();
        IntByReference lBytesReturned = new IntByReference(0);

        // 使用SDK已有的方法签名：Pointer lpOutBuffer
        boolean result = hcNetSDK.NET_DVR_GetDVRConfig(
                loginID,
                HCNetSDK.NET_DVR_GET_IPPARACFG_V40,
                0,
                ipParaCfg.getPointer(),
                ipParaCfg.size(),
                lBytesReturned
        );

        if (result) {
            ipParaCfg.read();
            return ipParaCfg;
        } else {
            System.err.println("获取IP通道配置失败，错误码: " + hcNetSDK.NET_DVR_GetLastError());
            return null;
        }
    }

    // ==================== 设备树构建 ====================

    /**
     * 获取模拟通道列表
     * @param session 设备会话
     * @return 模拟通道列表
     */
    public List<ChannelInfo> getAnalogChannels(DeviceSession session) {
        List<ChannelInfo> channels = new ArrayList<>();
        if (session.deviceCfg == null) {
            return channels;
        }

        byte analogChanNum = session.deviceInfo.struDeviceV30.byChanNum;
        byte[] analogChanEnable = session.ipParaCfg.byAnalogChanEnable;
        byte startChan = session.deviceInfo.struDeviceV30.byStartChan;

        for (int i = 0; i < analogChanNum; i++) {
            int chanNo = startChan + i;
            boolean enabled = (analogChanEnable[i] & 0xFF) == 1;

            ChannelInfo info = new ChannelInfo();
            info.channelNo = chanNo;
            info.channelName = "Camera" + chanNo;
            info.enabled = enabled;
            info.type = ChannelType.ANALOG;

            channels.add(info);
        }

        return channels;
    }

    /**
     * 获取IP通道列表（V40版本，使用NET_DVR_IPPARACFG_V40结构体）
     * @param session 设备会话
     * @return IP通道列表
     */
    public List<ChannelInfo> getIPChannels(DeviceSession session) {
        List<ChannelInfo> channels = new ArrayList<>();
        if (session.ipParaCfg == null) {
            return channels;
        }

        // V40: 数字通道数在ipParaCfg.dwDChanNum中（int类型）
        int ipChanNum = session.ipParaCfg.dwDChanNum;
        // V40: 起始数字通道在ipParaCfg.dwStartDChan中（int类型）
        int startDChan = session.ipParaCfg.dwStartDChan;
        // V40: 使用struStreamMode替代struIPChanInfo，类型为NET_DVR_STREAM_MODE[]
        HCNetSDK.NET_DVR_STREAM_MODE[] streamModes = session.ipParaCfg.struStreamMode;
        // V40: struIPDevInfo类型为NET_DVR_IPDEVINFO_V31[]
        HCNetSDK.NET_DVR_IPDEVINFO_V31[] ipDevInfos = session.ipParaCfg.struIPDevInfo;

        for (int i = 0; i < ipChanNum; i++) {
            if (i < streamModes.length) {
                HCNetSDK.NET_DVR_STREAM_MODE streamMode = streamModes[i];
                // V40: 通过uGetStream.struIPChan访问NET_DVR_IPCHANINFO_V40
                HCNetSDK.NET_DVR_IPCHANINFO_V40 ipChanInfo = streamMode.uGetStream.struIPChan;
                boolean enabled = (ipChanInfo.byEnable & 0xFF) == 1;
                int chanNo = startDChan + i;

                // 获取关联的IP设备信息
                String ipAddr = "";
                short port = 8000;
                // V40: wIPID是short类型（而非byte）
                int ipID = ipChanInfo.wIPID & 0xFFFF;
                if (ipID > 0 && ipID <= ipDevInfos.length) {
                    HCNetSDK.NET_DVR_IPDEVINFO_V31 ipDevInfo = ipDevInfos[ipID - 1];
                    if (ipDevInfo != null) {
                        // 提取IP地址（去掉末尾的0字节）
                        byte[] ipBytes = ipDevInfo.struIP.sIpV4;
                        int end = 0;
                        while (end < ipBytes.length && ipBytes[end] != 0) end++;
                        ipAddr = new String(ipBytes, 0, end, GBK);
                        port = ipDevInfo.wDVRPort;
                    }
                }

                ChannelInfo info = new ChannelInfo();
                info.channelNo = chanNo;
                info.channelName = "IPCamera" + chanNo;
                info.enabled = enabled;
                info.type = ChannelType.IP;
                info.ipAddress = ipAddr;
                info.port = port;

                channels.add(info);
            }
        }

        return channels;
    }

    /**
     * 获取零通道
     * @param session 设备会话
     * @return 零通道列表（最多1个）
     */
    public List<ChannelInfo> getZeroChannels(DeviceSession session) {
        List<ChannelInfo> channels = new ArrayList<>();
        byte zeroChanNum = session.deviceInfo.struDeviceV30.byZeroChanNum;

        if (zeroChanNum > 0) {
            ChannelInfo info = new ChannelInfo();
            info.channelNo = 0;
            info.channelName = "ZeroChan0";
            info.enabled = true;
            info.type = ChannelType.ZERO;
            channels.add(info);
        }

        return channels;
    }

    /**
     * 获取设备所有通道（设备树）
     * 通道顺序：模拟通道 -> IP通道 -> 零通道
     * @param session 设备会话
     * @return 所有通道列表
     */
    public List<ChannelInfo> getAllChannels(DeviceSession session) {
        List<ChannelInfo> allChannels = new ArrayList<>();

        // 1. 模拟通道
        allChannels.addAll(getAnalogChannels(session));

        // 2. IP通道
        allChannels.addAll(getIPChannels(session));

        // 3. 零通道
        allChannels.addAll(getZeroChannels(session));

        return allChannels;
    }

    /**
     * 获取设备信息
     * @param session 设备会话
     * @return 设备信息
     */
    public DeviceInfo getDeviceInfo(DeviceSession session) {
        if (session == null || session.deviceInfo == null) {
            return null;
        }

        HCNetSDK.NET_DVR_DEVICEINFO_V30 devInfo30 = session.deviceInfo.struDeviceV30;

        DeviceInfo info = new DeviceInfo();
        info.loginID = session.loginID;
        info.ipAddress = session.ipAddress;
        info.port = session.port;
        info.serialNumber = new String(devInfo30.sSerialNumber).trim();
        // 设备名称从deviceCfg获取（NET_DVR_DEVICEINFO_V30中没有sDVRName字段）
        if (session.deviceCfg != null) {
            info.deviceName = new String(session.deviceCfg.sDVRName).trim();
        } else {
            info.deviceName = "";
        }
        info.deviceType = devInfo30.byDVRType;
        info.analogChanNum = devInfo30.byChanNum;
        // V40: 使用ipParaCfg中的V40字段
        info.ipChanNum = session.ipParaCfg != null ? session.ipParaCfg.dwDChanNum : devInfo30.byIPChanNum & 0xFF;
        info.startChan = devInfo30.byStartChan;
        info.startDChan = session.ipParaCfg != null ? session.ipParaCfg.dwStartDChan : devInfo30.byStartDChan & 0xFF;
        info.zeroChanNum = devInfo30.byZeroChanNum;
        info.audioChanNum = devInfo30.byAudioChanNum;

        return info;
    }

    // ==================== 设备树文本输出 ====================

    /**
     * 生成设备树文本（用于控制台显示）
     * @param session 设备会话
     * @return 设备树文本
     */
    public String getDeviceTreeString(DeviceSession session) {
        if (session == null) {
            return "设备会话为空";
        }

        DeviceInfo deviceInfo = getDeviceInfo(session);
        if (deviceInfo == null) {
            return "设备信息为空";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== 设备树 ===\n");
        sb.append("[设备] ").append(deviceInfo.deviceName)
                .append(" (").append(deviceInfo.ipAddress).append(":").append(deviceInfo.port).append(")\n");
        sb.append("  序列号: ").append(deviceInfo.serialNumber).append("\n");
        sb.append("  设备类型: ").append(deviceInfo.deviceType).append("\n");
        sb.append("  模拟通道: ").append(deviceInfo.analogChanNum).append("\n");
        sb.append("  IP通道: ").append(deviceInfo.ipChanNum).append("\n");
        sb.append("  数字通道起始号: ").append(deviceInfo.startDChan).append("\n");
        sb.append("  零通道: ").append(deviceInfo.zeroChanNum).append("\n");
        sb.append("  语音通道: ").append(deviceInfo.audioChanNum).append("\n");
        sb.append("\n[通道列表]\n");

        List<ChannelInfo> allChannels = getAllChannels(session);
        for (ChannelInfo ch : allChannels) {
            String prefix = "  ";
            String typePrefix = "";
            switch (ch.type) {
                case ANALOG:
                    typePrefix = "[模拟] ";
                    break;
                case IP:
                    typePrefix = "[IP] ";
                    break;
                case ZERO:
                    typePrefix = "[零通道] ";
                    break;
            }
            sb.append(prefix).append(typePrefix)
                    .append(ch.channelName)
                    .append(" (通道").append(ch.channelNo).append(")")
                    .append(ch.enabled ? " [在线]" : " [离线]").append("\n");
        }

        sb.append("=== 共 ").append(allChannels.size()).append(" 个通道 ===");

        return sb.toString();
    }

    // ==================== 设备列表管理 ====================

    /**
     * 获取所有已登录设备数量
     * @return 设备数量
     */
    public int getDeviceCount() {
        return deviceSessions.size();
    }

    /**
     * 获取所有设备会话
     * @return 设备会话列表
     */
    public List<DeviceSession> getAllSessions() {
        return new ArrayList<>(deviceSessions.values());
    }

    /**
     * 根据登录ID获取设备会话
     * @param loginID 登录ID
     * @return 设备会话，不存在返回null
     */
    public DeviceSession getSession(int loginID) {
        return deviceSessions.get(loginID);
    }

    /**
     * 获取当前错误码
     * @return 错误码
     */
    public int getLastError() {
        return hcNetSDK.NET_DVR_GetLastError();
    }
}
