package com.amentcity.nvr.stream.cluster;

import org.springframework.stereotype.Component;

@Component
public class LeastStreamStrategy implements LoadBalanceStrategy {

    private final StreamNodeManager manager;

    public LeastStreamStrategy(StreamNodeManager manager){
        this.manager = manager;
    }

    @Override
    public StreamNode select(){
        return manager.chooseNode();
    }
}
