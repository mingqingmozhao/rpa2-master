import request from '@/utils/request'

// 获取权限树（用于角色分配时勾选权限）
export function getPermissionTree() {
  return request({
    url: '/admin/permission/tree',
    method: 'get'
  })
}

// 分页查询角色列表
export function getRoleList(params) {
  return request({
    url: '/admin/role/list',
    method: 'get',
    params
  })
}

// 创建角色
export function createRole(data) {
  return request({
    url: '/admin/role',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(id, data) {
  return request({
    url: `/admin/role/${id}`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/admin/role/${id}`,
    method: 'delete'
  })
}

// 获取角色详情
export function getRoleDetail(id) {
  return request({
    url: `/admin/role/${id}`,
    method: 'get'
  })
}

// 分配权限
export function assignPermissions(id, permissionIds) {
  return request({
    url: `/admin/role/${id}/permissions`,
    method: 'put',
    data: { permissionIds }
  })
}

// 获取角色权限
export function getRolePermissions(id) {
  return request({
    url: `/admin/role/${id}/permissions`,
    method: 'get'
  })
}
