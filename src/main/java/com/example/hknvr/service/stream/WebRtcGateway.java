package com.example.hknvr.service.stream;

import com.example.hknvr.model.LivePreviewSession;
import com.example.hknvr.model.WebRtcStream;
import org.springframework.stereotype.Service;

@Service
public class WebRtcGateway {

    public WebRtcStream publish(LivePreviewSession session) {
        WebRtcStream stream = new WebRtcStream();
        stream.setStreamId(session.getStreamId());
        stream.setCameraId(session.getCameraId());
        stream.setUrl("webrtc://127.0.0.1/live/" + session.getStreamId());
        stream.setStatus(WebRtcStream.Status.RUNNING);
        return stream;
    }
}
