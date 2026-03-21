package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 存储求职者和企业HR的基本信息
 */
@Data
@TableName("user")
public class User {
    /**
     * 用户ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 手机号（唯一标识）
     */
    private String phone;

    /**
     * 用户角色：1-求职者，2-企业HR
     */
    private Integer role;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
