package com.amentcity.nvr.stream.ffmpeg;

import org.springframework.stereotype.Component;

@Component
public class FfmpegCommandBuilder {

    public String build(String rtsp,String streamId){
        return String.format("ffmpeg -i %s -c:v copy -f webm /stream/%s",rtsp,streamId);
    }
}
