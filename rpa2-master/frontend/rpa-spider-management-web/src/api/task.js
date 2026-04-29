import request from '@/utils/request'

/**
 * 分页查询任务列表
 */
export function getTaskList(params) {
  return request({
    url: '/task/list',
    method: 'get',
    params
  })
}

/**
 * 创建任务
 */
export function createTask(data) {
  return request({
    url: '/task',
    method: 'post',
    data
  })
}

/**
 * 更新任务
 */
export function updateTask(id, data) {
  return request({
    url: `/task/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除任务
 */
export function deleteTask(id) {
  return request({
    url: `/task/${id}`,
    method: 'delete'
  })
}

/**
 * 获取任务详情
 */
export function getTaskById(id) {
  return request({
    url: `/task/${id}`,
    method: 'get'
  })
}

/**
 * 立即触发处理任务队列
 */
export function triggerQueue() {
  return request({
    url: '/task/queue/trigger',
    method: 'post'
  })
}

/**
 * 执行任务（默认异步推送给机器人）
 */
export function executeTask(id) {
  return request({
    url: `/task/${id}/execute/async`,
    method: 'post'
  })
}

/**
 * 执行任务（同步模式，在服务端直接执行）
 */
export function executeTaskSync(id) {
  return request({
    url: `/task/${id}/execute/sync`,
    method: 'post'
  })
}

/**
 * 获取流程列表（用于任务创建时选择流程）
 */
export function getProcessList(params) {
  return request({
    url: '/process/list',
    method: 'get',
    params
  })
}

/**
 * 获取可用机器人列表
 */
export function getAvailableRobots() {
  return request({
    url: '/robot/available',
    method: 'get'
  })
}

/**
 * 获取所有机器人列表（用于管理页面）
 */
export function getAllRobots(params) {
  return request({
    url: '/robot',
    method: 'get',
    params
  })
}

/**
 * 获取任务执行记录列表
 */
export function getExecutionList(params) {
  return request({
    url: '/task/execution/list',
    method: 'get',
    params
  })
}

/**
 * 批量删除任务
 */
export function batchDeleteTasks(ids) {
  return request({
    url: '/task/batch',
    method: 'delete',
    data: ids
  })
}
