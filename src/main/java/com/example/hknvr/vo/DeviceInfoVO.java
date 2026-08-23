package com.example.hknvr.vo;

/**
 * 设备信息响应VO
 */
public class DeviceInfoVO {

    /** 登录ID */
    private int userId;

    /** 设备IP地址 */
    private String ipAddress;

    /** 设备端口 */
    private int port;

    /** 序列号 */
    private String serialNumber;

    /** 设备名称 */
    private String deviceName;

    /** 设备类型 */
    private int deviceType;

    /** 模拟通道数 */
    private int analogChanNum;

    /** IP通道数 */
    private int ipChanNum;

    /** 起始模拟通道号 */
    private int startChan;

    /** 起始数字通道号 */
    private int startDChan;

    /** 零通道数 */
    private int zeroChanNum;

    /** 语音通道数 */
    private int audioChanNum;

    public DeviceInfoVO() {
    }

    // ==================== Getter & Setter ====================

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getAnalogChanNum() {
        return analogChanNum;
    }

    public void setAnalogChanNum(int analogChanNum) {
        this.analogChanNum = analogChanNum;
    }

    public int getIpChanNum() {
        return ipChanNum;
    }

    public void setIpChanNum(int ipChanNum) {
        this.ipChanNum = ipChanNum;
    }

    public int getStartChan() {
        return startChan;
    }

    public void setStartChan(int startChan) {
        this.startChan = startChan;
    }

    public int getStartDChan() {
        return startDChan;
    }

    public void setStartDChan(int startDChan) {
        this.startDChan = startDChan;
    }

    public int getZeroChanNum() {
        return zeroChanNum;
    }

    public void setZeroChanNum(int zeroChanNum) {
        this.zeroChanNum = zeroChanNum;
    }

    public int getAudioChanNum() {
        return audioChanNum;
    }

    public void setAudioChanNum(int audioChanNum) {
        this.audioChanNum = audioChanNum;
    }
}