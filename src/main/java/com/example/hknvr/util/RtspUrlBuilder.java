package com.example.hknvr.util;

import com.example.hknvr.config.HikvisionProperties;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ???? RTSP ?????
 * ????Channels/{channel}01?????Channels/{channel}02
 */
public final class RtspUrlBuilder {

    private RtspUrlBuilder() {
    }

    public static String buildMainStreamUrl(HikvisionProperties properties, int channel) {
        return buildStreamUrl(properties, channel, "01");
    }

    public static String buildSubStreamUrl(HikvisionProperties properties, int channel) {
        return buildStreamUrl(properties, channel, "02");
    }

    private static String buildStreamUrl(HikvisionProperties properties, int channel, String streamSuffix) {
        HikvisionProperties.Device device = properties.getDevice();
        int rtspChannel = channel * 100 + Integer.parseInt(streamSuffix);
        String user = urlEncode(device.getUsername());
        String pass = urlEncode(device.getPassword());
        String protocol = String.valueOf(new char[] {'r', 't', 's', 'p', ':', '/', '/'});
        return String.format("%s%s:%s@%s:%d/Streaming/Channels/%d",
                protocol, user, pass, device.getIp(), device.getRtspPort(), rtspChannel);
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
