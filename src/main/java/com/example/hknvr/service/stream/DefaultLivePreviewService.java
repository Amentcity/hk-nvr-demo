package com.example.hknvr.service.stream;

import com.example.hknvr.model.LivePreviewSession;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultLivePreviewService implements LivePreviewService {

    private final Map<String, LivePreviewSession> sessions = new ConcurrentHashMap<>();

    @Override
    public LivePreviewSession start(String cameraId) {
        LivePreviewSession session = new LivePreviewSession();
        String id = "live-" + UUID.randomUUID();
        session.setSessionId(id);
        session.setCameraId(cameraId);
        session.setStreamId(cameraId);
        session.setStatus(LivePreviewSession.Status.RUNNING);
        sessions.put(id, session);
        return session;
    }

    @Override
    public boolean stop(String sessionId) {
        return sessions.remove(sessionId) != null;
    }

    @Override
    public LivePreviewSession get(String sessionId) {
        return sessions.get(sessionId);
    }
}
