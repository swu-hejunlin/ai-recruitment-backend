package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.dto.CompanyUpdateRequest;
import com.example.airecruitmentbackend.service.CompanyService;
import com.example.airecruitmentbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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
public class CompanyController {

    private final CompanyService companyService;
    private final JwtUtil jwtUtil;

    /**
     * 获取企业信息
     * 接口地址：GET /api/company/info
     *
     * @param request HTTP请求（从请求头中获取JWT令牌）
     * @return 企业信息
     */
    @GetMapping("/info")
    public Result<Company> getCompanyInfo(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("获取企业信息请求，userId：{}", userId);

        Company company = companyService.getByUserId(userId);
        if (company == null) {
            return Result.error("企业信息不存在，请先完善企业信息");
        }

        return Result.success("获取企业信息成功", company);
    }

    /**
     * 更新企业信息
     * 接口地址：PUT /api/company/update
     *
     * @param request HTTP请求（从请求头中获取JWT令牌）
     * @param updateRequest 企业信息更新请求
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> updateCompanyInfo(HttpServletRequest request,
                                          @Valid @RequestBody CompanyUpdateRequest updateRequest) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("更新企业信息请求，userId：{}，企业ID：{}", userId, updateRequest.getId());

        Company company = companyService.getById(updateRequest.getId());
        if (company == null) {
            return Result.error("企业信息不存在");
        }

        if (!company.getUserId().equals(userId)) {
            return Result.error("无权修改其他企业的信息");
        }

        BeanUtils.copyProperties(updateRequest, company);
        companyService.updateCompany(company);

        return Result.success("更新企业信息成功", null);
    }

    /**
     * 上传企业logo
     * 接口地址：POST /api/company/logo
     *
     * @param request HTTP请求（从请求头中获取JWT令牌）
     * @param logoUrl logo URL
     * @return 操作结果
     */
    @PostMapping("/logo")
    public Result<Void> updateLogo(HttpServletRequest request,
                                    @RequestParam("logoUrl") String logoUrl) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("更新企业logo请求，userId：{}，logoUrl：{}", userId, logoUrl);

        companyService.updateLogo(userId, logoUrl);
        return Result.success("上传logo成功", null);
    }

    /**
     * 上传营业执照
     * 接口地址：POST /api/company/license
     *
     * @param request HTTP请求（从请求头中获取JWT令牌）
     * @param licenseUrl 营业执照URL
     * @return 操作结果
     */
    @PostMapping("/license")
    public Result<Void> updateBusinessLicense(HttpServletRequest request,
                                              @RequestParam("licenseUrl") String licenseUrl) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        log.info("更新企业营业执照请求，userId：{}，licenseUrl：{}", userId, licenseUrl);

        companyService.updateBusinessLicense(userId, licenseUrl);
        return Result.success("上传营业执照成功", null);
    }
}
