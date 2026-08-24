package com.example.hknvr.controller;

import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.service.CameraService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cameras")
public class CameraController {

    private final CameraService cameraService;

    public CameraController(CameraService cameraService) {
        this.cameraService = cameraService;
    }

    @GetMapping
    public List<CameraInfo> list() {
        return cameraService.listCameras();
    }

    @GetMapping("/{cameraId}")
    public CameraInfo detail(@PathVariable String cameraId) {
        return cameraService.getCameraById(cameraId);
    }
}
