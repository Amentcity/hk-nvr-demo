package com.example.hknvr.service;

import com.example.hknvr.config.HikvisionProperties;
import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.model.LivePlayResult;
import com.example.hknvr.util.RtspUrlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实时预览统一改为直接使用 RTSP 流地址。
 */
@Slf4j
@Service
public class LivePreviewService {

    private final HikvisionProperties properties;
    private final CameraService cameraService;
    private final Map<String, Process> activeStreams = new ConcurrentHashMap<>();

    public LivePreviewService(HikvisionProperties properties, CameraService cameraService) {
        this.properties = properties;
        this.cameraService = cameraService;
    }

    public List<LivePlayResult> buildPlayResults(List<String> cameraIds, boolean subStream) {
        if (cameraIds == null || cameraIds.isEmpty()) {
            throw new IllegalArgumentException("cameraIds must not be empty");
        }
        List<LivePlayResult> results = new ArrayList<>();
        for (String cameraId : cameraIds) {
            CameraInfo camera = cameraService.getCameraById(cameraId);
            String rtspUrl = buildRtspStreamUrl(cameraId, subStream);
            results.add(LivePlayResult.builder()
                    .cameraId(cameraId)
                    .cameraName(camera.getName())
                    .channel(camera.getChannel())
                    .rtspUrl(rtspUrl)
                    .streamUrl(rtspUrl)
                    .build());
        }
        log.info("Prepared {} RTSP live stream(s), subStream={}", results.size(), subStream);
        return results;
    }

    public String buildRtspStreamUrl(String cameraId, boolean subStream) {
        CameraInfo camera = cameraService.getCameraById(cameraId);
        return subStream
                ? RtspUrlBuilder.buildSubStreamUrl(properties, camera.getChannel())
                : RtspUrlBuilder.buildMainStreamUrl(properties, camera.getChannel());
    }

    public void stopStreams(List<String> cameraIds) {
        if (cameraIds == null || cameraIds.isEmpty()) {
            return;
        }
        for (String cameraId : cameraIds) {
            stopStream(cameraId);
        }
    }

    public void stopAllStreams() {
        for (String cameraId : activeStreams.keySet()) {
            stopStream(cameraId);
        }
    }

    public List<String> listActiveStreams() {
        return new ArrayList<>(activeStreams.keySet());
    }

    /**
     * 保留兼容接口：已切换为 RTSP 模式，后续前端应直接使用 rtspUrl。 
     */
    public void streamMjpeg(String cameraId, boolean subStream, HttpServletResponse response) throws IOException {
        String rtspUrl = buildRtspStreamUrl(cameraId, subStream);
        response.setContentType("text/plain;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("RTSP mode active. Use rtspUrl: " + rtspUrl);
    }

    public void stopStream(String cameraId) {
        Process process = activeStreams.remove(cameraId);
        if (process != null) {
            process.destroyForcibly();
            log.info("Stopped live stream for camera {}", cameraId);
        }
    }
}
