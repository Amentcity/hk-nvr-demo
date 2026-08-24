package com.example.hknvr.service;

import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.service.camera.NvrCameraRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CameraService {

    private final NvrCameraRegistry cameraRegistry;

    public CameraService(NvrCameraRegistry cameraRegistry) {
        this.cameraRegistry = cameraRegistry;
    }

    /**
     * Returns cameras discovered from Hikvision NVR.
     */
    public List<CameraInfo> listCameras() {
        return cameraRegistry.list();
    }

    /**
     * Returns a single camera by unified camera id.
     */
    public CameraInfo getCameraById(String cameraId) {
        CameraInfo camera = cameraRegistry.get(cameraId);
        if (camera == null) {
            throw new IllegalArgumentException("Unknown camera id: " + cameraId);
        }
        return camera;
    }
}
