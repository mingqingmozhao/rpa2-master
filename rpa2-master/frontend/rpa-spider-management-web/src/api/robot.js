import request from '@/utils/request'

// 分页查询机器人
export function getRobotList(params) {
  return request({
    url: '/robot/list',
    method: 'get',
    params
  })
}

// 创建机器人
export function createRobot(data) {
  return request({
    url: '/robot',
    method: 'post',
    data
  })
}

// 更新机器人
export function updateRobot(id, data) {
  return request({
    url: `/robot/${id}`,
    method: 'put',
    data
  })
}

// 删除机器人
export function deleteRobot(id) {
  return request({
    url: `/robot/${id}`,
    method: 'delete'
  })
}

// 获取机器人详情
export function getRobotDetail(id) {
  return request({
    url: `/robot/${id}`,
    method: 'get'
  })
}

// 获取机器人状态
export function getRobotStatus(id) {
  return request({
    url: `/robot/${id}/status`,
    method: 'get'
  })
}

// 更新机器人心跳
export function updateRobotHeartbeat(id, data) {
  return request({
    url: `/robot/${id}/heartbeat`,
    method: 'post',
    data
  })
}
