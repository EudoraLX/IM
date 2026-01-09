import request from './index'

export const getMonitorConfig = () => {
  return request({
    url: '/monitor/config',
    method: 'get'
  })
}

export const saveMonitorConfig = (data) => {
  return request({
    url: '/monitor/save',
    method: 'post',
    data
  })
}

export const getMonitorStatistics = () => {
  return request({
    url: '/monitor/statistics',
    method: 'get'
  })
}

export const getOrganizations = () => {
  return request({
    url: '/monitor/organizations',
    method: 'get'
  })
}

export const refreshMonitor = () => {
  return request({
    url: '/dataProcessing/process',
    method: 'post'
  })
}

