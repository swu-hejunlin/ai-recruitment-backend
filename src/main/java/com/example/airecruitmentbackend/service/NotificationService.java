package com.example.airecruitmentbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.entity.Notification;

/**
 * 通知Service接口
 */
public interface NotificationService {

    /**
     * 发送通知
     */
    void sendNotification(Long receiverId, Integer type, String title, String content, Long businessId);

    /**
     * 查询通知列表
     */
    Page<Notification> getNotifications(Long receiverId, int pageNum, int pageSize);

    /**
     * 获取未读通知数量
     */
    Long getUnreadCount(Long receiverId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId, Long receiverId);

    /**
     * 标记业务相关的通知为已读
     */
    void markAsReadByBusinessId(Long businessId, Long receiverId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long receiverId);
}
