package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知实体类
 */
@Data
@TableName("notification")
public class Notification {
    /**
     * 通知ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收者用户ID
     */
    private Long receiverId;

    /**
     * 通知类型：1-新投递提醒，2-面试状态变更，3-系统公告
     */
    private Integer type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 业务关联ID（如application_id）
     */
    private Long businessId;

    /**
     * 已读状态：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 触发时间
     */
    private LocalDateTime createTime;
}
