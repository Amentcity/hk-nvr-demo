package com.example.hknvr.service.camera;

import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.sdk.DeviceTreeManager.ChannelInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts Hikvision NVR channel information into unified CameraInfo objects.
 */
@Component
public class ChannelCameraConverter {

    public List<CameraInfo> convert(String nvrId, List<ChannelInfo> channels) {
        List<CameraInfo> cameras = new ArrayList<>();

        if (channels == null) {
            return cameras;
        }

        for (ChannelInfo channel : channels) {
            CameraInfo camera = CameraInfo.builder()
                    .id(buildId(nvrId, channel.channelNo))
                    .name(channel.channelName)
                    .nvrId(nvrId)
                    .channel(channel.channelNo)
                    .ip(channel.ipAddress)
                    .type(channel.type.name())
                    .online(channel.enabled)
                    .build();

            cameras.add(camera);
        }

        return cameras;
    }

    private String buildId(String nvrId, int channelNo) {
        return nvrId + "_CH" + channelNo;
    }
}
