package com.example.airecruitmentbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.entity.Position;

import java.util.List;

/**
 * 职位Service接口
 */
public interface PositionService {

    /**
     * 发布职位
     */
    void addPosition(Position position);

    /**
     * 更新职位
     */
    void updatePosition(Position position);

    /**
     * 删除职位
     */
    void deletePosition(Long id, Long bossId);

    /**
     * 根据ID查询职位（带企业信息）
     */
    Position getPositionById(Long id);

    /**
     * Boss查询自己发布的所有职位
     */
    Page<Position> getPositionsByBoss(Long bossId, int pageNum, int pageSize);

    /**
     * 求职者查询职位列表（只显示招聘中的，支持筛选）
     */
    Page<Position> getActivePositions(String title, String city, String category, int pageNum, int pageSize);

    /**
     * 关闭职位
     */
    void closePosition(Long id, Long bossId);

    /**
     * 开启职位
     */
    void openPosition(Long id, Long bossId);

    /**
     * 根据公司ID查询职位列表
     */
    List<Position> getPositionsByCompanyId(Long companyId);

    /**
     * 获取最新职位列表（首页推荐）
     *
     * @param limit 返回数量限制
     * @return 最新职位列表
     */
    List<Position> getLatestPositions(int limit);

    /**
     * 获取热门职位列表（首页推荐）
     * 按投递数量倒序返回
     *
     * @param limit 返回数量限制
     * @return 热门职位列表
     */
    List<Position> getHotPositions(int limit);
}
