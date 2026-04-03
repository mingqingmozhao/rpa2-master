import request from '@/utils/request'

// 分页查询权限列表（扁平表格）
export function getResourceList(params) {
  return request({
    url: '/admin/permission/list',
    method: 'get',
    params
  })
}

// 获取权限树（用于父级选择 / 角色分配）
export function getResourceTree() {
  return request({
    url: '/admin/permission/tree',
    method: 'get'
  })
}

// 创建权限
export function createResource(data) {
  return request({
    url: '/admin/permission',
    method: 'post',
    data
  })
}

// 更新权限
export function updateResource(id, data) {
  return request({
    url: `/admin/permission/${id}`,
    method: 'put',
    data
  })
}

// 删除权限
export function deleteResource(id) {
  return request({
    url: `/admin/permission/${id}`,
    method: 'delete'
  })
}

// 获取权限详情
export function getResourceDetail(id) {
  return request({
    url: `/admin/permission/${id}`,
    method: 'get'
  })
}
