package com.example.hknvr.service;

import com.example.hknvr.config.HikvisionProperties;
import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.sdk.HCNetSDK;
import com.example.hknvr.sdk.HikSdkManager;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class CameraService {

    private final HikvisionProperties properties;
    private final DeviceSessionService deviceSessionService;

    public CameraService(HikvisionProperties properties, DeviceSessionService deviceSessionService) {
        this.properties = properties;
        this.deviceSessionService = deviceSessionService;
    }

    public List<CameraInfo> listCameras() {
        List<CameraInfo> cameras = loadCamerasFromNvr();
        return cameras.isEmpty() ? Collections.emptyList() : cameras;
    }

    public HikvisionProperties.CameraConfig getCameraById(String cameraId) {
        return properties.getCameras().stream()
                .filter(c -> cameraId.equals(c.getId()))
                .findFirst()
                .orElseGet(() -> listCameras().stream()
                        .filter(c -> cameraId.equals(c.getId()))
                        .map(this::convertConfig)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown camera id: " + cameraId)));
    }

    private HikvisionProperties.CameraConfig convertConfig(CameraInfo camera) {
        HikvisionProperties.CameraConfig config = new HikvisionProperties.CameraConfig();
        config.setId(camera.getId());
        config.setName(camera.getName());
        config.setChannel(camera.getChannel());
        return config;
    }

    private List<CameraInfo> loadCamerasFromNvr() {
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
                log.warn("获取IP通道配置失败, 错误码: {}", sdk.NET_DVR_GetLastError());
                return Collections.emptyList();
            }

            config.read();

            return config.dwDChanNum > 0
                    ? buildCameras(config.dwStartDChan, config.dwDChanNum, config)
                    : buildCamerasFromIP(config);
        } catch (Exception e) {
            log.warn("加载NVR摄像头失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<CameraInfo> buildCameras(int startChannel, int count,
                                          HCNetSDK.NET_DVR_IPPARACFG_V40 config) {
        List<CameraInfo> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int channel = startChannel + i;
            result.add(buildCamera(channel, resolveIp(config, channel, startChannel)));
        }
        return result;
    }

    private List<CameraInfo> buildCamerasFromIP(HCNetSDK.NET_DVR_IPPARACFG_V40 config) {
        List<CameraInfo> result = new ArrayList<>();
        for (int i = 0; i < config.struIPDevInfo.length; i++) {
            HCNetSDK.NET_DVR_IPDEVINFO_V31 device = config.struIPDevInfo[i];
            if (device.byEnable != 0) {
                result.add(buildCamera(config.dwStartDChan + i,
                        new String(device.struIP.sIpV4).trim()));
            }
        }
        return result;
    }

    private CameraInfo buildCamera(int channel, String ip) {
        HikvisionProperties.CameraConfig preferred = findPreferred(channel);
        return CameraInfo.builder()
                .id(preferred != null && preferred.getId() != null ? preferred.getId() : "cam-" + channel)
                .name(preferred != null && preferred.getName() != null ? preferred.getName() : "Camera " + channel)
                .channel(channel)
                .ip(ip)
                .build();
    }

    private String resolveIp(HCNetSDK.NET_DVR_IPPARACFG_V40 config, int channel, int startChannel) {
        int index = channel - startChannel;
        if (index < 0 || index >= config.struIPDevInfo.length) {
            return "";
        }
        HCNetSDK.NET_DVR_IPDEVINFO_V31 device = config.struIPDevInfo[index];
        return device.byEnable == 0 ? "" : new String(device.struIP.sIpV4).trim();
    }

    private HikvisionProperties.CameraConfig findPreferred(int channel) {
        return properties.getCameras().stream()
                .filter(c -> c.getChannel() == channel)
                .findFirst()
                .orElse(null);
    }
}
