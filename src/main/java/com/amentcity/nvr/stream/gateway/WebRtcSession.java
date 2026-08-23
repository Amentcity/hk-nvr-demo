package com.amentcity.nvr.stream.gateway;

import lombok.Data;

@Data
public class WebRtcSession {
    private String streamId;
    private String url;
    private String status;
}
