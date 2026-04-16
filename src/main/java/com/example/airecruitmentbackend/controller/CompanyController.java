package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.dto.CompanyUpdateRequest;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 企业信息控制器
 * 处理企业信息相关的接口请求
 */
@Slf4j
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController extends BaseController {

    private final CompanyService companyService;

    /**
     * 获取当前登录企业信息（仅企业HR）
     * 接口地址：GET /api/company/info
     */
    @GetMapping("/info")
    public Result<Company> getCompanyInfo() {
        // 角色校验：只有企业HR(role=2)才能访问
        Integer role = getCurrentUserRole();
        if (role != 2) {
            throw new ForbiddenException("只有企业HR才能访问企业管理");
        }
        
        Long userId = getCurrentUserId();
        log.info("获取企业信息请求，userId：{}", userId);

        Company company = companyService.getByUserId(userId);
        if (company == null) {
            return Result.error("企业信息不存在，请先完善企业信息");
        }

        return Result.success("获取企业信息成功", company);
    }

    /**
     * 根据公司ID查询企业信息（供求职者查看公司详情页）
     * 接口地址：GET /api/company/{id}
     * 公开接口，无需登录
     */
    @GetMapping("/{id}")
    public Result<Company> getCompanyById(@PathVariable("id") Long id) {
        log.info("根据公司ID查询企业信息请求，companyId：{}", id);

        Company company = companyService.getById(id);
        if (company == null) {
            return Result.error("企业信息不存在");
        }

        return Result.success("查询成功", company);
    }

    /**
     * 更新企业信息（仅企业HR）
     * 接口地址：PUT /api/company/update
     */
    @PutMapping("/update")
    public Result<Void> updateCompanyInfo(@Valid @RequestBody CompanyUpdateRequest updateRequest) {
        // 角色校验：只有企业HR(role=2)才能更新
        Integer role = getCurrentUserRole();
        if (role != 2) {
            throw new ForbiddenException("只有企业HR才能更新企业信息");
        }
        
        Long userId = getCurrentUserId();
        log.info("更新企业信息请求，userId：{}，企业ID：{}", userId, updateRequest.getId());

        Company company = companyService.getById(updateRequest.getId());
        if (company == null) {
            return Result.error("企业信息不存在");
        }

        if (!company.getUserId().equals(userId)) {
            throw new ForbiddenException("无权修改其他企业的信息");
        }

        BeanUtils.copyProperties(updateRequest, company);
        companyService.updateCompany(company);

        return Result.success("更新企业信息成功", null);
    }

    /**
     * 上传企业logo（仅企业HR）
     * 接口地址：POST /api/company/logo
     */
    @PostMapping("/logo")
    public Result<Void> updateLogo(@RequestParam("logoUrl") String logoUrl) {
        // 角色校验：只有企业HR(role=2)才能上传
        Integer role = getCurrentUserRole();
        if (role != 2) {
            throw new ForbiddenException("只有企业HR才能上传logo");
        }
        
        Long userId = getCurrentUserId();
        log.info("更新企业logo请求，userId：{}，logoUrl：{}", userId, logoUrl);

        companyService.updateLogo(userId, logoUrl);
        return Result.success("上传logo成功", null);
    }

    /**
     * 上传营业执照（仅企业HR）
     * 接口地址：POST /api/company/license
     */
    @PostMapping("/license")
    public Result<Void> updateBusinessLicense(@RequestParam("licenseUrl") String licenseUrl) {
        // 角色校验：只有企业HR(role=2)才能上传
        Integer role = getCurrentUserRole();
        if (role != 2) {
            throw new ForbiddenException("只有企业HR才能上传营业执照");
        }
        
        Long userId = getCurrentUserId();
        log.info("更新企业营业执照请求，userId：{}，licenseUrl：{}", userId, licenseUrl);

        companyService.updateBusinessLicense(userId, licenseUrl);
        return Result.success("上传营业执照成功", null);
    }
}
