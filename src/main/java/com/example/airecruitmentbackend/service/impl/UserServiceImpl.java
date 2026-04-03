package com.example.airecruitmentbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.airecruitmentbackend.dto.LoginRequest;
import com.example.airecruitmentbackend.dto.LoginResponse;
import com.example.airecruitmentbackend.dto.SendCodeRequest;
import com.example.airecruitmentbackend.dto.SwitchRoleRequest;
import com.example.airecruitmentbackend.entity.User;
import com.example.airecruitmentbackend.entity.JobSeeker;
import com.example.airecruitmentbackend.entity.Company;
import com.example.airecruitmentbackend.exception.BusinessException;
import com.example.airecruitmentbackend.mapper.UserMapper;
import com.example.airecruitmentbackend.mapper.JobSeekerMapper;
import com.example.airecruitmentbackend.mapper.CompanyMapper;
import com.example.airecruitmentbackend.service.UserService;
import com.example.airecruitmentbackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 * 实现用户登录相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JobSeekerMapper jobSeekerMapper;
    private final CompanyMapper companyMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;

    /**
     * Redis验证码key前缀
     */
    private static final String CODE_PREFIX = "login:code:";

    /**
     * 验证码过期时间（秒）
     */
    private static final long CODE_EXPIRE_SECONDS = 300;

    /**
     * 发送验证码
     * 生成6位随机验证码，存入Redis，并模拟发送短信
     *
     * @param request 发送验证码请求
     */
    @Override
    public void sendCode(SendCodeRequest request) {
        String phone = request.getPhone();

        // 生成6位随机数字验证码
        String code = generateCode();

        // 将验证码存入Redis，key为"login:code:手机号"，value为验证码，过期时间5分钟
        String redisKey = CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        log.info("========== 验证码发送成功 ==========");
        log.info("手机号：{}", phone);
        log.info("验证码：{}", code);
        log.info("有效期：5分钟");
        log.info("========== 后续可对接阿里云/腾讯云短信API ==========");

        // TODO: 后续可对接阿里云/腾讯云短信API发送真实短信
    }

    /**
     * 验证码登录
     * 校验验证码，成功则返回JWT令牌
     * 如果手机号已存在但角色不同，返回needSwitchRole=true，提示前端
     *
     * @param request 登录请求
     * @return 登录响应，包含JWT令牌和用户信息
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();
        Integer role = request.getRole();

        // 校验角色范围
        if (role != 1 && role != 2) {
            throw new BusinessException(400, "角色参数错误");
        }

        // 从Redis获取验证码
        String redisKey = CODE_PREFIX + phone;
        String storedCode = (String) redisTemplate.opsForValue().get(redisKey);

        // 验证码校验
        if (storedCode == null) {
            throw new BusinessException(400, "验证码已过期或不存在");
        }
        if (!storedCode.equals(code)) {
            throw new BusinessException(400, "验证码错误");
        }

        // 查询用户是否存在（只按手机号查询，因为手机号唯一）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);

        // 用户不存在则自动注册
        if (user == null) {
            user = registerUser(phone, role);
            // 新用户直接登录成功，删除验证码
            redisTemplate.delete(redisKey);
            String token = jwtUtil.generateToken(user.getId(), user.getRole());
            log.info("新用户登录成功：userId={}, phone={}, role={}", user.getId(), phone, role);
            return new LoginResponse(token, user.getId(), user.getRole(), false, null);
        }

        // 用户存在，检查角色是否匹配
        if (!user.getRole().equals(role)) {
            log.info("用户存在但角色不同，需要身份切换：userId={}, phone={}, 当前角色={}, 目标角色={}",
                    user.getId(), phone, user.getRole(), role);
            // 不删除验证码，让用户确认切换身份
            return new LoginResponse(null, user.getId(), user.getRole(), true, user.getRole());
        }

        // 角色匹配，登录成功
        redisTemplate.delete(redisKey);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        log.info("用户登录成功：userId={}, phone={}, role={}", user.getId(), phone, role);
        return new LoginResponse(token, user.getId(), user.getRole(), false, null);
    }

    /**
     * 身份切换
     * 用户确认切换角色后，修改用户角色并返回新的JWT令牌
     *
     * @param request 身份切换请求
     * @return 登录响应，包含新的JWT令牌和用户信息
     */
    @Override
    @Transactional
    public LoginResponse switchRole(SwitchRoleRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();
        Integer role = request.getRole();

        // 校验角色范围
        if (role != 1 && role != 2) {
            throw new BusinessException(400, "角色参数错误");
        }

        // 从Redis获取验证码
        String redisKey = CODE_PREFIX + phone;
        String storedCode = (String) redisTemplate.opsForValue().get(redisKey);

        // 验证码校验
        if (storedCode == null) {
            throw new BusinessException(400, "验证码已过期或不存在");
        }
        if (!storedCode.equals(code)) {
            throw new BusinessException(400, "验证码错误");
        }

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 检查角色是否真的需要切换
        if (user.getRole().equals(role)) {
            throw new BusinessException(400, "当前角色与目标角色相同，无需切换");
        }

        // 删除已使用的验证码
        redisTemplate.delete(redisKey);

        // 更新用户角色
        user.setRole(role);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户角色切换成功：userId={}, phone={}, 原角色={}, 新角色={}",
                user.getId(), phone, role == 1 ? 2 : 1, role);

        // 生成新的JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        return new LoginResponse(token, user.getId(), user.getRole(), false, null);
    }

    @Override

    public Integer getRoleByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return user.getRole();
    }

    /**
     * 自动注册新用户
     * 同时创建对应的初始数据记录（求职者信息或企业信息）
     *
     * @param phone 手机号
     * @param role  用户角色
     * @return 新注册的用户对象
     */
    @Transactional
    public User registerUser(String phone, Integer role) {
        User user = new User();
        user.setPhone(phone);
        user.setRole(role);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);

        // 根据角色自动创建对应的初始数据记录
        if (role == 1) {
            // 创建空的求职者信息记录，设置默认用户名
            JobSeeker jobSeeker = new JobSeeker();
            jobSeeker.setUserId(user.getId());
            jobSeeker.setPhone(phone); // 默认使用注册手机号
            jobSeeker.setName("求职者_" + phone); // 默认用户名：求职者_手机号
            jobSeekerMapper.insert(jobSeeker);
            log.info("自动创建求职者初始信息：jobSeekerId={}, userId={}", jobSeeker.getId(), user.getId());
        } else if (role == 2) {
            // 创建空的企业信息记录，设置默认企业名
            Company company = new Company();
            company.setUserId(user.getId());
            company.setCompanyName("企业_" + phone); // 默认企业名：企业_手机号
            companyMapper.insert(company);
            log.info("自动创建企业初始信息：companyId={}, userId={}", company.getId(), user.getId());
        }

        log.info("自动注册新用户并创建初始数据：userId={}, phone={}, role={}", user.getId(), phone, role);

        return user;
    }

    /**
     * 生成6位随机数字验证码
     *
     * @return 6位数字验证码
     */
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
