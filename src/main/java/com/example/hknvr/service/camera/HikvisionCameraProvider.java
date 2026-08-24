package com.example.hknvr.service.camera;

import com.example.hknvr.config.HikvisionProperties;
import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.service.DeviceSessionService;
import com.example.hknvr.sdk.HCNetSDK;
import com.example.hknvr.sdk.HikSdkManager;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class HikvisionCameraProvider implements CameraProvider {

    private final HikvisionProperties properties;
    private final DeviceSessionService deviceSessionService;

    public HikvisionCameraProvider(HikvisionProperties properties,
                                   DeviceSessionService deviceSessionService) {
        this.properties = properties;
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

            List<CameraInfo> result = new ArrayList<>();
            int start = config.dwStartDChan;
            int count = config.dwDChanNum;

            for (int i = 0; i < count; i++) {
                int channel = start + i;
                result.add(CameraInfo.builder()
                        .id("cam-" + channel)
                        .name("Camera " + channel)
                        .channel(channel)
                        .build());
            }

            return result;
        } catch (Exception e) {
            log.warn("查询NVR摄像头失败", e);
            return Collections.emptyList();
        }
    }
}
