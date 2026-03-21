# 智能招聘平台 - 登录模块接口文档

## 接口说明
本文档描述登录模块的核心接口，包含发送验证码、验证码登录和身份切换功能。

## 业务规则
- **手机号唯一性**：同一个手机号只能有一个账号
- **身份切换**：如果已注册用户以不同角色登录，系统会提示身份冲突，需要用户确认切换身份
- **自动注册**：首次使用手机号登录时，系统会自动创建用户

## 基础信息
- **Base URL**: `http://localhost:8080`
- **接口返回格式**: JSON
- **字符编码**: UTF-8

## 统一返回格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1710931200000
}
```

### 失败响应
```json
{
  "code": 400,
  "message": "错误信息描述",
  "data": null,
  "timestamp": 1710931200000
}
```

**字段说明**：
- `code`: 状态码，200-成功，其他-失败
- `message`: 返回消息描述
- `data`: 返回数据（具体接口说明）
- `timestamp`: 时间戳（毫秒）

---

## 1. 发送验证码接口

### 接口信息
- **接口地址**: `POST /api/user/send-code`
- **接口描述**: 发送手机验证码（用于登录或身份切换）

### 请求参数

**Headers**：
```
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "phone": "13800138000",
  "role": 1
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| phone | String | 是 | 手机号 | 1开头，11位数字 |
| role | Integer | 否 | 用户角色 | 1-求职者，2-企业HR（用于提示身份切换） |

### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "验证码发送成功",
  "data": null,
  "timestamp": 1710931200000
}
```

**失败响应示例**：

1. 手机号格式错误
```json
{
  "code": 400,
  "message": "手机号格式不正确",
  "data": null,
  "timestamp": 1710931200000
}
```

### 业务说明
- 验证码为6位随机数字
- 验证码有效期：5分钟（300秒）
- 验证码存储在Redis中，key格式为`login:code:{phone}`
- 当前为模拟发送，验证码会打印在后端控制台（后续可对接短信API）
- 同一手机号重复发送会覆盖之前的验证码

---

## 2. 验证码登录接口

### 接口信息
- **接口地址**: `POST /api/user/login`
- **接口描述**: 使用手机号+验证码进行登录，返回JWT令牌

### 请求参数

**Headers**：
```
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "phone": "13800138000",
  "code": "123456",
  "role": 1
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| phone | String | 是 | 手机号 | 1开头，11位数字 |
| code | String | 是 | 验证码 | 6位数字 |
| role | Integer | 是 | 用户角色 | 1-求职者，2-企业HR |

### 返回参数

#### 成功响应（正常登录）
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInJvbGUiOjEsImlhdCI6MTcxMDkzMTIwMCwiZXhwIjoxNzEwOTQ4NDAwfQ.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
    "userId": 1,
    "role": 1,
    "needSwitchRole": false,
    "currentRole": null
  },
  "timestamp": 1710931200000
}
```

#### 需要身份切换响应
```json
{
  "code": 200,
  "message": "检测到身份冲突，请确认是否切换身份",
  "data": {
    "token": null,
    "userId": 1,
    "role": 1,
    "needSwitchRole": true,
    "currentRole": 1
  },
  "timestamp": 1710931200000
}
```

**返回数据字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| token | String | JWT令牌（有效期为2小时），needSwitchRole为true时为null |
| userId | Long | 用户ID |
| role | Integer | 用户角色：1-求职者，2-企业HR |
| needSwitchRole | Boolean | 是否需要身份切换：true-需要切换，false-正常登录 |
| currentRole | Integer | 当前用户的现有角色（当needSwitchRole为true时返回） |

**失败响应示例**：

1. 验证码已过期
```json
{
  "code": 400,
  "message": "验证码已过期或不存在",
  "data": null,
  "timestamp": 1710931200000
}
```

2. 验证码错误
```json
{
  "code": 400,
  "message": "验证码错误",
  "data": null,
  "timestamp": 1710931200000
}
```

3. 角色参数错误
```json
{
  "code": 400,
  "message": "角色参数错误",
  "data": null,
  "timestamp": 1710931200000
}
```

### 业务说明

#### 正常登录流程
1. 验证码校验通过后，验证码会被立即删除（一次性使用）
2. 如果用户不存在，系统会自动注册新用户
3. 如果用户存在且角色匹配，直接登录成功，返回JWT令牌
4. JWT令牌有效期为2小时（可在配置文件中修改）

#### 身份切换流程
1. 如果用户存在但角色不同，系统返回`needSwitchRole=true`
2. 前端收到此响应后，应弹窗提示用户："检测到您已注册为[求职者/企业HR]，是否切换为[企业HR/求职者]身份？"
3. 用户确认后，前端调用身份切换接口（见接口3）
4. 身份切换成功后，返回新的JWT令牌

### JWT令牌使用方式
后续需要验证用户身份的接口，需要在请求头中携带JWT令牌：

**Headers**：
```
Authorization: Bearer {token}
```

示例：
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInJvbGUiOjEsImlhdCI6MTcxMDkzMTIwMCwiZXhwIjoxNzEwOTQ4NDAwfQ.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

---

## 3. 身份切换接口

### 接口信息
- **接口地址**: `POST /api/user/switch-role`
- **接口描述**: 用户确认切换身份后，修改用户角色并返回新的JWT令牌

### 请求参数

**Headers**：
```
Content-Type: application/json
```

**Body（JSON）**：
```json
{
  "phone": "13800138000",
  "code": "123456",
  "role": 2
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| phone | String | 是 | 手机号 | 1开头，11位数字 |
| code | String | 是 | 验证码 | 6位数字（需要重新获取） |
| role | Integer | 是 | 目标角色 | 1-求职者，2-企业HR |

### 返回参数

**成功响应**：
```json
{
  "code": 200,
  "message": "身份切换成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInJvbGUiOjIsImlhdCI6MTcxMDkzMTIwMCwiZXhwIjoxNzEwOTQ4NDAwfQ.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
    "userId": 1,
    "role": 2,
    "needSwitchRole": false,
    "currentRole": null
  },
  "timestamp": 1710931200000
}
```

**失败响应示例**：

1. 验证码已过期
```json
{
  "code": 400,
  "message": "验证码已过期或不存在",
  "data": null,
  "timestamp": 1710931200000
}
```

2. 用户不存在
```json
{
  "code": 400,
  "message": "用户不存在",
  "data": null,
  "timestamp": 1710931200000
}
```

3. 角色相同，无需切换
```json
{
  "code": 400,
  "message": "当前角色与目标角色相同，无需切换",
  "data": null,
  "timestamp": 1710931200000
}
```

### 业务说明
- 身份切换需要重新发送验证码并输入验证码（确保本人操作）
- 切换成功后，用户的角色会更新，并返回新的JWT令牌
- 前端应替换本地存储的JWT令牌
- 用户可以随时切换回原来的角色（重复上述流程）

---

## 业务流程图

### 正常登录流程
```
用户 → 发送验证码 → 输入验证码+角色 → 登录成功 → 获取JWT令牌
```

### 身份切换流程
```
用户 → 发送验证码 → 输入验证码+角色 → 检测到身份冲突
    → 前端弹窗确认 → 重新发送验证码 → 调用身份切换接口 → 获取新JWT令牌
```

---

## 测试示例（使用cURL）

### 场景1：新用户注册登录（求职者）
```bash
# 1. 发送验证码
curl -X POST http://localhost:8080/api/user/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "role": 1
  }'

# 2. 登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "code": "123456",
    "role": 1
  }'
```

### 场景2：身份切换（求职者切换为企业HR）
```bash
# 1. 发送验证码（提示身份切换）
curl -X POST http://localhost:8080/api/user/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "role": 2
  }'

# 2. 尝试登录（会返回needSwitchRole=true）
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "code": "123456",
    "role": 2
  }'

# 3. 重新发送验证码
curl -X POST http://localhost:8080/api/user/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "role": 2
  }'

# 4. 确认身份切换
curl -X POST http://localhost:8080/api/user/switch-role \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "code": "123456",
    "role": 2
  }'
```

---

## 前端实现建议

### 1. 登录页面逻辑
```javascript
// 发送验证码
async function sendCode(phone, role) {
  const response = await axios.post('/api/user/send-code', { phone, role });
  return response.data;
}

// 登录
async function login(phone, code, role) {
  const response = await axios.post('/api/user/login', { phone, code, role });
  const { data } = response.data;

  // 检查是否需要身份切换
  if (data.needSwitchRole) {
    // 弹窗提示用户
    const roleName = data.currentRole === 1 ? '求职者' : '企业HR';
    const targetName = role === 1 ? '求职者' : '企业HR';

    const confirmed = confirm(
      `检测到您已注册为${roleName}身份，\n` +
      `是否切换为${targetName}身份？\n\n` +
      `确认后需要重新输入验证码。`
    );

    if (confirmed) {
      // 跳转到身份切换流程
      showSwitchRoleDialog(phone, role);
    }
  } else {
    // 正常登录成功
    localStorage.setItem('token', data.token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('role', data.role);
    // 跳转到首页
    router.push('/home');
  }
}
```

### 2. 身份切换弹窗
```javascript
async function switchRole(phone, code, role) {
  try {
    const response = await axios.post('/api/user/switch-role', {
      phone,
      code,
      role
    });

    const { data } = response.data;

    // 切换成功，更新本地存储
    localStorage.setItem('token', data.token);
    localStorage.setItem('role', data.role);

    alert('身份切换成功！');
    // 刷新页面或跳转到对应角色首页
    window.location.reload();
  } catch (error) {
    alert('身份切换失败：' + error.response.data.message);
  }
}
```

---

## 注意事项

1. **手机号唯一性**：同一个手机号只能有一个账号，角色可以切换
2. **验证码安全性**：验证码存储在Redis中，设置5分钟过期，超过时间需要重新获取
3. **身份切换安全**：身份切换需要重新输入验证码，确保本人操作
4. **JWT令牌管理**：建议前端在登录成功后保存JWT令牌，并在后续需要认证的接口中携带
5. **自动注册**：首次使用手机号登录时，系统会自动创建用户，无需单独注册接口
6. **角色切换记录**：建议在前端或后端记录用户角色切换日志（可选）
