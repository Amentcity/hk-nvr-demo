package com.example.hknvr.service;

import com.example.hknvr.sdk.HikSdkManager;
import com.example.hknvr.sdk.HikSdkManager.*;
import com.example.hknvr.service.camera.ChannelCameraConverter;
import com.example.hknvr.service.camera.NvrCameraRegistry;
import com.example.hknvr.model.CameraInfo;
import com.example.hknvr.vo.ChannelVO;
import com.example.hknvr.vo.DeviceInfoVO;
import com.example.hknvr.vo.DeviceTreeVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceTreeService {

    private final HikSdkManager manager;
    private final ChannelCameraConverter converter;
    private final NvrCameraRegistry cameraRegistry;

    private final Map<Integer, DeviceSession> sessionCache = new ConcurrentHashMap<>();

    public DeviceTreeService(
            HikSdkManager manager,
            ChannelCameraConverter converter,
            NvrCameraRegistry cameraRegistry
    ) {
        this.manager = manager;
        this.converter = converter;
        this.cameraRegistry = cameraRegistry;
        this.manager.init();
    }

    public int login(String ipAddress, int port, String username, String password) {
        DeviceSession session = manager.login(ipAddress, port, username, password);
        if (session != null) {
            sessionCache.put(session.loginID, session);
            registerCameras(session);
            return session.loginID;
        }
        return -1;
    }

    private void registerCameras(DeviceSession session) {
        List<ChannelInfo> channels = manager.getAllChannels(session);
        List<CameraInfo> cameras = converter.convert(
                session.ipAddress,
                channels
        );
        cameraRegistry.registerAll(cameras);
    }

    public boolean logout(int userId) {
        DeviceSession session = sessionCache.remove(userId);
        if (session != null) {
            return manager.logout(session.loginID);
        }
        return false;
    }

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

        List<ChannelVO> channelVOs = new ArrayList<>();
        for (ChannelInfo ch : manager.getAllChannels(session)) {
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

    public List<ChannelVO> getChannels(int userId) {
        DeviceSession session = sessionCache.get(userId);
        List<ChannelVO> result = new ArrayList<>();
        if (session == null) {
            return result;
        }
        for (ChannelInfo ch : manager.getAllChannels(session)) {
            ChannelVO vo = new ChannelVO();
            vo.setChannelNo(ch.channelNo);
            vo.setChannelName(ch.channelName);
            vo.setEnabled(ch.enabled);
            vo.setType(ch.type.name());
            vo.setIpAddress(ch.ipAddress);
            vo.setPort(ch.port);
            result.add(vo);
        }
        return result;
    }

    public DeviceInfoVO getDeviceInfo(int userId) {
        DeviceSession session = sessionCache.get(userId);
        if (session == null) return null;
        DeviceInfo info = manager.getDeviceInfo(session);
        if (info == null) return null;
        DeviceInfoVO vo = new DeviceInfoVO();
        vo.setUserId(info.loginID);
        vo.setIpAddress(info.ipAddress);
        vo.setPort(info.port);
        vo.setSerialNumber(info.serialNumber);
        vo.setDeviceName(info.deviceName);
        return vo;
    }

    public int getDeviceCount() {
        return sessionCache.size();
    }

    public void destroy() {
        manager.logoutAll();
        sessionCache.clear();
        manager.cleanupSDK();
    }
}
