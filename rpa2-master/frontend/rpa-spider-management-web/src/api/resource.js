import request from '@/utils/request'

// 分页查询资源列表
export function getResourceList(params) {
  return request({
    url: '/resource/list',
    method: 'get',
    params
  })
}

// 创建资源
export function createResource(data) {
  return request({
    url: '/resource',
    method: 'post',
    data
  })
}

// 更新资源
export function updateResource(id, data) {
  return request({
    url: `/resource/${id}`,
    method: 'put',
    data
  })
}

// 删除资源
export function deleteResource(id) {
  return request({
    url: `/resource/${id}`,
    method: 'delete'
  })
}

// 获取资源详情
export function getResourceDetail(id) {
  return request({
    url: `/resource/${id}`,
    method: 'get'
  })
}

// 获取资源树
export function getResourceTree() {
  return request({
    url: '/resource/tree',
    method: 'get'
  })
}
