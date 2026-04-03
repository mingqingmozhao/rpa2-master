import request from '@/utils/request'

/**
 * 分页查询机器人列表
 */
export function getRobotList(params) {
  return request({
    url: '/robot',
    method: 'get',
    params
  })
}

/**
 * 根据 ID 查询机器人详情
 */
export function getRobotById(id) {
  return request({
    url: `/robot/${id}`,
    method: 'get'
  })
}

/**
 * 创建机器人
 */
export function createRobot(data) {
  return request({
    url: '/robot',
    method: 'post',
    data
  })
}

/**
 * 更新机器人
 */
export function updateRobot(id, data) {
  return request({
    url: `/robot/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除机器人
 */
export function deleteRobot(id) {
  return request({
    url: `/robot/${id}`,
    method: 'delete'
  })
}

/**
 * 获取机器人状态统计
 */
export function getRobotStatus() {
  return request({
    url: '/robot/status',
    method: 'get'
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
 * 更新机器人状态
 */
export function updateRobotStatus(id, status) {
  return request({
    url: `/robot/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 更新心跳时间
 */
export function updateRobotHeartbeat(id) {
  return request({
    url: `/robot/${id}/heartbeat`,
    method: 'post'
  })
}
