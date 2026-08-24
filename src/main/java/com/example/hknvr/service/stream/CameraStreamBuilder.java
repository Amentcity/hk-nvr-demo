package com.example.hknvr.service.stream;

import com.example.hknvr.model.CameraInfo;
import org.springframework.stereotype.Service;

@Service
public class CameraStreamBuilder {

    public String buildMainStream(CameraInfo camera) {
        return String.format(
                "rtsp://%s:%s@%s:554/Streaming/Channels/%d01",
                "user",
                "password",
                camera.getIp(),
                camera.getChannel());
    }

    public String buildSubStream(CameraInfo camera) {
        return String.format(
                "rtsp://%s:%s@%s:554/Streaming/Channels/%d02",
                "user",
                "password",
                camera.getIp(),
                camera.getChannel());
    }
}
