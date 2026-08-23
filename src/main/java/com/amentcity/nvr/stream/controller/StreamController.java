package com.amentcity.nvr.stream.controller;

import com.amentcity.nvr.common.result.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stream")
public class StreamController {

    @PostMapping("/start/{cameraId}")
    public Result<?> start(@PathVariable Long cameraId){
        return Result.success("webrtc://live/" + cameraId);
    }

    @PostMapping("/stop/{streamId}")
    public Result<?> stop(@PathVariable String streamId){
        return Result.success(true);
    }
}
