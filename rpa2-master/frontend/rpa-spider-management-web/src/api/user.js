import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 用户退出
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/auth/user-info',
    method: 'get'
  })
}

// 修改个人资料
export function updateUserInfo(data) {
  return request({
    url: '/auth/profile',
    method: 'put',
    data
  })
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/auth/password',
    method: 'post',
    data
  })
}

// 分页查询用户列表
export function getUserList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

// 创建用户
export function createUser(data) {
  return request({
    url: '/admin/user',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(id, data) {
  return request({
    url: `/admin/user/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'delete'
  })
}

// 获取用户详情
export function getUserDetail(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'get'
  })
}

// 重置密码
export function resetPassword(id, newPassword) {
  return request({
    url: `/admin/user/reset-password`,
    method: 'post',
    data: { userId: id, newPassword }
  })
}

// 上传头像
export function uploadAvatar(data) {
  return request({
    url: '/auth/avatar',
    method: 'post',
    data
    // Content-Type 会自动由 axios 设置为 multipart/form-data 并带上 boundary
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/admin/user/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 分配用户角色（Body 方式）
export function assignUserRole(id, roleId) {
  return request({
    url: `/admin/user/${id}/role`,
    method: 'put',
    data: { roleId }
  })
}

// 获取角色列表
export function getRoleList(params) {
  return request({
    url: '/admin/role/list',
    method: 'get',
    params
  })
}
