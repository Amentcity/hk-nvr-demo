package com.example.hknvr.service.camera;

import com.example.hknvr.model.CameraInfo;

import java.util.List;

/**
 * Camera data source abstraction.
 * Implementations may load cameras from NVR SDK, database, or configuration.
 */
public interface CameraProvider {

    List<CameraInfo> queryCameras();
}
