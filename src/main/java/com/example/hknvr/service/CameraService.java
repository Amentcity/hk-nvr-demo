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

        return properties.getCameras().stream()
                .map(c -> CameraInfo.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .channel(c.getChannel())
                        .build())
                .collect(Collectors.toList());
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

            IntByReference bytesReturned = new IntByReference();
            boolean ok = sdk.NET_DVR_GetDVRConfig(
                    deviceSessionService.getUserId(),
                    HCNetSDK.NET_DVR_GET_IPPARACFG_V40,
                    0,
                    deviceConfig.getPointer(),
                    deviceConfig.size(),
                    bytesReturned);

            if (!ok) {
                return Collections.emptyList();
            }

            int startChannel = deviceConfig.dwStartDChan & 0xFF;
            int channelCount = deviceConfig.dwAChanNum & 0xFF;
            if (channelCount == 0) {
                return Collections.emptyList();
            }

            List<CameraInfo> cameras = new ArrayList<>();
            for (int channel = startChannel; channel < startChannel + channelCount; channel++) {
                final int currentChannel = channel;
                HikvisionProperties.CameraConfig preferred = properties.getCameras().stream()
                        .filter(c -> c.getChannel() == currentChannel)
                        .findFirst()
                        .orElse(null);

                String id = preferred != null && preferred.getId() != null ? preferred.getId() : "cam-" + currentChannel;
                String name = preferred != null && preferred.getName() != null ? preferred.getName() : "Camera " + currentChannel;

                cameras.add(CameraInfo.builder()
                        .id(id)
                        .name(name)
                        .channel(currentChannel)
                        .build());
            }
            return cameras;
        } catch (Exception e) {
            log.warn("Failed to load camera list from NVR, falling back to configured list: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
