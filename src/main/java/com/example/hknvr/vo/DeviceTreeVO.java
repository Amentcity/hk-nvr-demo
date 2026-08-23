package com.example.hknvr.vo;

import java.util.List;

/**
 * 设备树响应VO
 */
public class DeviceTreeVO {

    /** 登录ID */
    private int userId;

    /** 设备名称 */
    private String deviceName;

    /** 设备类型 */
    private String deviceType;

    /** 序列号 */
    private String serialNumber;

    /** 模拟通道数 */
    private int analogChannelCount;

    /** IP通道数 */
    private int ipChannelCount;

    /** 数字通道起始号 */
    private int startDChan;

    /** 零通道数 */
    private int zeroChanCount;

    /** 语音通道数 */
    private int audioChanCount;

    /** 通道列表 */
    private List<ChannelVO> channels;

    public DeviceTreeVO() {
    }

    // ==================== Getter & Setter ====================

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getAnalogChannelCount() {
        return analogChannelCount;
    }

    public void setAnalogChannelCount(int analogChannelCount) {
        this.analogChannelCount = analogChannelCount;
    }

    public int getIpChannelCount() {
        return ipChannelCount;
    }

    public void setIpChannelCount(int ipChannelCount) {
        this.ipChannelCount = ipChannelCount;
    }

    public int getStartDChan() {
        return startDChan;
    }

    public void setStartDChan(int startDChan) {
        this.startDChan = startDChan;
    }

    public int getZeroChanCount() {
        return zeroChanCount;
    }

    public void setZeroChanCount(int zeroChanCount) {
        this.zeroChanCount = zeroChanCount;
    }

    public int getAudioChanCount() {
        return audioChanCount;
    }

    public void setAudioChanCount(int audioChanCount) {
        this.audioChanCount = audioChanCount;
    }

    public List<ChannelVO> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelVO> channels) {
        this.channels = channels;
    }
}