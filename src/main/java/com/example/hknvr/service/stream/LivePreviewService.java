package com.example.hknvr.service.stream;

import com.example.hknvr.model.LivePreviewSession;

public interface LivePreviewService {
    LivePreviewSession start(String cameraId);
    boolean stop(String sessionId);
    LivePreviewSession get(String sessionId);
}
