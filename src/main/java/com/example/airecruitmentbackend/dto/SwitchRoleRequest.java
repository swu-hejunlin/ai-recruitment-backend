package com.example.airecruitmentbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 身份切换请求DTO
 */
@Data
public class SwitchRoleRequest {
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String code;

    /**
     * 目标角色：1-求职者，2-企业HR
     */
    @NotNull(message = "角色不能为空")
    private Integer role;
}
