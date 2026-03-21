package com.example.airecruitmentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    /**
     * JWT令牌
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户角色：1-求职者，2-企业HR
     */
    private Integer role;

    /**
     * 是否需要身份切换
     * true-手机号已存在但角色不同，需要前端弹窗确认
     * false-正常登录
     */
    private Boolean needSwitchRole;

    /**
     * 当前用户的现有角色
     * 当needSwitchRole为true时，返回用户当前的角色
     */
    private Integer currentRole;
}
