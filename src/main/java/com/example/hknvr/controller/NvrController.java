package com.example.hknvr.controller;

import com.example.hknvr.model.ApiResponse;
import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.model.LivePlayRequest;
import com.example.hknvr.model.LivePlayResult;
import com.example.hknvr.model.RecordingSessionStartResult;
import com.example.hknvr.model.RecordingSessionStatus;
import com.example.hknvr.model.RecordingStartRequest;
import com.example.hknvr.model.RecordingStopRequest;
import com.example.hknvr.model.RecordingStopResult;
import com.example.hknvr.sdk.HikSdkManager;
import com.example.hknvr.service.CameraService;
import com.example.hknvr.service.DeviceSessionService;
import com.example.hknvr.service.LivePreviewService;
import com.example.hknvr.service.VideoClipDownloadService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NvrController {

    private final CameraService cameraService;
    private final LivePreviewService livePreviewService;
    private final VideoClipDownloadService clipDownloadService;
    private final DeviceSessionService sessionService;
    private final HikSdkManager sdkManager;

    public NvrController(CameraService cameraService,
                         LivePreviewService livePreviewService,
                         VideoClipDownloadService clipDownloadService,
                         DeviceSessionService sessionService,
                         HikSdkManager sdkManager) {
        this.cameraService = cameraService;
        this.livePreviewService = livePreviewService;
        this.clipDownloadService = clipDownloadService;
        this.sessionService = sessionService;
        this.sdkManager = sdkManager;
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        Map<String, Object> data = new HashMap<>();
        data.put("sdkInitialized", sdkManager.isInitialized());
        data.put("loggedIn", sessionService.isLoggedIn());
        return ApiResponse.ok(data);
    }

    @GetMapping("/cameras")
    public ApiResponse<List<CameraInfo>> listCameras() {
        return ApiResponse.ok(cameraService.listCameras());
    }

    /**
     * 返回播放会话信息，不直接暴露 RTSP 地址。
     */
    @PostMapping("/live/play")
    public ApiResponse<List<LivePlayResult>> playLive(@RequestBody LivePlayRequest request) {
        List<LivePlayResult> results = livePreviewService.buildPlayResults(request.getCameraIds(), true);
        return ApiResponse.ok(results);
    }

    @PostMapping("/live/stop")
    public ApiResponse<Void> stopLiveBatch(@RequestBody LivePlayRequest request) {
        livePreviewService.stopStreams(request.getCameraIds());
        return ApiResponse.ok("Streams stopped", null);
    }

    @GetMapping("/live/active")
    public ApiResponse<List<String>> listActiveStreams() {
        return ApiResponse.ok(livePreviewService.listActiveStreams());
    }

    @PostMapping("/live/{cameraId}/stop")
    public ApiResponse<Void> stopLive(@PathVariable String cameraId) {
        livePreviewService.stopStream(cameraId);
        return ApiResponse.ok("Stream stopped", null);
    }

    @PostMapping("/recordings/start")
    public ApiResponse<RecordingSessionStartResult> startRecording(@RequestBody RecordingStartRequest request) {
        return ApiResponse.ok(clipDownloadService.startRecording(request));
    }

    @PostMapping("/recordings/stop")
    public ApiResponse<RecordingStopResult> stopRecording(@RequestBody RecordingStopRequest request) {
        return ApiResponse.ok(clipDownloadService.stopRecording(request));
    }

    @GetMapping("/recordings/active")
    public ApiResponse<List<RecordingSessionStatus>> listActiveRecordings() {
        return ApiResponse.ok(clipDownloadService.listActiveRecordings());
    }
}
