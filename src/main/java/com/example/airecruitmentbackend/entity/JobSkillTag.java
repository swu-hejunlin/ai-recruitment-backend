package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位技能标签实体类
 * 存储岗位的技能标签信息
 */
@Data
@TableName("job_skill_tag")
public class JobSkillTag {
    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 岗位ID（对应数据库position_id字段）
     */
    private Long positionId;

    /**
     * 技能标签
     */
    private String skillTag;

    /**
     * 技能等级：required-必须，preferred-优先
     */
    private String skillLevel;

    /**
     * 熟练度权重（1-100）
     */
    private Integer proficiencyWeight;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
