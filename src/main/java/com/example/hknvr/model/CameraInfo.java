package com.example.hknvr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CameraInfo {

    private String id;

    private String name;

    private int channel;

    private String ip;

    /** 主码流RTSP */
    private String mainStream;

    /** 子码流RTSP */
    private String subStream;

    /** 在线状态 */
    private Boolean online;
}
