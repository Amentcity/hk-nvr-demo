package com.example.hknvr.dto;


import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class StreamTestResult {


    private String sessionId;


    private String cameraId;


    private String rtspUrl;


    private String status;

}