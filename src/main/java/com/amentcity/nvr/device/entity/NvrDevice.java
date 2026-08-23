package com.amentcity.nvr.device.entity;

import lombok.Data;

@Data
public class NvrDevice {
    private Long id;
    private String name;
    private String ip;
    private Integer port;
    private String username;
    private String password;
    private Integer status;
    private String serial;
}
