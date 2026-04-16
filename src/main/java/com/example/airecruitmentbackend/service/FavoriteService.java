package com.example.airecruitmentbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.airecruitmentbackend.entity.Favorite;

import java.util.List;
import java.util.Map;

/**
 * 收藏服务接口
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * 添加收藏
     * @param userId 用户ID
     * @param targetType 收藏类型：1-职位，2-企业，3-求职者
     * @param targetId 目标ID
     * @return 是否成功
     */
    boolean addFavorite(Long userId, Integer targetType, Long targetId);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param targetType 收藏类型：1-职位，2-企业，3-求职者
     * @param targetId 目标ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Integer targetType, Long targetId);

    /**
     * 获取用户的收藏列表
     * @param userId 用户ID
     * @param targetType 收藏类型：1-职位，2-企业，3-求职者
     * @return 收藏列表（包含详细信息）
     */
    List<Map<String, Object>> getFavorites(Long userId, Integer targetType);

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param targetType 收藏类型：1-职位，2-企业，3-求职者
     * @param targetId 目标ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long userId, Integer targetType, Long targetId);
}
