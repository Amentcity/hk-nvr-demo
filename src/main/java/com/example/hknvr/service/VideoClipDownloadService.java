package com.example.hknvr.service;

import com.example.hknvr.config.HikvisionProperties;
import com.example.hknvr.model.ClipDownloadRequest;
import com.example.hknvr.model.ClipDownloadResult;
import com.example.hknvr.sdk.HCNetSDK;
import com.example.hknvr.sdk.HikSdkManager;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.example.hknvr.model.RecordingSessionStartResult;
import com.example.hknvr.model.RecordingSessionStatus;
import com.example.hknvr.model.RecordingStartRequest;
import com.example.hknvr.model.RecordingStopRequest;
import com.example.hknvr.model.RecordingStopResult;
import lombok.Getter;

/**
 * 通过海康 SDK 按时间段下载多路摄像头录像片段。
 */
@Slf4j
@Service
public class VideoClipDownloadService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HikSdkManager sdkManager;
    private final DeviceSessionService sessionService;
    private final CameraService cameraService;
    private final HikvisionProperties properties;
    private final FfmpegConvertService ffmpegConvertService;
    private final Map<String, RecordingSession> activeSessions = new ConcurrentHashMap<>();

    public VideoClipDownloadService(HikSdkManager sdkManager,
                                    DeviceSessionService sessionService,
                                    CameraService cameraService,
                                    HikvisionProperties properties,
                                    FfmpegConvertService ffmpegConvertService) {
        this.sdkManager = sdkManager;
        this.sessionService = sessionService;
        this.cameraService = cameraService;
        this.properties = properties;
        this.ffmpegConvertService = ffmpegConvertService;
    }

    public RecordingSessionStartResult startRecording(RecordingStartRequest request) {
        if (request == null || request.getCameraIds() == null || request.getCameraIds().isEmpty()) {
            throw new IllegalArgumentException("cameraIds must not be empty");
        }

        LocalDateTime startedAt = fetchNvrCurrentTime();
        String sessionId = UUID.randomUUID().toString();
        RecordingSession session = new RecordingSession(sessionId, request.getCameraIds(), startedAt, request.getSubDir());
        activeSessions.put(sessionId, session);

        log.info("Started recording session {} for cameras {} at NVR time {}", sessionId, request.getCameraIds(), startedAt);
        return RecordingSessionStartResult.builder()
                .sessionId(sessionId)
                .startedAt(startedAt.format(TIME_FORMAT))
                .cameraIds(new ArrayList<>(request.getCameraIds()))
                .subDir(session.getSubDir())
                .build();
    }

    public RecordingStopResult stopRecording(RecordingStopRequest request) {
        if (request == null || request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId must not be empty");
        }

        RecordingSession session = activeSessions.remove(request.getSessionId());
        if (session == null) {
            throw new IllegalArgumentException("Recording session not found: " + request.getSessionId());
        }

        LocalDateTime stoppedAt = fetchNvrCurrentTime();
        ClipDownloadRequest clipRequest = new ClipDownloadRequest();
        clipRequest.setCameraIds(session.getCameraIds());
        clipRequest.setStartTime(session.getStartedAt().format(TIME_FORMAT));
        clipRequest.setEndTime(stoppedAt.format(TIME_FORMAT));
        clipRequest.setSubDir(session.getSubDir());

        List<ClipDownloadResult> results = downloadClips(clipRequest);

        log.info("Stopped recording session {} at NVR time {} with {} result(s)", session.getSessionId(), stoppedAt, results.size());
        return RecordingStopResult.builder()
                .sessionId(session.getSessionId())
                .startedAt(session.getStartedAt().format(TIME_FORMAT))
                .stoppedAt(stoppedAt.format(TIME_FORMAT))
                .results(results)
                .build();
    }

    public List<RecordingSessionStatus> listActiveRecordings() {
        return activeSessions.values().stream()
                .map(session -> RecordingSessionStatus.builder()
                        .sessionId(session.getSessionId())
                        .startedAt(session.getStartedAt().format(TIME_FORMAT))
                        .cameraIds(new ArrayList<>(session.getCameraIds()))
                        .subDir(session.getSubDir())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    private LocalDateTime fetchNvrCurrentTime() {
        sdkManager.ensureInitialized();
        HCNetSDK sdk = HikSdkManager.getHcNetSDK();
        if (sdk == null) {
            throw new IllegalStateException("Hikvision SDK is not initialized");
        }

        HCNetSDK.NET_DVR_TIME currentTime = new HCNetSDK.NET_DVR_TIME();
        IntByReference bytesReturned = new IntByReference();
        boolean success = sdk.NET_DVR_GetDVRConfig(
                sessionService.getUserId(),
                HCNetSDK.NET_DVR_GET_TIMECFG,
                0,
                currentTime.getPointer(),
                currentTime.size(),
                bytesReturned);

        if (!success) {
            int error = sdk.NET_DVR_GetLastError();
            throw new IllegalStateException("Failed to query NVR time, SDK error=" + error);
        }

        return LocalDateTime.of(
                currentTime.dwYear,
                currentTime.dwMonth,
                currentTime.dwDay,
                currentTime.dwHour,
                currentTime.dwMinute,
                currentTime.dwSecond);
    }

    @Getter
    private static class RecordingSession {
        private final String sessionId;
        private final List<String> cameraIds;
        private final LocalDateTime startedAt;
        private final String subDir;

        private RecordingSession(String sessionId, List<String> cameraIds, LocalDateTime startedAt, String subDir) {
            this.sessionId = sessionId;
            this.cameraIds = new ArrayList<>(cameraIds);
            this.startedAt = startedAt;
            this.subDir = subDir;
        }
    }

    public List<ClipDownloadResult> downloadClips(ClipDownloadRequest request) {
        validateRequest(request);
        LocalDateTime start = parseTime(request.getStartTime()).minusMinutes(1);
        LocalDateTime end = parseTime(request.getEndTime());
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        File saveDir = resolveSaveDir(request.getSubDir());
        int userId = sessionService.getUserId();

        List<ClipDownloadResult> results = new ArrayList<>();
        for (String cameraId : request.getCameraIds()) {
            results.add(downloadSingleClip(userId, cameraId, start, end, saveDir));
        }
        return results;
    }

    @Async("clipDownloadExecutor")
    public CompletableFuture<List<ClipDownloadResult>> downloadClipsAsync(ClipDownloadRequest request) {
        return CompletableFuture.completedFuture(downloadClips(request));
    }

    private ClipDownloadResult downloadSingleClip(int userId, String cameraId,
                                                  LocalDateTime start, LocalDateTime end, File saveDir) {
        HikvisionProperties.CameraConfig camera = cameraService.getCameraById(cameraId);
        String fileName = String.format("%s_ch%d_%s_%s.dav",
                camera.getId(),
                camera.getChannel(),
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        String savePath = new File(saveDir, fileName).getAbsolutePath();

        HCNetSDK sdk = HikSdkManager.getHcNetSDK();
        HCNetSDK.NET_DVR_PLAYCOND cond = new HCNetSDK.NET_DVR_PLAYCOND();
        cond.dwChannel = camera.getChannel();
        fillTime(cond.struStartTime, start);
        fillTime(cond.struStopTime, end);
        cond.write();

        log.info("Downloading clip: camera={}, channel={}, {} -> {}, file={}",
                camera.getName(), camera.getChannel(), start, end, savePath);

        int handle = sdk.NET_DVR_GetFileByTime_V40(userId, savePath, cond);
        if (handle < 0) {
            int err = sdk.NET_DVR_GetLastError();
            return ClipDownloadResult.builder()
                    .cameraId(cameraId)
                    .cameraName(camera.getName())
                    .channel(camera.getChannel())
                    .success(false)
                    .message("Download start failed, SDK error=" + err)
                    .build();
        }

        try {
            if (!sdk.NET_DVR_PlayBackControl(handle, HCNetSDK.NET_DVR_PLAYSTART, 0, null)) {
                int err = sdk.NET_DVR_GetLastError();
                return failResult(camera, "PlayBackControl START failed, error=" + err);
            }

            int progress;
            do {
                Thread.sleep(500);
                IntByReference posRef = new IntByReference(0);
//                sdk.NET_DVR_PlayBackControl(handle, HCNetSDK.NET_DVR_PLAYGETPOS, 0, posRef.getPointer());
                progress = posRef.getValue();
                log.debug("Download progress camera {}: {}%", camera.getName(), progress);
            } while (progress >= 0 && progress < 100);

            File saved = new File(savePath);
            if (!saved.exists() || saved.length() == 0) {
                return failResult(camera, "Download finished but file is empty or missing");
            }

            String finalPath = savePath;
            if (properties.getRecording().isConvertToMp4()) {
                String mp4 = ffmpegConvertService.convertToMp4(savePath);
                if (mp4 != null) {
                    finalPath = mp4;
                }
            }

            return ClipDownloadResult.builder()
                    .cameraId(cameraId)
                    .cameraName(camera.getName())
                    .channel(camera.getChannel())
                    .success(true)
                    .message("Download completed and saved as MP4 when conversion succeeded")
                    .filePath(finalPath)
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return failResult(camera, "Download interrupted");
        } finally {
            sdk.NET_DVR_StopGetFile(handle);
        }
    }

    private ClipDownloadResult failResult(HikvisionProperties.CameraConfig camera, String message) {
        return ClipDownloadResult.builder()
                .cameraId(camera.getId())
                .cameraName(camera.getName())
                .channel(camera.getChannel())
                .success(false)
                .message(message)
                .build();
    }

    private void fillTime(HCNetSDK.NET_DVR_TIME time, LocalDateTime dt) {
        time.dwYear = dt.getYear();
        time.dwMonth = dt.getMonthValue();
        time.dwDay = dt.getDayOfMonth();
        time.dwHour = dt.getHour();
        time.dwMinute = dt.getMinute();
        time.dwSecond = dt.getSecond();
    }

    private File resolveSaveDir(String subDir) {
        File base = new File(properties.getRecording().getSaveDir());
        if (subDir != null && !subDir.trim().isEmpty()) {
            base = new File(base, subDir.trim());
        } else {
            base = new File(base, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        }
        if (!base.exists() && !base.mkdirs()) {
            throw new IllegalStateException("Cannot create save directory: " + base.getAbsolutePath());
        }
        return base;
    }

    private LocalDateTime parseTime(String text) {
        try {
            return LocalDateTime.parse(text.trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format, expected yyyy-MM-dd HH:mm:ss: " + text);
        }
    }

    private void validateRequest(ClipDownloadRequest request) {
        sdkManager.ensureInitialized();
        if (request.getCameraIds() == null || request.getCameraIds().isEmpty()) {
            throw new IllegalArgumentException("cameraIds must not be empty");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("startTime and endTime are required");
        }
    }
}
