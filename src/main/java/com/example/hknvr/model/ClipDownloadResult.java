package com.example.hknvr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClipDownloadResult {
    private String cameraId;
    private String cameraName;
    private int channel;
    private boolean success;
    private String message;
    private String filePath;
}
