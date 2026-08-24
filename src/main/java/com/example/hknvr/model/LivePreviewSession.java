package com.example.hknvr.model;

import lombok.Data;

@Data
public class LivePreviewSession {
    private String sessionId;
    private String cameraId;
    private String rtspUrl;
    private String streamId;
    private String webrtcUrl;
    private Status status;

    public enum Status {
        CREATED, RUNNING, STOPPED, ERROR
    }
}
