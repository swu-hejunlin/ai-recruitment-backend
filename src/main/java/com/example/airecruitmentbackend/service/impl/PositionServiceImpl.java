package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
import com.example.airecruitmentbackend.mapper.PositionMapper;
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

    @Override
    public void addPosition(Position position) {
        // 校验薪资范围
        validateSalary(position);
        // 校验企业信息
        Company company = companyMapper.selectById(position.getCompanyId());
        if (company == null) {
            throw new BusinessException("企业信息不存在");
        }
        // 默认状态为招聘中
        if (position.getStatus() == null) {
            position.setStatus(1);
        }
        positionMapper.insert(position);
        log.info("发布职位成功，id：{}，title：{}", position.getId(), position.getTitle());
    }

    @Override
    public void updatePosition(Position position) {
        // 校验薪资范围
        validateSalary(position);
        Position existing = positionMapper.selectById(position.getId());
        if (existing == null) {
            throw new BusinessException("职位不存在");
        }
        positionMapper.updateById(position);
        log.info("更新职位成功，id：{}", position.getId());
    }

    @Override
    public void deletePosition(Long id, Long bossId) {
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        if (!position.getBossId().equals(bossId)) {
            throw new BusinessException("无权删除其他人的职位");
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
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getBossId, bossId)
               .orderByDesc(Position::getCreateTime);
        Page<Position> result = positionMapper.selectPage(page, wrapper);
        // 补充企业信息
        for (Position p : result.getRecords()) {
            enrichCompanyInfo(p);
        }
        return result;
    }

    @Override
    public Page<Position> getActivePositions(String title, String city, String category, int pageNum, int pageSize) {
        Page<Position> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        // 只查询招聘中的职位
        wrapper.eq(Position::getStatus, 1);
        // 按 title 模糊搜索
        if (StringUtils.hasText(title)) {
            wrapper.like(Position::getTitle, title);
        }
        // 按 city 精确筛选
        if (StringUtils.hasText(city)) {
            wrapper.eq(Position::getCity, city);
        }
        // 按 category 精确筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(Position::getCategory, category);
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
            throw new BusinessException("无权操作其他人的职位");
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
            throw new BusinessException("无权操作其他人的职位");
        }
        position.setStatus(1);
        positionMapper.updateById(position);
        log.info("开启职位成功，id：{}", id);
    }

    @Override
    public List<Position> getPositionsByCompanyId(Long companyId) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getCompanyId, companyId)
               .eq(Position::getStatus, 1)
               .orderByDesc(Position::getCreateTime);
        List<Position> positions = positionMapper.selectList(wrapper);
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
