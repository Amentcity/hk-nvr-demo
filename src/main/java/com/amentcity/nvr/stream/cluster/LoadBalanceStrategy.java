package com.amentcity.nvr.stream.cluster;

public interface LoadBalanceStrategy {
    StreamNode select();
}
