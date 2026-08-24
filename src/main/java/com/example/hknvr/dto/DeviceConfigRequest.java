package com.example.hknvr.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 设备配置请求DTO
 */
@Setter
@Getter
public class DeviceConfigRequest {

    /** 登录ID（通过登录接口获取） */
    private int userId;

    public DeviceConfigRequest() {
    }

    public DeviceConfigRequest(int userId) {
        this.userId = userId;
    }

}