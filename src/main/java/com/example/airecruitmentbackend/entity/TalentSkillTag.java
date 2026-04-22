package com.example.airecruitmentbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人才技能标签实体类
 * 存储人才的技能标签信息
 */
@Data
@TableName("talent_skill_tag")
public class TalentSkillTag {
    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 技能标签
     */
    private String skillTag;

    /**
     * 熟练度等级（1-5）：1-了解，2-熟悉，3-掌握，4-精通，5-专家
     */
    private Integer proficiencyLevel;

    /**
     * 使用年限
     */
    private Integer yearsUsed;

    /**
     * 最后使用时间
     */
    private LocalDate lastUsedTime;

    /**
     * 是否亮点技能：0-否，1-是
     */
    private Integer isHighlight;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
