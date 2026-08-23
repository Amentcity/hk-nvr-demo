package com.amentcity.nvr.stream.cluster;

import lombok.Data;

@Data
public class StreamNode {
    private String id;
    private String host;
    private Integer maxStreams;
    private Integer currentStreams;
    private boolean online;
}
