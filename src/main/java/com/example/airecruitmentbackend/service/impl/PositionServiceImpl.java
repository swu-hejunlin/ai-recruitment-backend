package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
import com.example.airecruitmentbackend.mapper.PositionMapper;
import com.example.airecruitmentbackend.service.JobProfileService;
import com.example.airecruitmentbackend.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 职位Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionMapper positionMapper;
    private final CompanyMapper companyMapper;
    private final JobProfileService jobProfileService;

    @Override
    public void addPosition(Position position) {
        // 校验薪资范围
        validateSalary(position);
        // 校验企业信息
        Company company = companyMapper.selectById(position.getCompanyId());
        if (company == null) {
            throw new BusinessException("企业信息不存在");
        }
        // 填充冗余字段，方便前端展示
        position.setCompanyLogo(company.getLogo());
        position.setCompanyName(company.getCompanyName());
        // 默认状态为招聘中
        if (position.getStatus() == null) {
            position.setStatus(1);
        }
        positionMapper.insert(position);
        log.info("发布职位成功，id：{}，title：{}", position.getId(), position.getTitle());
        
        // 异步生成岗位画像
        try {
            log.info("开始异步生成岗位画像，职位ID：{}", position.getId());
            jobProfileService.generateJobProfileAsync(position.getId())
                .thenAccept(response -> {
                    log.info("岗位画像异步生成成功，职位ID：{}", position.getId());
                })
                .exceptionally(ex -> {
                    log.error("岗位画像异步生成失败，职位ID：{}", position.getId(), ex);
                    return null;
                });
        } catch (Exception e) {
            log.error("启动岗位画像异步生成失败，职位ID：{}", position.getId(), e);
            // 异步生成失败不影响职位发布
        }
    }

    @Override
    public void updatePosition(Position position) {
        // 校验薪资范围
        validateSalary(position);
        Position existing = positionMapper.selectById(position.getId());
        if (existing == null) {
            throw new BusinessException("职位不存在");
        }
        // 如果企业信息变更，同步更新冗余字段
        if (!existing.getCompanyId().equals(position.getCompanyId())) {
            Company company = companyMapper.selectById(position.getCompanyId());
            if (company != null) {
                position.setCompanyLogo(company.getLogo());
                position.setCompanyName(company.getCompanyName());
            }
        }
        positionMapper.updateById(position);
        log.info("更新职位成功，id：{}", position.getId());
        
        // 异步更新岗位画像
        try {
            log.info("开始异步更新岗位画像，职位ID：{}", position.getId());
            jobProfileService.updateJobProfileAsync(position.getId())
                .thenAccept(response -> {
                    log.info("岗位画像异步更新成功，职位ID：{}", position.getId());
                })
                .exceptionally(ex -> {
                    log.error("岗位画像异步更新失败，职位ID：{}", position.getId(), ex);
                    return null;
                });
        } catch (Exception e) {
            log.error("启动岗位画像异步更新失败，职位ID：{}", position.getId(), e);
            // 异步更新失败不影响职位更新
        }
    }

    @Override
    public void deletePosition(Long id, Long bossId) {
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        if (!position.getBossId().equals(bossId)) {
            throw new ForbiddenException("无权删除其他人的职位");
        }
        
        // 删除岗位画像
        try {
            log.info("开始删除岗位画像，职位ID：{}", id);
            jobProfileService.deleteJobProfile(id);
            log.info("岗位画像删除成功，职位ID：{}", id);
        } catch (Exception e) {
            log.error("删除岗位画像失败，职位ID：{}", id, e);
            // 画像删除失败不影响职位删除
        }
        
        positionMapper.deleteById(id);
        log.info("删除职位成功，id：{}", id);
    }

    @Override
    public Position getPositionById(Long id) {
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        // 级联查询企业信息
        enrichCompanyInfo(position);
        return position;
    }

    @Override
    public Page<Position> getPositionsByBoss(Long bossId, int pageNum, int pageSize) {
        Page<Position> page = new Page<>(pageNum, pageSize);
        Page<Position> result = positionMapper.selectPage(page, new LambdaQueryWrapper<Position>()
                .eq(Position::getBossId, bossId)
                .orderByDesc(Position::getCreateTime));
        // 补充企业信息
        for (Position p : result.getRecords()) {
            enrichCompanyInfo(p);
        }
        return result;
    }

    @Override
    public Page<Position> getActivePositions(String title, String city, String category, Integer workYearsMin, Integer educationMin, Integer salaryMin, Integer salaryMax, String searchType, int pageNum, int pageSize) {
        Page<Position> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        // 只查询招聘中的职位
        wrapper.eq(Position::getStatus, 1);
        
        // 多字段联合搜索
        if (StringUtils.hasText(title)) {
            if (StringUtils.hasText(searchType)) {
                String[] types = searchType.split(",");
                wrapper.and(w -> {
                    for (String type : types) {
                        switch (type) {
                            case "title":
                                w.or().like(Position::getTitle, title);
                                break;
                            case "description":
                                w.or().like(Position::getDescription, title);
                                break;
                            case "requirement":
                                w.or().like(Position::getRequirement, title);
                                break;
                            default:
                                w.or().like(Position::getTitle, title);
                                break;
                        }
                    }
                });
            } else {
                // 默认只搜索职位名称
                wrapper.like(Position::getTitle, title);
            }
        }
        
        // 按 city 精确筛选
        if (StringUtils.hasText(city)) {
            wrapper.eq(Position::getCity, city);
        }
        
        // 按 category 精确筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(Position::getCategory, category);
        }
        
        // 按工作经验筛选
        if (workYearsMin != null) {
            wrapper.ge(Position::getWorkYearsMin, workYearsMin);
        }
        
        // 按学历要求筛选
        if (educationMin != null) {
            wrapper.ge(Position::getEducationMin, educationMin);
        }
        
        // 按薪资范围筛选
        if (salaryMin != null) {
            wrapper.ge(Position::getSalaryMax, salaryMin);
        }
        if (salaryMax != null) {
            wrapper.le(Position::getSalaryMin, salaryMax);
        }
        
        wrapper.orderByDesc(Position::getCreateTime);
        Page<Position> result = positionMapper.selectPage(page, wrapper);
        
        // 补充企业信息
        for (Position p : result.getRecords()) {
            enrichCompanyInfo(p);
        }
        
        return result;
    }

    @Override
    public void closePosition(Long id, Long bossId) {
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        if (!position.getBossId().equals(bossId)) {
            throw new ForbiddenException("无权操作其他人的职位");
        }
        position.setStatus(0);
        positionMapper.updateById(position);
        log.info("关闭职位成功，id：{}", id);
    }

    @Override
    public void openPosition(Long id, Long bossId) {
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        if (!position.getBossId().equals(bossId)) {
            throw new ForbiddenException("无权操作其他人的职位");
        }
        position.setStatus(1);
        positionMapper.updateById(position);
        log.info("开启职位成功，id：{}", id);
    }

    @Override
    public List<Position> getPositionsByCompanyId(Long companyId) {
        List<Position> positions = positionMapper.selectList(new LambdaQueryWrapper<Position>()
                .eq(Position::getCompanyId, companyId)
                .eq(Position::getStatus, 1)
                .orderByDesc(Position::getCreateTime));
        for (Position p : positions) {
            enrichCompanyInfo(p);
        }
        return positions;
    }

    @Override
    public List<Position> getLatestPositions(int limit) {
        List<Position> positions = positionMapper.selectList(new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, 1)
                .orderByDesc(Position::getCreateTime)
                .last("LIMIT " + limit));
        for (Position p : positions) {
            enrichCompanyInfo(p);
        }
        return positions;
    }

    @Override
    public List<Position> getHotPositions(int limit) {
        // 热门职位：按创建时间倒序，同时筛选薪资较高的职位（简单实现）
        // 实际项目中应该根据投递数量排序，这里用薪资作为热门度指标
        List<Position> positions = positionMapper.selectList(new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, 1)
                .orderByDesc(Position::getSalaryMax)
                .orderByDesc(Position::getCreateTime)
                .last("LIMIT " + limit));
        for (Position p : positions) {
            enrichCompanyInfo(p);
        }
        return positions;
    }

    /**
     * 校验薪资范围
     */
    private void validateSalary(Position position) {
        if (position.getSalaryMin() != null && position.getSalaryMax() != null) {
            if (position.getSalaryMax() <= position.getSalaryMin()) {
                throw new BusinessException("最高薪资必须大于最低薪资");
            }
        }
    }

    /**
     * 补充企业信息
     */
    private void enrichCompanyInfo(Position position) {
        if (position.getCompanyId() != null) {
            Company company = companyMapper.selectById(position.getCompanyId());
            if (company != null) {
                position.setCompanyLogo(company.getLogo());
                position.setCompanyName(company.getCompanyName());
            }
        }
    }
}
