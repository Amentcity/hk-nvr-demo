package com.example.hknvr.service.camera;

import com.example.hknvr.config.HikvisionProperties;
import com.example.hknvr.model.CameraInfo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Camera provider backed by application configuration.
 * Used as a fallback source when external camera discovery is unavailable.
 */
@Component
public class ConfigCameraProvider implements CameraProvider {

    private final HikvisionProperties properties;

    public ConfigCameraProvider(HikvisionProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<CameraInfo> queryCameras() {
        if (properties.getCameras() == null) {
            return List.of();
        }

        return properties.getCameras().stream()
                .map(camera -> CameraInfo.builder()
                        .id(camera.getId())
                        .name(camera.getName())
                        .channel(parseChannel(camera.getChannel()))
                        .build())
                .toList();
    }

    private int parseChannel(String channel) {
        try {
            return Integer.parseInt(channel);
        } catch (Exception ignored) {
            return 0;
        }
    }
}
