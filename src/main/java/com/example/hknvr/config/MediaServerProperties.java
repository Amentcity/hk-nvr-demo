package com.example.hknvr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "media.server")
public class MediaServerProperties {
    private String host = "127.0.0.1";
    private int port = 80;
    private String app = "live";
}
