package com.amentcity.nvr.stream.service;

import com.amentcity.nvr.stream.vo.StreamInfo;

public interface StreamService {

    StreamInfo start(Long cameraId);

    void stop(String streamId);
}
