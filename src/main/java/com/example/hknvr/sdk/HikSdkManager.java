package com.example.hknvr.sdk;

import com.example.hknvr.config.HikvisionProperties;
import com.example.hknvr.util.OsUtils;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.charset.StandardCharsets;

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

    public HikSdkManager(HikvisionProperties properties) {
        this.properties = properties;
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
}
