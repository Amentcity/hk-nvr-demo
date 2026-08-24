package com.example.hknvr.service.camera;

import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.sdk.HCNetSDK;
import com.example.hknvr.sdk.HikSdkManager;
import com.example.hknvr.service.DeviceSessionService;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@Primary
public class HikvisionCameraProvider implements CameraProvider {

    private final DeviceSessionService deviceSessionService;

    public HikvisionCameraProvider(DeviceSessionService deviceSessionService) {
        this.deviceSessionService = deviceSessionService;
    }

    @Override
    public List<CameraInfo> queryCameras() {
        try {
            if (!deviceSessionService.isLoggedIn()) {
                deviceSessionService.login();
            }

            HCNetSDK sdk = HikSdkManager.getHcNetSDK();
            if (sdk == null) {
                return Collections.emptyList();
            }

            HCNetSDK.NET_DVR_IPPARACFG_V40 config = new HCNetSDK.NET_DVR_IPPARACFG_V40();
            config.dwSize = config.size();
            config.write();

            boolean success = sdk.NET_DVR_GetDVRConfig(
                    deviceSessionService.getUserId(),
                    HCNetSDK.NET_DVR_GET_IPPARACFG_V40,
                    0,
                    config.getPointer(),
                    config.size(),
                    new IntByReference());

            if (!success) {
                log.warn("获取摄像头配置失败, error={}", sdk.NET_DVR_GetLastError());
                return Collections.emptyList();
            }

            config.read();

            return config.dwDChanNum > 0
                    ? buildCameras(config.dwStartDChan, config.dwDChanNum, config)
                    : buildCamerasFromIP(config);
        } catch (Exception e) {
            log.warn("查询NVR摄像头失败", e);
            return Collections.emptyList();
        }
    }

    private List<CameraInfo> buildCameras(int start, int count, HCNetSDK.NET_DVR_IPPARACFG_V40 config) {
        List<CameraInfo> result = new ArrayList<CameraInfo>();
        for (int i = 0; i < count; i++) {
            int channel = start + i;
            result.add(buildCamera(channel, resolveIp(config, channel, start)));
        }
        return result;
    }

    private List<CameraInfo> buildCamerasFromIP(HCNetSDK.NET_DVR_IPPARACFG_V40 config) {
        List<CameraInfo> result = new ArrayList<CameraInfo>();
        for (int i = 0; i < config.struIPDevInfo.length; i++) {
            HCNetSDK.NET_DVR_IPDEVINFO_V31 device = config.struIPDevInfo[i];
            if (device.byEnable != 0) {
                result.add(buildCamera(config.dwStartDChan + i, new String(device.struIP.sIpV4).trim()));
            }
        }
        return result;
    }

    private CameraInfo buildCamera(int channel, String ip) {
        return CameraInfo.builder()
                .id("cam-" + channel)
                .name("Camera " + channel)
                .channel(channel)
                .ip(ip)
                .build();
    }

    private String resolveIp(HCNetSDK.NET_DVR_IPPARACFG_V40 config, int channel, int start) {
        int index = channel - start;
        if (index < 0 || index >= config.struIPDevInfo.length) {
            return "";
        }
        HCNetSDK.NET_DVR_IPDEVINFO_V31 device = config.struIPDevInfo[index];
        return device.byEnable == 0 ? "" : new String(device.struIP.sIpV4).trim();
    }
}
