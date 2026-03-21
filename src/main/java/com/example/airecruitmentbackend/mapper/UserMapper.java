package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.airecruitmentbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 继承MyBatis Plus的BaseMapper，获得基础CRUD能力
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
