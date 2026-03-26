import request from '@/utils/request'

// 分页查询任务
export function getTaskList(params) {
  return request({
    url: '/task/list',
    method: 'get',
    params
  })
}

// 创建任务
export function createTask(data) {
  return request({
    url: '/task',
    method: 'post',
    data
  })
}

// 更新任务
export function updateTask(id, data) {
  return request({
    url: `/task/${id}`,
    method: 'put',
    data
  })
}

// 删除任务
export function deleteTask(id) {
  return request({
    url: `/task/${id}`,
    method: 'delete'
  })
}

// 获取任务详情
export function getTaskDetail(id) {
  return request({
    url: `/task/${id}`,
    method: 'get'
  })
}

// 执行任务
export function executeTask(id) {
  return request({
    url: `/task/${id}/execute`,
    method: 'post'
  })
}
