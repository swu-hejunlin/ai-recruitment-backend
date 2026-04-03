package com.example.airecruitmentbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.entity.Position;
import com.example.airecruitmentbackend.entity.User;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
import com.example.airecruitmentbackend.mapper.UserMapper;
import com.example.airecruitmentbackend.service.PositionService;
import com.example.airecruitmentbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职位控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/position")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;

    /**
     * 发布职位（仅Boss角色）
     */
    @PostMapping("/add")
    public Result<Void> addPosition(HttpServletRequest request,
                                     @Valid @RequestBody Position position) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        validateBossRole(userId);
        position.setBossId(userId);
        positionService.addPosition(position);
        return Result.success("发布职位成功", null);
    }

    /**
     * 更新职位（仅Boss角色）
     */
    @PutMapping("/update")
    public Result<Void> updatePosition(HttpServletRequest request,
                                       @Valid @RequestBody Position position) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        validateBossRole(userId);
        // 校验是否为本人发布的职位
        Position existing = positionService.getPositionById(position.getId());
        if (!existing.getBossId().equals(userId)) {
            throw new BusinessException("无权修改其他人的职位");
        }
        position.setBossId(userId);
        positionService.updatePosition(position);
        return Result.success("更新职位成功", null);
    }

    /**
     * 删除职位（仅Boss角色）
     */
    @DeleteMapping("/delete")
    public Result<Void> deletePosition(HttpServletRequest request,
                                       @RequestParam("id") Long id) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        validateBossRole(userId);
        positionService.deletePosition(id, userId);
        return Result.success("删除职位成功", null);
    }

    /**
     * 查询职位详情
     */
    @GetMapping("/detail")
    public Result<Position> getPositionDetail(@RequestParam("id") Long id) {
        Position position = positionService.getPositionById(id);
        return Result.success("查询成功", position);
    }

    /**
     * Boss查询自己发布的所有职位
     */
    @GetMapping("/boss/list")
    public Result<Page<Position>> getBossPositions(HttpServletRequest request,
                                                    @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        validateBossRole(userId);
        Page<Position> page = positionService.getPositionsByBoss(userId, pageNum, pageSize);
        return Result.success("查询成功", page);
    }

    /**
     * 求职者查询职位列表（招聘中，支持筛选）
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
     * 关闭职位（仅Boss角色）
     */
    @PutMapping("/close")
    public Result<Void> closePosition(HttpServletRequest request,
                                       @RequestParam("id") Long id) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        validateBossRole(userId);
        positionService.closePosition(id, userId);
        return Result.success("关闭职位成功", null);
    }

    /**
     * 开启职位（仅Boss角色）
     */
    @PutMapping("/open")
    public Result<Void> openPosition(HttpServletRequest request,
                                     @RequestParam("id") Long id) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        validateBossRole(userId);
        positionService.openPosition(id, userId);
        return Result.success("开启职位成功", null);
    }

    /**
     * 根据公司ID查询职位列表
     */
    @GetMapping("/company")
    public Result<List<Position>> getPositionsByCompany(@RequestParam("companyId") Long companyId) {
        List<Position> positions = positionService.getPositionsByCompanyId(companyId);
        return Result.success("查询成功", positions);
    }

    /**
     * 查看职位对应的公司信息
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
     * 校验是否为Boss角色
     */
    private void validateBossRole(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() != 2) {
            throw new BusinessException("只有Boss角色才能执行此操作");
        }
    }
}
