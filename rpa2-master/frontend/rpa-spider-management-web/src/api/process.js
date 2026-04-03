import request from '@/utils/request'

// 分页查询流程
export function getProcessList(params) {
  return request({
    url: '/process/list',
    method: 'get',
    params
  })
}

// 创建流程
export function createProcess(data) {
  return request({
    url: '/process',
    method: 'post',
    data
  })
}

// 更新流程
export function updateProcess(id, data) {
  return request({
    url: `/process/${id}`,
    method: 'put',
    data
  })
}

// 删除流程
export function deleteProcess(id) {
  return request({
    url: `/process/${id}`,
    method: 'delete'
  })
}

// 获取流程详情
export function getProcessDetail(id) {
  return request({
    url: `/process/${id}`,
    method: 'get'
  })
}

// 获取流程脚本
export function getProcessScripts(id) {
  return request({
    url: `/process/${id}/scripts`,
    method: 'get'
  })
}

// 更新流程脚本
export function updateProcessScripts(id, data) {
  return request({
    url: `/process/${id}/scripts`,
    method: 'put',
    data
  })
}

// 校验单个 Groovy 脚本语法
export function validateGroovyScript(data) {
  return request({
    url: '/process/validate/groovy',
    method: 'post',
    data
  })
}

// 校验流程所有环节脚本
export function validateAllProcessSteps(id) {
  return request({
    url: `/process/${id}/validate/all`,
    method: 'post'
  })
}
