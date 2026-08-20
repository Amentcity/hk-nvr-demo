package com.example.hknvr.model;

import lombok.Data;

import java.util.List;

@Data
public class RecordingStartRequest {
    private List<String> cameraIds;
    private String subDir;
}
