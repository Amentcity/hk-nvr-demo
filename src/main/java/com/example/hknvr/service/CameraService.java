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
import java.util.stream.Collectors;

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
        List<CameraInfo> nvrCameras = loadCamerasFromNvr();
        if (!nvrCameras.isEmpty()) {
            return nvrCameras;
        }

//        return properties.getCameras().stream()
//                .map(c -> CameraInfo.builder()
//                        .id(c.getId())
//                        .name(c.getName())
//                        .channel(c.getChannel())
//                        .build())
//                .collect(Collectors.toList());
        return nvrCameras;
    }

    public HikvisionProperties.CameraConfig getCameraById(String cameraId) {
        for (HikvisionProperties.CameraConfig config : properties.getCameras()) {
            if (config.getId().equals(cameraId)) {
                return config;
            }
        }

        for (CameraInfo camera : listCameras()) {
            if (camera.getId().equals(cameraId)) {
                HikvisionProperties.CameraConfig config = new HikvisionProperties.CameraConfig();
                config.setId(camera.getId());
                config.setName(camera.getName());
                config.setChannel(camera.getChannel());
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

            HCNetSDK.NET_DVR_IPPARACFG_V40 deviceConfig = new HCNetSDK.NET_DVR_IPPARACFG_V40();
            deviceConfig.dwSize = deviceConfig.size();
            deviceConfig.write();

            IntByReference bytesReturned = new IntByReference();
            boolean ok = sdk.NET_DVR_GetDVRConfig(
                    deviceSessionService.getUserId(),
                    HCNetSDK.NET_DVR_GET_IPPARACFG_V40,
                    0,
                    deviceConfig.getPointer(),
                    deviceConfig.size(),
                    bytesReturned);

            if (!ok) {
                log.warn("获取IP通道配置失败, 错误码: {}", sdk.NET_DVR_GetLastError());
                return Collections.emptyList();
            }

            deviceConfig.read();

            int startChannel = deviceConfig.dwStartDChan;
            int channelCount = deviceConfig.dwDChanNum;

            // 优先用数字通道数；为0则遍历IP设备数组
            if (channelCount > 0) {
                return buildCamerasByChannelRange(startChannel, channelCount, deviceConfig);
            } else {
                log.info("dwDChanNum为0，回退到遍历struIPDevInfo");
                return buildCamerasFromIPDevInfo(deviceConfig);
            }
        } catch (Exception e) {
            log.warn("Failed to load camera list from NVR, falling back to configured list: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** 按通道号区间构建（NVR数字通道） */
    private List<CameraInfo> buildCamerasByChannelRange(int startChannel, int channelCount,
                                                        HCNetSDK.NET_DVR_IPPARACFG_V40 deviceConfig) {
        List<CameraInfo> cameras = new ArrayList<>();
        for (int i = 0; i < channelCount; i++) {
            int currentChannel = startChannel + i;
            String ip = resolveIpByChannel(deviceConfig, currentChannel, startChannel);

            HikvisionProperties.CameraConfig preferred = findPreferred(currentChannel);
            String id = preferred != null && preferred.getId() != null ? preferred.getId() : "cam-" + currentChannel;
            String name = preferred != null && preferred.getName() != null ? preferred.getName() : "Camera " + currentChannel;

            cameras.add(CameraInfo.builder()
                    .id(id)
                    .name(name)
                    .channel(currentChannel)
                    .ip(ip)
                    .build());
        }
        return cameras;
    }

    /** 遍历已启用的IP设备构建 */
    private List<CameraInfo> buildCamerasFromIPDevInfo(HCNetSDK.NET_DVR_IPPARACFG_V40 deviceConfig) {
        List<CameraInfo> cameras = new ArrayList<>();
        int startChannel = deviceConfig.dwStartDChan;

        for (int i = 0; i < deviceConfig.struIPDevInfo.length; i++) {
            HCNetSDK.NET_DVR_IPDEVINFO_V31 dev = deviceConfig.struIPDevInfo[i];
            if (dev.byEnable == 0) {
                continue;
            }

            int currentChannel = startChannel + i;
            String ip = new String(dev.struIP.sIpV4).trim();

            HikvisionProperties.CameraConfig preferred = findPreferred(currentChannel);
            String id = preferred != null && preferred.getId() != null ? preferred.getId() : "cam-" + currentChannel;
            String name = preferred != null && preferred.getName() != null ? preferred.getName() : "Camera " + currentChannel;

            cameras.add(CameraInfo.builder()
                    .id(id)
                    .name(name)
                    .channel(currentChannel)
                    .ip(ip)
                    .build());
        }
        return cameras;
    }

    /** 根据通道号从IP设备数组中反查IP */
    private String resolveIpByChannel(HCNetSDK.NET_DVR_IPPARACFG_V40 deviceConfig,
                                      int currentChannel, int startChannel) {
        int index = currentChannel - startChannel;
        if (index < 0 || index >= deviceConfig.struIPDevInfo.length) {
            return "";
        }
        HCNetSDK.NET_DVR_IPDEVINFO_V31 dev = deviceConfig.struIPDevInfo[index];
        if (dev.byEnable == 0) {
            return "";
        }
        return new String(dev.struIP.sIpV4).trim();
    }

    private HikvisionProperties.CameraConfig findPreferred(int channel) {
        return properties.getCameras().stream()
                .filter(c -> c.getChannel() == channel)
                .findFirst()
                .orElse(null);
    }



}
