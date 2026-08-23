package com.example.hknvr.dto;

/**
 * 设备配置请求DTO
 */
public class DeviceConfigRequest {

    /** 登录ID（通过登录接口获取） */
    private int userId;

    public DeviceConfigRequest() {
    }

    public DeviceConfigRequest(int userId) {
        this.userId = userId;
    }

    // ==================== Getter & Setter ====================

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}