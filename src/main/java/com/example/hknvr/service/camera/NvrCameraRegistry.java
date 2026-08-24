package com.example.hknvr.service.camera;

import com.example.hknvr.model.CameraInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified camera registry.
 *
 * Phase 1 foundation:
 * - Removes the dependency on static camera configuration.
 * - Provides a single camera source for video service and digital twin.
 * - Can be populated by Hikvision device tree scanning in the next step.
 */
@Service
public class NvrCameraRegistry {

    private final Map<String, CameraInfo> cameras = new ConcurrentHashMap<>();

    public void register(CameraInfo camera) {
        if (camera == null || camera.getId() == null) {
            return;
        }
        cameras.put(camera.getId(), camera);
    }

    public void registerAll(List<CameraInfo> cameraList) {
        if (cameraList == null) {
            return;
        }
        cameraList.forEach(this::register);
    }

    public List<CameraInfo> list() {
        return new ArrayList<>(cameras.values());
    }

    public CameraInfo get(String cameraId) {
        return cameras.get(cameraId);
    }

    public void clear() {
        cameras.clear();
    }
}
