package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.entity.Notification;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.mapper.NotificationMapper;
import com.example.airecruitmentbackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 通知Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public void sendNotification(Long receiverId, Integer type, String title, String content, Long businessId) {
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setBusinessId(businessId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
        log.info("发送通知成功，receiverId：{}，title：{}", receiverId, title);
    }

    @Override
    public Page<Notification> getNotifications(Long receiverId, int pageNum, int pageSize) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        return notificationMapper.selectPage(page, new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, receiverId)
                .orderByDesc(Notification::getCreateTime));
    }

    @Override
    public Long getUnreadCount(Long receiverId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, receiverId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public void markAsRead(Long notificationId, Long receiverId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getReceiverId().equals(receiverId)) {
            throw new ForbiddenException("无权操作此通知");
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
        log.info("标记通知已读，notificationId：{}", notificationId);
    }

    @Override
    public void markAsReadByBusinessId(Long businessId, Long receiverId) {
        Notification notification = new Notification();
        notification.setIsRead(1);
        notificationMapper.update(notification, new LambdaQueryWrapper<Notification>()
                .eq(Notification::getBusinessId, businessId)
                .eq(Notification::getReceiverId, receiverId)
                .eq(Notification::getIsRead, 0));
        log.info("标记业务相关通知已读，businessId：{}", businessId);
    }

    @Override
    public void markAllAsRead(Long receiverId) {
        Notification notification = new Notification();
        notification.setIsRead(1);
        notificationMapper.update(notification, new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, receiverId)
                .eq(Notification::getIsRead, 0));
        log.info("标记所有通知已读，receiverId：{}", receiverId);
    }
}
