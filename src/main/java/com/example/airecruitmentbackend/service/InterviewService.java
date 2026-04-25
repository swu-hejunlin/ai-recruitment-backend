package com.example.airecruitmentbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.airecruitmentbackend.dto.InterviewEvaluationDTO;
import com.example.airecruitmentbackend.dto.InterviewRequest;
import com.example.airecruitmentbackend.dto.InterviewDetailDTO;
import com.example.airecruitmentbackend.entity.Interview;

import java.util.List;

/**
 * 面试服务接口
 */
public interface InterviewService extends IService<Interview> {

    /**
     * 创建面试邀请
     * @param request 面试请求
     * @param bossId 企业HR ID
     * @return 面试ID
     */
    Long createInterview(InterviewRequest request, Long bossId);

    /**
     * 更新面试状态
     * @param interviewId 面试ID
     * @param status 状态：2-已确认，3-已拒绝，4-已完成
     * @return 是否成功
     */
    boolean updateInterviewStatus(Long interviewId, Integer status);

    /**
     * 获取企业HR的面试列表
     * @param companyId 企业ID
     * @return 面试列表
     */
    List<InterviewDetailDTO> getCompanyInterviews(Long companyId);

    /**
     * 获取求职者的面试列表
     * @param jobSeekerId 求职者ID
     * @return 面试列表
     */
    List<InterviewDetailDTO> getJobSeekerInterviews(Long jobSeekerId);

    /**
     * 获取面试详情
     * @param interviewId 面试ID
     * @return 面试详情
     */
    InterviewDetailDTO getInterviewDetail(Long interviewId);

    /**
     * 删除面试
     * @param interviewId 面试ID
     * @param bossId 企业HR ID
     * @return 是否成功
     */
    boolean deleteInterview(Long interviewId, Long bossId);

    /**
     * 处理模拟面试
     * @param video 视频文件
     * @param userId 用户ID
     * @return 评估结果
     */
    Object processMockInterview(org.springframework.web.multipart.MultipartFile video, Long userId);

    Object processMockInterview(org.springframework.web.multipart.MultipartFile video, Long userId, Long interviewId);

    Object processMockInterview(org.springframework.web.multipart.MultipartFile video, Long userId, Long interviewId, String sessionKey);

    /**
     * 生成模拟面试题
     * @param userId 用户ID
     * @return 面试题列表
     */
    Object generateMockInterviewQuestions(Long userId);

    Object generateMockInterviewQuestions(Long userId, Long interviewId);

    Object generateMockInterviewQuestions(Long userId, Long interviewId, String positionName, 
            String positionCategory, String city, String description, String requirement);

    /**
     * 真实AI面试结束（异步处理）
     * @param interviewId 面试ID
     * @param video 面试视频
     */
    void finishRealInterview(Long interviewId, org.springframework.web.multipart.MultipartFile video);

    /**
     * 获取面试评估结果（供BOSS端使用）
     * @param interviewId 面试ID
     * @return 评估结果
     */
    InterviewEvaluationDTO getInterviewEvaluation(Long interviewId);

    /**
     * 检查面试评估结果是否存在
     * @param interviewId 面试ID
     * @return 是否存在
     */
    boolean hasEvaluation(Long interviewId);
}
