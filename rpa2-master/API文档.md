# RPA系统认证模块 - 前端接口文档

## 基础信息

| 项目 | 值 |
|------|-----|
| 基础URL | `http://localhost:8080`（开发环境）|
| 接口前缀 | `/api/auth` |
| 数据格式 | JSON |
| 编码 | UTF-8 |

---

## 统一响应格式

所有接口返回统一格式：

```json
{
    "code": 200,      // 状态码：200成功，401未认证，403无权限，400参数错误，500系统错误
    "msg": "success", // 消息说明
    "data": {}        // 业务数据（可能为null）
}
```

---

## 1. 登录接口

### 请求信息
- **URL**: `POST /api/auth/login`
- **Content-Type**: `application/json`
- **无需认证**

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |
| rememberMe | boolean | 否 | 记住我（默认false）|

### 请求示例
```json
{
    "username": "admin",
    "password": "admin123",
    "rememberMe": false
}
```

### 成功响应（HTTP 200）
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "token": "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4ifQ...",
        "tokenType": "Bearer",
        "expiresIn": 7200,
        "userInfo": {
            "userId": 1,
            "username": "admin",
            "nickname": "管理员",
            "email": "admin@example.com",
            "phone": "13800138000",
            "avatar": null,
            "status": 1
        },
        "roles": ["USER"],
        "permissions": []
    }
}
```

### 失败响应

**用户名或密码错误（HTTP 401）**
```json
{
    "code": 401,
    "msg": "用户名或密码错误",
    "data": null
}
```

**账号已禁用（HTTP 401）**
```json
{
    "code": 401,
    "msg": "账号已禁用",
    "data": null
}
```

**参数校验失败（HTTP 400）**
```json
{
    "code": 400,
    "msg": "参数校验失败",
    "data": {
        "username": "用户名不能为空",
        "password": "密码不能为空"
    }
}
```

---

## 2. 退出登录接口

### 请求信息
- **URL**: `POST /api/auth/logout`
- **无需认证**（但建议携带Token）

### 请求头
```
Authorization: Bearer {token}
```

### 成功响应（HTTP 200）
```json
{
    "code": 200,
    "msg": "success",
    "data": null
}
```

### 前端处理逻辑
1. 调用退出接口
2. 清除本地存储的Token
3. 跳转到登录页

---

## 3. 获取当前用户信息接口

### 请求信息
- **URL**: `GET /api/auth/user-info`
- **需要认证**

### 请求头
```
Authorization: Bearer {token}
```

### 成功响应（HTTP 200）
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "userId": 1,
        "username": "admin",
        "nickname": "管理员",
        "email": "admin@example.com",
        "phone": "13800138000",
        "avatar": null,
        "status": 1,
        "roles": ["USER"],
        "permissions": []
    }
}
```

### 失败响应

**未认证（HTTP 401）**
```json
{
    "code": 401,
    "msg": "未登录或Token已过期",
    "data": null
}
```

---

## 前端实现逻辑

### 1. Token管理

```javascript
// 存储Token
localStorage.setItem('token', response.data.token);
localStorage.setItem('expiresIn', response.data.expiresIn);

// 获取Token
const token = localStorage.getItem('token');

// 清除Token
localStorage.removeItem('token');
localStorage.removeItem('expiresIn');
```

### 2. 请求拦截器（添加Token）

```javascript
// Axios示例
axios.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => Promise.reject(error)
);
```

### 3. 响应拦截器（处理401）

```javascript
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            // Token过期或无效，清除本地存储并跳转到登录页
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);
```

### 4. 登录流程

```javascript
async function login(username, password, rememberMe) {
    try {
        const response = await axios.post('/api/auth/login', {
            username,
            password,
            rememberMe
        });
        
        if (response.data.code === 200) {
            // 保存Token
            localStorage.setItem('token', response.data.data.token);
            
            // 保存用户信息（可选）
            localStorage.setItem('userInfo', JSON.stringify(response.data.data.userInfo));
            
            // 跳转到首页
            window.location.href = '/';
        }
    } catch (error) {
        // 显示错误信息
        alert(error.response?.data?.msg || '登录失败');
    }
}
```

### 5. 退出流程

```javascript
async function logout() {
    try {
        // 调用退出接口（可选，因为后端只是简单实现）
        await axios.post('/api/auth/logout');
    } catch (error) {
        console.error('退出失败', error);
    } finally {
        // 无论成功与否，都清除本地数据
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        window.location.href = '/login';
    }
}
```

### 6. 页面刷新获取用户信息

```javascript
// 在应用初始化时调用
async function initUserInfo() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login';
        return;
    }
    
    try {
        const response = await axios.get('/api/auth/user-info');
        if (response.data.code === 200) {
            // 保存用户信息到状态管理（Vuex/Pinia/Redux等）
            store.setUserInfo(response.data.data);
        }
    } catch (error) {
        // 获取失败，可能是Token过期
        window.location.href = '/login';
    }
}
```

---

## 错误码对照表

| code | HTTP状态码 | 说明 | 处理方式 |
|------|-----------|------|----------|
| 200 | 200 | 成功 | 正常处理 |
| 400 | 400 | 参数错误 | 提示用户检查输入 |
| 401 | 401 | 未认证/Token过期 | 清除Token，跳转登录页 |
| 403 | 403 | 无权限 | 提示用户无权访问 |
| 500 | 500 | 系统错误 | 提示系统繁忙，稍后重试 |

---

## 测试账号

```
用户名：admin
密码：admin123
```

---

## CORS说明

后端已开启跨域，开发环境允许所有来源访问。如需指定前端域名，请联系后端人员配置。

---

## 更新日志

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-03-20 | v1.0 | 初始版本，包含登录、退出、获取用户信息接口 |
