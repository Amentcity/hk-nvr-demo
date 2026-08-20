package com.example.hknvr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivePlayResult {
    private String cameraId;
    private String cameraName;
    private int channel;
    /** 实时播放 RTSP 地址，适合前端播放器直接使用 */
    private String rtspUrl;
    /** 兼容旧字段：保持与 RTSP 地址一致 */
    private String streamUrl;
}
