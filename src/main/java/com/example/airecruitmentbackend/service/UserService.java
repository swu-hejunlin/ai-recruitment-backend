package com.example.airecruitmentbackend.service;

import com.example.airecruitmentbackend.dto.LoginRequest;
import com.example.airecruitmentbackend.dto.LoginResponse;
import com.example.airecruitmentbackend.dto.SendCodeRequest;
import com.example.airecruitmentbackend.dto.SwitchRoleRequest;

/**
 * 用户服务接口
 * 定义用户登录相关的业务方法
 */
public interface UserService {

    /**
     * 发送验证码
     * 生成6位随机验证码，存入Redis，并模拟发送短信
     *
     * @param request 发送验证码请求
     */
    void sendCode(SendCodeRequest request);

    /**
     * 验证码登录
     * 校验验证码，成功则返回JWT令牌
     * 如果手机号已存在但角色不同，返回needSwitchRole=true，提示前端
     *
     * @param request 登录请求
     * @return 登录响应，包含JWT令牌和用户信息
     */
    LoginResponse login(LoginRequest request);

    /**
     * 身份切换
     * 用户确认切换角色后，修改用户角色并返回新的JWT令牌
     *
     * @param request 身份切换请求
     * @return 登录响应，包含新的JWT令牌和用户信息
     */
    LoginResponse switchRole(SwitchRoleRequest request);
}
