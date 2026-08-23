package com.amentcity.nvr.stream.ffmpeg;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FfmpegTaskManager {

    private final ConcurrentHashMap<String, Process> tasks = new ConcurrentHashMap<>();

    public void register(String id, Process process){
        tasks.put(id, process);
    }

    public void stop(String id){
        Process process = tasks.get(id);
        if(process != null){
            process.destroy();
        }
    }
}
