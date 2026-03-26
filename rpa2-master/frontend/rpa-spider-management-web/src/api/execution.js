import request from '@/utils/request'

// 分页查询执行记录
export function getExecutionList(params) {
  return request({
    url: '/execution/list',
    method: 'get',
    params
  })
}

// 删除执行记录
export function deleteExecution(id) {
  return request({
    url: `/execution/${id}`,
    method: 'delete'
  })
}

// 获取执行记录详情
export function getExecutionDetail(id) {
  return request({
    url: `/execution/${id}`,
    method: 'get'
  })
}
