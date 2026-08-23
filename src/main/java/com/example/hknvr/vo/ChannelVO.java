package com.example.hknvr.vo;

/**
 * 通道信息响应VO
 */
public class ChannelVO {

    /** 通道号 */
    private int channelNo;

    /** 通道名称 */
    private String channelName;

    /** 是否启用 */
    private boolean enabled;

    /** 通道类型：ANALOG/IP/ZERO/AUDIO/EXTERNAL */
    private String type;

    /** IP通道的IP地址 */
    private String ipAddress;

    /** IP通道的端口 */
    private int port;

    public ChannelVO() {
    }

    // ==================== Getter & Setter ====================

    public int getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(int channelNo) {
        this.channelNo = channelNo;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}