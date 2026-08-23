package com.example.hknvr.service;

import com.example.hknvr.sdk.DeviceTreeManager;
import com.example.hknvr.sdk.DeviceTreeManager.ChannelInfo;
import com.example.hknvr.sdk.DeviceTreeManager.DeviceInfo;
import com.example.hknvr.sdk.DeviceTreeManager.DeviceSession;
import com.example.hknvr.vo.ChannelVO;
import com.example.hknvr.vo.DeviceInfoVO;
import com.example.hknvr.vo.DeviceTreeVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备树业务逻辑服务
 * 内部持有 DeviceTreeManager 实例，封装SDK调用并提供业务级方法
 */
@Service
public class DeviceTreeService {

    /** 设备树管理器（SDK封装层） */
    private final DeviceTreeManager manager;

    /** 设备会话缓存：userId -> DeviceSession */
    private final java.util.Map<Integer, DeviceSession> sessionCache = new java.util.concurrent.ConcurrentHashMap<>();

    public DeviceTreeService() {
        this.manager = new DeviceTreeManager();
        // 自动初始化SDK
        this.manager.initSDK();
    }

    /**
     * 登录设备
     * @param ipAddress 设备IP地址
     * @param port 设备端口
     * @param username 用户名
     * @param password 密码
     * @return 登录ID，失败返回-1
     */
    public int login(String ipAddress, int port, String username, String password) {
        DeviceSession session = manager.login(ipAddress, port, username, password);
        if (session != null) {
            sessionCache.put(session.loginID, session);
            return session.loginID;
        }
        return -1;
    }

    /**
     * 注销设备
     * @param userId 登录ID
     * @return 是否成功
     */
    public boolean logout(int userId) {
        DeviceSession session = sessionCache.remove(userId);
        if (session != null) {
            return manager.logout(session.loginID);
        }
        return false;
    }

    /**
     * 获取设备树（含所有通道信息）
     * @param userId 登录ID
     * @return 设备树VO
     */
    public DeviceTreeVO getDeviceTree(int userId) {
        DeviceSession session = sessionCache.get(userId);
        if (session == null) {
            return null;
        }

        DeviceInfo devInfo = manager.getDeviceInfo(session);
        if (devInfo == null) {
            return null;
        }

        DeviceTreeVO vo = new DeviceTreeVO();
        vo.setUserId(userId);
        vo.setDeviceName(devInfo.deviceName);
        vo.setSerialNumber(devInfo.serialNumber);
        vo.setAnalogChannelCount(devInfo.analogChanNum);
        vo.setIpChannelCount(devInfo.ipChanNum);
        vo.setStartDChan(devInfo.startDChan);
        vo.setZeroChanCount(devInfo.zeroChanNum);
        vo.setAudioChanCount(devInfo.audioChanNum);

        // 设备类型描述
        vo.setDeviceType(getDeviceTypeDescription(devInfo.deviceType));

        // 获取所有通道
        List<ChannelInfo> channels = manager.getAllChannels(session);
        List<ChannelVO> channelVOs = new ArrayList<>();
        for (ChannelInfo ch : channels) {
            ChannelVO channelVO = new ChannelVO();
            channelVO.setChannelNo(ch.channelNo);
            channelVO.setChannelName(ch.channelName);
            channelVO.setEnabled(ch.enabled);
            channelVO.setType(ch.type.name());
            channelVO.setIpAddress(ch.ipAddress);
            channelVO.setPort(ch.port);
            channelVOs.add(channelVO);
        }
        vo.setChannels(channelVOs);

        return vo;
    }

    /**
     * 获取通道列表
     * @param userId 登录ID
     * @return 通道VO列表
     */
    public List<ChannelVO> getChannels(int userId) {
        DeviceSession session = sessionCache.get(userId);
        if (session == null) {
            return new ArrayList<>();
        }

        List<ChannelInfo> channels = manager.getAllChannels(session);
        List<ChannelVO> channelVOs = new ArrayList<>();
        for (ChannelInfo ch : channels) {
            ChannelVO channelVO = new ChannelVO();
            channelVO.setChannelNo(ch.channelNo);
            channelVO.setChannelName(ch.channelName);
            channelVO.setEnabled(ch.enabled);
            channelVO.setType(ch.type.name());
            channelVO.setIpAddress(ch.ipAddress);
            channelVO.setPort(ch.port);
            channelVOs.add(channelVO);
        }
        return channelVOs;
    }

    /**
     * 获取设备详细信息
     * @param userId 登录ID
     * @return 设备信息VO
     */
    public DeviceInfoVO getDeviceInfo(int userId) {
        DeviceSession session = sessionCache.get(userId);
        if (session == null) {
            return null;
        }

        DeviceInfo devInfo = manager.getDeviceInfo(session);
        if (devInfo == null) {
            return null;
        }

        DeviceInfoVO vo = new DeviceInfoVO();
        vo.setUserId(devInfo.loginID);
        vo.setIpAddress(devInfo.ipAddress);
        vo.setPort(devInfo.port);
        vo.setSerialNumber(devInfo.serialNumber);
        vo.setDeviceName(devInfo.deviceName);
        vo.setDeviceType(devInfo.deviceType);
        vo.setAnalogChanNum(devInfo.analogChanNum);
        vo.setIpChanNum(devInfo.ipChanNum);
        vo.setStartChan(devInfo.startChan);
        vo.setStartDChan(devInfo.startDChan);
        vo.setZeroChanNum(devInfo.zeroChanNum);
        vo.setAudioChanNum(devInfo.audioChanNum);
        return vo;
    }

    /**
     * 获取已登录设备数量
     */
    public int getDeviceCount() {
        return sessionCache.size();
    }

    /**
     * 清理资源
     */
    public void destroy() {
        manager.logoutAll();
        sessionCache.clear();
        manager.cleanupSDK();
    }

    /**
     * 根据设备类型代码获取描述
     */
    /**
     * 根据设备类型代码获取描述
     */
    private String getDeviceTypeDescription(int deviceType) {
        switch (deviceType) {
            case 1: return "DVR";
            case 2: return "DVDVR";
            case 3: return "DVR1000";
            case 4: return "HIK DVR";
            case 5: return "NVR";
            case 6: return "IPC";
            case 7: return "HVR";
            case 8: return "CVR";
            case 9: return "AHD DVR";
            case 10: return "HDCCTV DVR";
            case 11: return "HDSDI DVR";
            case 12: return "HD-CVI DVR";
            case 13: return "IVS DVR";
            case 14:
            case 21: return "NVR5000";
            case 15: return "NVR2000";
            case 16: return "NVR4000";
            case 17: return "NVR6000";
            case 18: return "NVR8000";
            case 19: return "NVR1000";
            case 20: return "NVR3000";
            default: return String.format("Unknown(%d)", deviceType);
        }
    }
}