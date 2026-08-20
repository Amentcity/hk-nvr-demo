package com.example.hknvr.model;

import lombok.Data;

import java.util.List;

@Data
public class LivePlayRequest {
    /** 摄像头 ID 列表，对应 application.yml 中的 cameras.id */
    private List<String> cameraIds;
    /** 是否使用子码流（默认 true，带宽占用更低） */
    private Boolean substream;
}
