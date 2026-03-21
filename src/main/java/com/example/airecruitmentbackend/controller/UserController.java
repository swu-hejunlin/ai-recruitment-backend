package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.dto.LoginRequest;
import com.example.airecruitmentbackend.dto.LoginResponse;
import com.example.airecruitmentbackend.dto.SendCodeRequest;
import com.example.airecruitmentbackend.dto.SwitchRoleRequest;
import com.example.airecruitmentbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * 处理用户登录相关的接口请求
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 发送验证码接口
     * 接口地址：POST /api/user/send-code
     *
     * @param request 发送验证码请求（手机号+角色）
     * @return 操作结果
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("收到发送验证码请求，手机号：{}，角色：{}", request.getPhone(), request.getRole());
        userService.sendCode(request);
        return Result.success("验证码发送成功", null);
    }

    /**
     * 验证码登录接口
     * 接口地址：POST /api/user/login
     *
     * @param request 登录请求（手机号+验证码+角色）
     * @return 登录响应（包含JWT令牌和用户信息）
     *         如果需要身份切换，返回needSwitchRole=true
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求，手机号：{}，角色：{}", request.getPhone(), request.getRole());
        LoginResponse response = userService.login(request);

        if (Boolean.TRUE.equals(response.getNeedSwitchRole())) {
            return Result.success("检测到身份冲突，请确认是否切换身份", response);
        } else {
            return Result.success("登录成功", response);
        }
    }

    /**
     * 身份切换接口
     * 接口地址：POST /api/user/switch-role
     *
     * @param request 身份切换请求（手机号+验证码+目标角色）
     * @return 登录响应（包含新的JWT令牌和用户信息）
     */
    @PostMapping("/switch-role")
    public Result<LoginResponse> switchRole(@Valid @RequestBody SwitchRoleRequest request) {
        log.info("收到身份切换请求，手机号：{}，目标角色：{}", request.getPhone(), request.getRole());
        LoginResponse response = userService.switchRole(request);
        return Result.success("身份切换成功", response);
    }
}
