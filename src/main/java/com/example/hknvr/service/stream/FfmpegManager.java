package com.example.hknvr.service.stream;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FfmpegManager {

    private final Map<String, Process> processes = new ConcurrentHashMap<>();

    public void start(String sessionId, String rtspUrl, String streamId) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg",
                "-i", rtspUrl,
                "-c:v", "copy",
                "-an",
                "-f", "flv",
                "rtmp://127.0.0.1/live/" + streamId
        );
        Process process = builder.start();
        processes.put(sessionId, process);
    }

    public void stop(String sessionId) {
        Process process = processes.remove(sessionId);
        if (process != null) {
            process.destroy();
        }
    }

    public boolean running(String sessionId) {
        Process process = processes.get(sessionId);
        return process != null && process.isAlive();
    }
}
