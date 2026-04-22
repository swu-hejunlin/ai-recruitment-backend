package com.example.airecruitmentbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 岗位画像生成请求DTO
 */
@Data
public class JobProfileGenerateRequest {
    /**
     * 岗位ID
     */
    private Long jobId;
}
