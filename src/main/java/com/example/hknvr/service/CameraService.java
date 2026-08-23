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
        return cameras == null ? Collections.emptyList() : cameras;
    }

    public HikvisionProperties.CameraConfig getCameraById(String cameraId) {
        for (HikvisionProperties.CameraConfig config : properties.getCameras()) {
            if (config.getId().equals(cameraId)) {
                return config;
            }
        }
        throw new IllegalArgumentException("Unknown camera id: " + cameraId);
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

            boolean ok = sdk.NET_DVR_GetDVRConfig(
                    deviceSessionService.getUserId(),
                    HCNetSDK.NET_DVR_GET_IPPARACFG_V40,
                    0,
                    config.getPointer(),
                    config.size(),
                    new IntByReference());

            if (!ok) {
                log.warn("获取通道失败:{}", sdk.NET_DVR_GetLastError());
                return Collections.emptyList();
            }

            config.read();
            return buildCamerasByChannelRange(config.dwStartDChan, config.dwDChanNum, config);

        } catch (Exception e) {
            log.error("load camera error", e);
            return Collections.emptyList();
        }
    }

    private List<CameraInfo> buildCamerasByChannelRange(int start, int count,
                                                         HCNetSDK.NET_DVR_IPPARACFG_V40 config) {
        List<CameraInfo> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int channel = start + i;
            String ip = resolveIp(config, i);

            result.add(CameraInfo.builder()
                    .id("cam-" + channel)
                    .name("Camera " + channel)
                    .channel(channel)
                    .ip(ip)
                    .mainStream(buildRtsp(channel, false))
                    .subStream(buildRtsp(channel, true))
                    .online(true)
                    .build());
        }

        return result;
    }

    private String resolveIp(HCNetSDK.NET_DVR_IPPARACFG_V40 config, int index) {
        if (index >= config.struIPDevInfo.length) {
            return "";
        }
        return new String(config.struIPDevInfo[index].struIP.sIpV4).trim();
    }

    private String buildRtsp(int channel, boolean sub) {
        HikvisionProperties.Device device = properties.getDevice();
        int stream = sub ? 2 : 1;
        return String.format(
                "rtsp://%s:%s@%s:554/Streaming/Channels/%d0%d",
                device.getUsername(),
                device.getPassword(),
                device.getIp(),
                channel,
                stream);
    }
}
