package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.entity.Notification;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.mapper.NotificationMapper;
import com.example.airecruitmentbackend.service.NotificationService;
import com.example.airecruitmentbackend.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketHandler webSocketHandler;

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

        Long unreadCount = getUnreadCount(receiverId);
        String wsMessage = String.format("{\"type\":\"notification\",\"unreadCount\":%d}", unreadCount);
        webSocketHandler.sendNotification(receiverId, wsMessage);

        log.info("发送通知成功，receiverId：{}，type：{}，title：{}", receiverId, type, title);
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
        if (notification.getIsRead() == 1) {
            return;
        }
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, notificationId)
                .set(Notification::getIsRead, 1));
        log.info("标记通知已读，notificationId：{}", notificationId);
    }

    @Override
    public void markAsReadByBusinessId(Long businessId, Long receiverId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getBusinessId, businessId)
                .eq(Notification::getReceiverId, receiverId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
        log.info("标记业务相关通知已读，businessId：{}，receiverId：{}", businessId, receiverId);
    }

    @Override
    public void markAllAsRead(Long receiverId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getReceiverId, receiverId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
        log.info("标记所有通知已读，receiverId：{}", receiverId);
    }
}
