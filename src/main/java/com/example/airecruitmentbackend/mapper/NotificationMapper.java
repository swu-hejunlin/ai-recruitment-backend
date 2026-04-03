package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通知Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 查询用户通知列表
     */
    Page<Notification> selectByReceiverId(Page<Notification> page, @Param("receiverId") Long receiverId);

    /**
     * 统计未读通知数量
     */
    Long countUnread(@Param("receiverId") Long receiverId);
}
