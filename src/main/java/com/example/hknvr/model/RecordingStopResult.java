package com.example.hknvr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingStopResult {
    private String sessionId;
    private String startedAt;
    private String stoppedAt;
    private List<ClipDownloadResult> results;
}
