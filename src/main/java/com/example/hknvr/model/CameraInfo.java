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
}
