package com.amentcity.nvr.stream.scheduler;

import com.amentcity.nvr.stream.cluster.LoadBalanceStrategy;
import com.amentcity.nvr.stream.cluster.StreamNode;
import org.springframework.stereotype.Service;

@Service
public class StreamScheduler {
    private final LoadBalanceStrategy strategy;

    public StreamScheduler(LoadBalanceStrategy strategy){
        this.strategy = strategy;
    }

    public StreamNode allocate(){
        return strategy.select();
    }
}
