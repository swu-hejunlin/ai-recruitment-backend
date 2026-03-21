package com.example.airecruitmentbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送验证码请求DTO
 */
@Data
public class SendCodeRequest {
    /**
     * 手机号
     * 格式：1开头，11位数字
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 用户角色：1-求职者，2-企业HR
     * 用于判断是否需要身份切换提示
     */
    private Integer role;
}
