package com.example.airecruitmentbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.entity.Notification;
import com.example.airecruitmentbackend.service.NotificationService;
import com.example.airecruitmentbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    public Result<Page<Notification>> getNotifications(
            HttpServletRequest request,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        Page<Notification> page = notificationService.getNotifications(userId, pageNum, pageSize);
        return Result.success("查询成功", page);
    }

    /**
     * 获取未读通知数量（小红点）
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        Long count = notificationService.getUnreadCount(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return Result.success("查询成功", result);
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/read")
    public Result<Void> markAsRead(HttpServletRequest request,
                                   @RequestParam("id") Long id) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        notificationService.markAsRead(id, userId);
        return Result.success("已标记为已读", null);
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        notificationService.markAllAsRead(userId);
        return Result.success("已全部标记为已读", null);
    }
}
