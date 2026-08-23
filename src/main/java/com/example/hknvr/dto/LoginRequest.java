package com.example.hknvr.dto;

/**
 * 设备登录请求DTO
 */
public class LoginRequest {

    /** 设备IP地址 */
    private String ip;

    /** 设备端口（默认8000） */
    private int port = 8000;

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    // ==================== Getter & Setter ====================

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}