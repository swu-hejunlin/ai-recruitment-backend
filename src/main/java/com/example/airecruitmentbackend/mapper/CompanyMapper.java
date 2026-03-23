package com.example.airecruitmentbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.airecruitmentbackend.entity.Company;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业信息Mapper接口
 */
@Mapper
public interface CompanyMapper extends BaseMapper<Company> {
}
