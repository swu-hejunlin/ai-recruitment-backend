package com.example.airecruitmentbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.exception.ForbiddenException;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
import com.example.airecruitmentbackend.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 职位控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/position")
@RequiredArgsConstructor
public class PositionController extends BaseController {

    private final PositionService positionService;
    private final CompanyMapper companyMapper;

    /**
     * 发布职位（仅企业HR）
     */
    @PostMapping("/add")
    public Result<Void> addPosition(@Valid @RequestBody Position position) {
        // 角色校验：只有企业HR(role=2)才能发布职位
        Integer role = getCurrentUserRole();
        if (role != 2) {
            throw new ForbiddenException("只有企业HR才能发布职位");
        }
        
        Long userId = getCurrentUserId();
        position.setBossId(userId);
        positionService.addPosition(position);
        return Result.success("发布职位成功", null);
    }

    /**
     * 更新职位（仅企业HR）
     */
    @PutMapping("/update")
    public Result<Void> updatePosition(@Valid @RequestBody Position position) {
        // 角色校验：只有企业HR(role=2)才能更新职位
        Integer role = getCurrentUserRole();
        if (role != 2) {
            throw new ForbiddenException("只有企业HR才能更新职位");
        }
        
        Long userId = getCurrentUserId();
        // 校验是否为本人发布的职位
        Position existing = positionService.getPositionById(position.getId());
        if (!existing.getBossId().equals(userId)) {
            throw new ForbiddenException("无权修改其他人的职位");
        }
        position.setBossId(userId);
        positionService.updatePosition(position);
        return Result.success("更新职位成功", null);
    }

    /**
     * 删除职位（仅企业HR）
     */
    @DeleteMapping("/delete")
    public Result<Void> deletePosition(@RequestParam("id") Long id) {
        // 角色校验：只有企业HR(role=2)才能删除职位
        Integer role = getCurrentUserRole();
        if (role != 2) {
            throw new ForbiddenException("只有企业HR才能删除职位");
        }
        
        Long userId = getCurrentUserId();
        positionService.deletePosition(id, userId);
        return Result.success("删除职位成功", null);
    }

    /**
     * 查询职位详情（公开）
     */
    @GetMapping("/detail")
    public Result<Position> getPositionDetail(@RequestParam("id") Long id) {
        Position position = positionService.getPositionById(id);
        return Result.success("查询成功", position);
    }

    /**
     * Boss查询自己发布的所有职位（仅企业HR）
     */
    @GetMapping("/boss/list")
    public Result<Page<Position>> getBossPositions(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        // 角色校验：只有企业HR(role=2)才能查询
        Integer role = getCurrentUserRole();
        if (role != 2) {
            throw new ForbiddenException("只有企业HR才能查看职位管理");
        }
        
        Long userId = getCurrentUserId();
        Page<Position> page = positionService.getPositionsByBoss(userId, pageNum, pageSize);
        return Result.success("查询成功", page);
    }

    /**
     * 求职者查询职位列表（招聘中，支持筛选）- 公开
     */
    @GetMapping("/list")
    public Result<Page<Position>> getActivePositions(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Page<Position> page = positionService.getActivePositions(title, city, category, pageNum, pageSize);
        return Result.success("查询成功", page);
    }

    /**
     * 关闭职位（仅企业HR）
     */
    @PutMapping("/close")
    public Result<Void> closePosition(@RequestParam("id") Long id) {
        // 角色校验：只有企业HR(role=2)才能操作
        Integer role = getCurrentUserRole();
        if (role != 2) {
            throw new ForbiddenException("只有企业HR才能管理职位");
        }
        
        Long userId = getCurrentUserId();
        positionService.closePosition(id, userId);
        return Result.success("关闭职位成功", null);
    }

    /**
     * 开启职位（仅企业HR）
     */
    @PutMapping("/open")
    public Result<Void> openPosition(@RequestParam("id") Long id) {
        // 角色校验：只有企业HR(role=2)才能操作
        Integer role = getCurrentUserRole();
        if (role != 2) {
            throw new ForbiddenException("只有企业HR才能管理职位");
        }
        
        Long userId = getCurrentUserId();
        positionService.openPosition(id, userId);
        return Result.success("开启职位成功", null);
    }

    /**
     * 根据公司ID查询职位列表（公开）
     */
    @GetMapping("/company")
    public Result<List<Position>> getPositionsByCompany(@RequestParam("companyId") Long companyId) {
        List<Position> positions = positionService.getPositionsByCompanyId(companyId);
        return Result.success("查询成功", positions);
    }

    /**
     * 查看职位对应的公司信息（公开）
     */
    @GetMapping("/company-info")
    public Result<Company> getCompanyByPosition(@RequestParam("positionId") Long positionId) {
        Position position = positionService.getPositionById(positionId);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        Company company = companyMapper.selectById(position.getCompanyId());
        if (company == null) {
            throw new BusinessException("公司信息不存在");
        }
        return Result.success("查询成功", company);
    }

    /**
     * 获取最新职位列表（首页推荐）- 公开
     */
    @GetMapping("/latest")
    public Result<List<Position>> getLatestPositions(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<Position> positions = positionService.getLatestPositions(limit);
        return Result.success("查询成功", positions);
    }

    /**
     * 获取热门职位列表（首页推荐）- 公开
     */
    @GetMapping("/hot")
    public Result<List<Position>> getHotPositions(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<Position> positions = positionService.getHotPositions(limit);
        return Result.success("查询成功", positions);
    }

    /**
     * 获取职位详情（含公司信息，供求职者查看）- 公开
     */
    @GetMapping("/detail-with-company")
    public Result<Map<String, Object>> getPositionDetailWithCompany(@RequestParam("id") Long id) {
        Position position = positionService.getPositionById(id);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        Company company = companyMapper.selectById(position.getCompanyId());
        if (company == null) {
            throw new BusinessException("公司信息不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("position", position);
        result.put("company", company);
        return Result.success("查询成功", result);
    }
}
