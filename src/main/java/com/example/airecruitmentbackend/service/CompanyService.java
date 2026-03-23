package com.example.airecruitmentbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.airecruitmentbackend.entity.Company;

/**
 * 企业信息服务接口
 */
public interface CompanyService extends IService<Company> {

    /**
     * 根据用户ID获取企业信息
     *
     * @param userId 用户ID
     * @return 企业信息
     */
    Company getByUserId(Long userId);

    /**
     * 更新企业信息
     *
     * @param company 企业信息
     * @return 是否成功
     */
    boolean updateCompany(Company company);

    /**
     * 上传企业logo
     *
     * @param userId 用户ID
     * @param logoUrl logo URL
     * @return 是否成功
     */
    boolean updateLogo(Long userId, String logoUrl);

    /**
     * 上传营业执照
     *
     * @param userId 用户ID
     * @param licenseUrl 营业执照URL
     * @return 是否成功
     */
    boolean updateBusinessLicense(Long userId, String licenseUrl);
}
