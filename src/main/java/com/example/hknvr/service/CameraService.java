package com.example.hknvr.service;

import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.service.camera.CameraProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CameraService {

    private final CameraProvider cameraProvider;

    public CameraService(CameraProvider cameraProvider) {
        this.cameraProvider = cameraProvider;
    }

    public List<CameraInfo> listCameras() {
        return cameraProvider.queryCameras();
    }

    public CameraInfo getCameraById(String cameraId) {
        return listCameras()
                .stream()
                .filter(camera -> cameraId.equals(camera.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown camera id: " + cameraId));
    }
}
