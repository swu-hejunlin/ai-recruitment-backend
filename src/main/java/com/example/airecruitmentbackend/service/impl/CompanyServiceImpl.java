package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
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

    @Override
    public Company getByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Company::getUserId, userId);
        Company company = companyMapper.selectOne(wrapper);

        return company;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCompany(Company company) {
        if (company == null || company.getId() == null) {
            throw new BusinessException("企业信息或ID不能为空");
        }

        int rows = companyMapper.updateById(company);
        if (rows == 0) {
            throw new BusinessException("更新企业信息失败");
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
