package com.example.hknvr.controller;

import com.example.hknvr.model.LivePreviewSession;
import com.example.hknvr.service.stream.LivePreviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/live")
public class LivePreviewController {

    private final LivePreviewService service;

    public LivePreviewController(LivePreviewService service){
        this.service = service;
    }

    @PostMapping("/start")
    public LivePreviewSession start(@RequestParam String cameraId){
        return service.start(cameraId);
    }

    @DeleteMapping("/{sessionId}")
    public boolean stop(@PathVariable String sessionId){
        return service.stop(sessionId);
    }

    @GetMapping("/{sessionId}")
    public LivePreviewSession get(@PathVariable String sessionId){
        return service.get(sessionId);
    }
}
