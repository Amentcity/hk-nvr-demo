package com.example.hknvr.service;

import com.example.hknvr.config.HikvisionProperties;
import com.example.hknvr.sdk.HCNetSDK;
import com.example.hknvr.sdk.HikSdkManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * NVR 设备登录与会话管理。
 */
@Slf4j
@Service
public class DeviceSessionService {

    private final HikSdkManager sdkManager;
    private final HikvisionProperties properties;
    private final AtomicInteger userId = new AtomicInteger(-1);

    public DeviceSessionService(HikSdkManager sdkManager, HikvisionProperties properties) {
        this.sdkManager = sdkManager;
        this.properties = properties;
    }

    public synchronized int login() {
        sdkManager.ensureInitialized();
        if (userId.get() >= 0) {
            return userId.get();
        }

        HCNetSDK sdk = HikSdkManager.getHcNetSDK();
        HikvisionProperties.Device device = properties.getDevice();

        HCNetSDK.NET_DVR_USER_LOGIN_INFO loginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
        HikSdkManager.writeString(loginInfo.sDeviceAddress, device.getIp());
        loginInfo.wPort = (short) device.getPort();
        HikSdkManager.writeString(loginInfo.sUserName, device.getUsername());
        HikSdkManager.writeString(loginInfo.sPassword, device.getPassword());
        loginInfo.bUseAsynLogin = false;

        HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
        int uid = sdk.NET_DVR_Login_V40(loginInfo, deviceInfo);
        if (uid < 0) {
            int err = sdk.NET_DVR_GetLastError();
            throw new IllegalStateException("NVR login failed, error code=" + err);
        }

        userId.set(uid);
        log.info("Logged in to NVR {}:{}, userId={}", device.getIp(), device.getPort(), uid);
        return uid;
    }

    public synchronized void logout() {
        if (userId.get() < 0) {
            return;
        }
        HCNetSDK sdk = HikSdkManager.getHcNetSDK();
        if (sdk != null) {
            sdk.NET_DVR_Logout(userId.get());
        }
        userId.set(-1);
        log.info("Logged out from NVR");
    }

    public int getUserId() {
        return login();
    }

    public boolean isLoggedIn() {
        return userId.get() >= 0;
    }
}
