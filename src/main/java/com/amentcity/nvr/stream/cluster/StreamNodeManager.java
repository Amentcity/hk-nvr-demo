package com.amentcity.nvr.stream.cluster;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class StreamNodeManager {
    private final List<StreamNode> nodes = new ArrayList<>();

    public void register(StreamNode node){
        nodes.add(node);
    }

    public StreamNode chooseNode(){
        return nodes.stream()
                .filter(StreamNode::isOnline)
                .min(Comparator.comparing(StreamNode::getCurrentStreams))
                .orElse(null);
    }
}
