package com.example.hknvr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClipDownloadRequest {
    /** 摄像头 ID 列表，对应 application.yml 中的 cameras.id */
    private List<String> cameraIds;
    /** 开始时间，格式 yyyy-MM-dd HH:mm:ss */
    private String startTime;
    /** 结束时间，格式 yyyy-MM-dd HH:mm:ss */
    private String endTime;
    /** 可选：自定义保存子目录名 */
    private String subDir;
}
