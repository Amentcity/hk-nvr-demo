package com.example.hknvr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "hikvision")
public class HikvisionProperties {

    private Device device = new Device();
    private Sdk sdk = new Sdk();
    private Recording recording = new Recording();
    private Ffmpeg ffmpeg = new Ffmpeg();
    private List<CameraConfig> cameras = new ArrayList<>();

    @Data
    public static class Device {
        private String ip;
        private int port = 8000;
        private String username;
        private String password;
        private int rtspPort = 554;
    }

    @Data
    public static class Sdk {
        private String libPath;
        private String logPath;
        private int connectTimeoutMs = 3000;
        private int reconnectIntervalMs = 10000;
    }

    @Data
    public static class Recording {
        private String saveDir;
        private boolean convertToMp4 = true;
    }

    @Data
    public static class Ffmpeg {
        private String path;
    }

    @Data
    public static class CameraConfig {
        private String id;
        private String name;
        /** NVR 通道号，从 1 开始 */
        private String channel;
    }
}
