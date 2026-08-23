package com.amentcity.nvr.stream.scheduler;

import org.springframework.stereotype.Service;

@Service
public class FfmpegTaskScheduler {

    public void start(String streamId){
        // allocate node
        // start ffmpeg process
        // register webrtc session
        // update stream status
    }

    public void stop(String streamId){
        // stop ffmpeg process
    }
}
