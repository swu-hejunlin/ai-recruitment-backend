package com.example.airecruitmentbackend.controller;

import com.example.airecruitmentbackend.common.Result;
import com.example.airecruitmentbackend.entity.Favorite;
import com.example.airecruitmentbackend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 收藏控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController extends BaseController {

    private final FavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Result<Void> addFavorite(@RequestParam Integer targetType, @RequestParam Long targetId) {
        Long userId = getCurrentUserId();
        log.info("收到添加收藏请求，userId：{}，targetType：{}，targetId：{}", userId, targetType, targetId);

        try {
            boolean result = favoriteService.addFavorite(userId, targetType, targetId);
            if (result) {
                log.info("添加收藏成功");
                return Result.success("添加收藏成功", null);
            } else {
                return Result.error("添加收藏失败");
            }
        } catch (Exception e) {
            log.error("添加收藏失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/remove")
    public Result<Void> removeFavorite(@RequestParam Integer targetType, @RequestParam Long targetId) {
        Long userId = getCurrentUserId();
        log.info("收到取消收藏请求，userId：{}，targetType：{}，targetId：{}", userId, targetType, targetId);

        try {
            boolean result = favoriteService.removeFavorite(userId, targetType, targetId);
            if (result) {
                log.info("取消收藏成功");
                return Result.success("取消收藏成功", null);
            } else {
                return Result.error("取消收藏失败");
            }
        } catch (Exception e) {
            log.error("取消收藏失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getFavorites(@RequestParam Integer targetType) {
        Long userId = getCurrentUserId();
        log.info("收到获取收藏列表请求，userId：{}，targetType：{}", userId, targetType);

        try {
            List<Map<String, Object>> favorites = favoriteService.getFavorites(userId, targetType);
            log.info("获取收藏列表成功，数量：{}", favorites.size());
            return Result.success("获取收藏列表成功", favorites);
        } catch (Exception e) {
            log.error("获取收藏列表失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Map<String, Boolean>> checkFavorite(@RequestParam Integer targetType, @RequestParam Long targetId) {
        Long userId = getCurrentUserId();
        log.info("收到检查收藏状态请求，userId：{}，targetType：{}，targetId：{}", userId, targetType, targetId);

        try {
            boolean isFavorite = favoriteService.isFavorite(userId, targetType, targetId);
            log.info("检查收藏状态成功，isFavorite：{}", isFavorite);
            return Result.success("检查收藏状态成功", Map.of("isFavorite", isFavorite));
        } catch (Exception e) {
            log.error("检查收藏状态失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
