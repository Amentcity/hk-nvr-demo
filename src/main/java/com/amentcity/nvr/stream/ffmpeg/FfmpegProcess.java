package com.amentcity.nvr.stream.ffmpeg;

import lombok.Data;

@Data
public class FfmpegProcess {
    private String id;
    private Process process;
    private String status;
}
