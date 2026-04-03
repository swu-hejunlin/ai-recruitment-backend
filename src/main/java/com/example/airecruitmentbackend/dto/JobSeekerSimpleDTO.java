package com.example.airecruitmentbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 求职者简要信息DTO
 * 用于Boss查看求职者列表
 */
@Data
public class JobSeekerSimpleDTO {
    private Long id;
    private String name;
    private Integer gender;
    private String avatar;
    private String phone;
    private String email;
    private Integer age;
    private Integer workYears;
    private String city;
    private String introduction;
    private String skills;
    private String resumeUrl;
    private LocalDateTime createTime;
}
