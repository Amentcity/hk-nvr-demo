package com.amentcity.nvr.stream.gateway;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebRtcGateway {

    private final ConcurrentHashMap<String, WebRtcSession> sessions = new ConcurrentHashMap<>();

    public WebRtcSession createSession(String streamId){
        WebRtcSession session = new WebRtcSession();
        session.setStreamId(streamId);
        session.setStatus("CREATED");
        sessions.put(streamId, session);
        return session;
    }

    public WebRtcSession getSession(String streamId){
        return sessions.get(streamId);
    }

    public void close(String streamId){
        sessions.remove(streamId);
    }
}
