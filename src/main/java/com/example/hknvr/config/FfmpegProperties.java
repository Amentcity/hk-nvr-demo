package com.example.hknvr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ffmpeg")
public class FfmpegProperties {
    private String path;
    private int maxProcess = 32;
}
