package com.example.hknvr.controller;


import com.example.hknvr.dto.StreamTestResult;
import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.service.camera.NvrCameraRegistry;
import com.example.hknvr.service.stream.CameraStreamBuilder;
import com.example.hknvr.service.stream.FfmpegManager;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/test")
public class StreamTestController {


    private final NvrCameraRegistry cameraRegistry;


    private final CameraStreamBuilder streamBuilder;


    private final FfmpegManager ffmpegManager;



    public StreamTestController(
            NvrCameraRegistry cameraRegistry,
            CameraStreamBuilder streamBuilder,
            FfmpegManager ffmpegManager
    ){

        this.cameraRegistry = cameraRegistry;
        this.streamBuilder = streamBuilder;
        this.ffmpegManager = ffmpegManager;

    }



    @PostMapping("/stream/{cameraId}")
    public StreamTestResult testStream(
            @PathVariable String cameraId
    ) throws Exception {


        CameraInfo camera =
                cameraRegistry.get(cameraId);



        if(camera == null){

            throw new RuntimeException(
                    "camera not found:"
                            + cameraId
            );
        }



        String rtspUrl =
                streamBuilder.buildMainStream(
                        camera
                );



        String sessionId =
                "test-" + cameraId;



        ffmpegManager.start(
                sessionId,
                rtspUrl,
                cameraId
        );



        return new StreamTestResult(
                sessionId,
                cameraId,
                rtspUrl,
                "STARTED"
        );

    }

}