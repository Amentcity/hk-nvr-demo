package com.example.hknvr.model;

import lombok.Data;

@Data
public class WebRtcStream {

    private String streamId;
    private String cameraId;
    private String url;
    private Status status;

    public enum Status {
        READY, RUNNING, ERROR
    }
}
