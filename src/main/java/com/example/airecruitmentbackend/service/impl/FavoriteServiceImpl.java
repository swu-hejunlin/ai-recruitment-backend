package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.entity.Favorite;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.FavoriteMapper;
import com.example.airecruitmentbackend.mapper.PositionMapper;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
import com.example.airecruitmentbackend.mapper.JobSeekerMapper;
import com.example.airecruitmentbackend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final PositionMapper positionMapper;
    private final CompanyMapper companyMapper;
    private final JobSeekerMapper jobSeekerMapper;

    @Override
    public boolean addFavorite(Long userId, Integer targetType, Long targetId) {
        // 验证收藏类型
        if (!List.of(1, 2, 3).contains(targetType)) {
            throw new BusinessException(400, "无效的收藏类型");
        }

        // 检查是否已收藏
        if (isFavorite(userId, targetType, targetId)) {
            throw new BusinessException(400, "已经收藏过了");
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);

        int result = favoriteMapper.insert(favorite);
        log.info("添加收藏成功：userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        return result > 0;
    }

    @Override
    public boolean removeFavorite(Long userId, Integer targetType, Long targetId) {
        // 验证收藏类型
        if (!List.of(1, 2, 3).contains(targetType)) {
            throw new BusinessException(400, "无效的收藏类型");
        }

        // 删除收藏记录
        int result = favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId));

        log.info("取消收藏成功：userId={}, targetType={}, targetId={}", userId, targetType, targetId);
        return result > 0;
    }

    @Override
    public List<Map<String, Object>> getFavorites(Long userId, Integer targetType) {
        // 验证收藏类型
        if (!List.of(1, 2, 3).contains(targetType)) {
            throw new BusinessException(400, "无效的收藏类型");
        }

        // 获取收藏记录
        List<Favorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .orderByDesc(Favorite::getCreateTime));

        // 构建返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Favorite favorite : favorites) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", favorite.getId());
            item.put("createTime", favorite.getCreateTime());

            switch (targetType) {
                case 1: // 职位收藏
                    Position position = positionMapper.selectById(favorite.getTargetId());
                    if (position != null) {
                        item.put("positionId", position.getId());
                        item.put("positionTitle", position.getTitle());
                        item.put("salaryMin", position.getSalaryMin());
                        item.put("salaryMax", position.getSalaryMax());
                        item.put("city", position.getCity());
                        
                        // 获取公司信息
                        Company company = companyMapper.selectById(position.getCompanyId());
                        if (company != null) {
                            item.put("companyName", company.getCompanyName());
                        }
                    }
                    break;
                case 2: // 公司收藏
                    Company company = companyMapper.selectById(favorite.getTargetId());
                    if (company != null) {
                        item.put("companyId", company.getId());
                        item.put("companyName", company.getCompanyName());
                        item.put("industry", company.getIndustry());
                        item.put("scale", company.getScale());
                    }
                    break;
                case 3: // 求职者收藏
                    JobSeeker jobSeeker = jobSeekerMapper.selectById(favorite.getTargetId());
                    if (jobSeeker != null) {
                        item.put("jobSeekerId", jobSeeker.getId());
                        item.put("jobSeekerName", jobSeeker.getName());
                        item.put("gender", jobSeeker.getGender());
                        item.put("workYears", jobSeeker.getWorkYears());
                    }
                    break;
            }

            result.add(item);
        }

        return result;
    }

    @Override
    public boolean isFavorite(Long userId, Integer targetType, Long targetId) {
        // 验证收藏类型
        if (!List.of(1, 2, 3).contains(targetType)) {
            throw new BusinessException(400, "无效的收藏类型");
        }

        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId)) > 0;
    }
}
