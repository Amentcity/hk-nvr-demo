package com.example.hknvr.controller;

import com.example.hknvr.common.Result;
import com.example.hknvr.dto.DeviceConfigRequest;
import com.example.hknvr.dto.LoginRequest;
import com.example.hknvr.service.DeviceTreeService;
import com.example.hknvr.vo.ChannelVO;
import com.example.hknvr.vo.DeviceInfoVO;
import com.example.hknvr.vo.DeviceTreeVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备树 REST 控制器
 * 提供设备登录、注销、获取设备树/通道列表等接口
 */
@RestController
@RequestMapping("/api/device")
public class DeviceController {

    /** 设备树服务（由Spring容器注入） */
    private final DeviceTreeService deviceTreeService;

    public DeviceController(DeviceTreeService deviceTreeService) {
        this.deviceTreeService = deviceTreeService;
    }

    /**
     * 登录设备
     * POST /api/device/login
     *
     * @param request 登录请求（ip, port, username, password）
     * @return 登录结果，包含 userId
     */
    @PostMapping("/login")
    public Result<Integer> login(@RequestBody LoginRequest request) {
        // 参数校验
        if (request.getIp() == null || request.getIp().trim().isEmpty()) {
            return Result.error(400, "设备IP地址不能为空");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }

        int userId = deviceTreeService.login(
                request.getIp().trim(),
                request.getPort(),
                request.getUsername().trim(),
                request.getPassword()
        );

        if (userId < 0) {
            return Result.error(500, "设备登录失败，请检查设备地址和账号密码");
        }

        return Result.success(userId);
    }

    /**
     * 注销设备
     * POST /api/device/logout
     *
     * @param request 注销请求（userId）
     * @return 注销结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody DeviceConfigRequest request) {
        if (request.getUserId() <= 0) {
            return Result.error(400, "userId无效");
        }

        boolean result = deviceTreeService.logout(request.getUserId());
        if (result) {
            return Result.success();
        }
        return Result.error(500, "设备注销失败，请确认userId是否正确");
    }

    /**
     * 获取设备树（含所有通道信息）
     * GET /api/device/tree/{userId}
     *
     * @param userId 登录ID
     * @return 设备树VO
     */
    @GetMapping("/tree/{userId}")
    public Result<DeviceTreeVO> getDeviceTree(@PathVariable("userId") int userId) {
        if (userId <= 0) {
            return Result.error(400, "userId无效");
        }

        DeviceTreeVO tree = deviceTreeService.getDeviceTree(userId);
        if (tree == null) {
            return Result.error(404, "设备会话不存在或已过期");
        }

        return Result.success(tree);
    }

    /**
     * 获取通道列表
     * GET /api/device/channels/{userId}
     *
     * @param userId 登录ID
     * @return 通道VO列表
     */
    @GetMapping("/channels/{userId}")
    public Result<List<ChannelVO>> getChannels(@PathVariable("userId") int userId) {
        if (userId <= 0) {
            return Result.error(400, "userId无效");
        }

        List<ChannelVO> channels = deviceTreeService.getChannels(userId);
        return Result.success(channels);
    }

    /**
     * 获取设备详细信息
     * GET /api/device/info/{userId}
     *
     * @param userId 登录ID
     * @return 设备信息VO
     */
    @GetMapping("/info/{userId}")
    public Result<DeviceInfoVO> getDeviceInfo(@PathVariable("userId") int userId) {
        if (userId <= 0) {
            return Result.error(400, "userId无效");
        }

        DeviceInfoVO info = deviceTreeService.getDeviceInfo(userId);
        if (info == null) {
            return Result.error(404, "设备会话不存在或已过期");
        }

        return Result.success(info);
    }
}