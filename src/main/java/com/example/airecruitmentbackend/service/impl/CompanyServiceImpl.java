package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.entity.Application;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.ApplicationMapper;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
import com.example.airecruitmentbackend.mapper.PositionMapper;
import com.example.airecruitmentbackend.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 企业信息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements CompanyService {

    private final CompanyMapper companyMapper;
    private final ApplicationMapper applicationMapper;
    private final PositionMapper positionMapper;

    @Override
    public Company getByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        return companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getUserId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCompany(Company company) {
        if (company == null || company.getId() == null) {
            throw new BusinessException("企业信息或ID不能为空");
        }

        // 查询原信息，用于判断企业名称是否变更
        Company original = companyMapper.selectById(company.getId());
        boolean nameChanged = original != null
                && company.getCompanyName() != null
                && !company.getCompanyName().equals(original.getCompanyName());

        int rows = companyMapper.updateById(company);
        if (rows == 0) {
            throw new BusinessException("更新企业信息失败");
        }

        // 同步更新投递记录中的冗余企业名称
        if (nameChanged) {
            applicationMapper.update(null, new LambdaUpdateWrapper<Application>()
                    .eq(Application::getCompanyId, company.getId())
                    .set(Application::getCompanyName, company.getCompanyName()));
            // 同步更新职位表冗余字段
            positionMapper.update(null, new LambdaUpdateWrapper<Position>()
                    .eq(Position::getCompanyId, company.getId())
                    .set(Position::getCompanyLogo, company.getLogo())
                    .set(Position::getCompanyName, company.getCompanyName()));
            log.info("同步更新冗余字段：companyId={}, name={}", company.getId(), company.getCompanyName());
        }

        log.info("更新企业信息成功：id={}", company.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLogo(Long userId, String logoUrl) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (logoUrl == null || logoUrl.trim().isEmpty()) {
            throw new BusinessException("Logo URL不能为空");
        }

        Company company = getByUserId(userId);
        if (company == null) {
            throw new BusinessException("企业信息不存在");
        }

        company.setLogo(logoUrl);
        int rows = companyMapper.updateById(company);
        if (rows == 0) {
            throw new BusinessException("更新Logo失败");
        }

        // 同步更新职位表中的冗余字段
        positionMapper.update(null, new LambdaUpdateWrapper<Position>()
                .eq(Position::getCompanyId, company.getId())
                .set(Position::getCompanyLogo, logoUrl)
                .set(Position::getCompanyName, company.getCompanyName()));
        log.info("同步更新职位表冗余字段：companyId={}, logoUrl={}", company.getId(), logoUrl);
        log.info("更新企业Logo成功：userId={}, logoUrl={}", userId, logoUrl);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBusinessLicense(Long userId, String licenseUrl) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (licenseUrl == null || licenseUrl.trim().isEmpty()) {
            throw new BusinessException("营业执照URL不能为空");
        }

        Company company = getByUserId(userId);
        if (company == null) {
            throw new BusinessException("企业信息不存在");
        }

        company.setBusinessLicense(licenseUrl);
        int rows = companyMapper.updateById(company);
        if (rows == 0) {
            throw new BusinessException("更新营业执照失败");
        }

        log.info("更新企业营业执照成功：userId={}, licenseUrl={}", userId, licenseUrl);
        return true;
    }
}
