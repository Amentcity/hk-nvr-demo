package com.example.hknvr.controller;

import com.example.hknvr.model.ApiResponse;
import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.model.LivePlayRequest;
import com.example.hknvr.model.LivePlayResult;
import com.example.hknvr.model.ClipDownloadRequest;
import com.example.hknvr.model.ClipDownloadResult;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
@CrossOrigin
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
     * 直接返回该摄像头的 RTSP 播放地址，前端播放器直接使用该地址。 
     */
    @GetMapping("/live/{cameraId}/rtsp")
    public ApiResponse<Map<String, String>> liveRtsp(@PathVariable String cameraId,
                                                    @RequestParam(defaultValue = "true") boolean substream) {
        Map<String, String> data = new HashMap<>();
        data.put("cameraId", cameraId);
        data.put("rtspUrl", livePreviewService.buildRtspStreamUrl(cameraId, substream));
        return ApiResponse.ok(data);
    }

    /**
     * 兼容旧 Mjpeg 入口：返回 RTSP 地址，而不是启动 FFmpeg 代理。
     */
    @GetMapping("/live/{cameraId}/mjpeg")
    public void liveMjpeg(@PathVariable String cameraId,
                          @RequestParam(defaultValue = "true") boolean substream,
                          HttpServletResponse response) throws IOException {
        livePreviewService.streamMjpeg(cameraId, substream, response);
    }

    /**
     * 多路同时播放：返回每路 RTSP 地址。
     */
    @PostMapping("/live/play")
    public ApiResponse<List<LivePlayResult>> playLive(@RequestBody LivePlayRequest request) {
        boolean substream = request.getSubstream() == null || request.getSubstream();
        List<LivePlayResult> results = livePreviewService.buildPlayResults(request.getCameraIds(), substream);
        return ApiResponse.ok(results);
    }

    @GetMapping("/live/play")
    public ApiResponse<List<LivePlayResult>> playLiveGet(
            @RequestParam List<String> cameraIds,
            @RequestParam(defaultValue = "true") boolean substream) {
        List<LivePlayResult> results = livePreviewService.buildPlayResults(cameraIds, substream);
        return ApiResponse.ok(results);
    }

    @PostMapping("/live/stop")
    public ApiResponse<Void> stopLiveBatch(@RequestBody LivePlayRequest request) {
        livePreviewService.stopStreams(request.getCameraIds());
        return ApiResponse.ok("Streams stopped", null);
    }

    @PostMapping("/live/stop/all")
    public ApiResponse<Void> stopAllLive() {
        livePreviewService.stopAllStreams();
        return ApiResponse.ok("All streams stopped", null);
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

    @Deprecated
    @PostMapping("/clips/download")
    public ApiResponse<List<ClipDownloadResult>> downloadClips(@RequestBody ClipDownloadRequest request) {
        List<ClipDownloadResult> results = clipDownloadService.downloadClips(request);
        return ApiResponse.ok(results);
    }

    @Deprecated
    @PostMapping("/clips/download/async")
    public ApiResponse<String> downloadClipsAsync(@RequestBody ClipDownloadRequest request) {
        CompletableFuture<List<ClipDownloadResult>> future = clipDownloadService.downloadClipsAsync(request);
        future.whenComplete((results, ex) -> {
            if (ex != null) {
                return;
            }
            results.stream().filter(ClipDownloadResult::isSuccess).count();
        });
        return ApiResponse.ok("Download task submitted", "async");
    }
}
