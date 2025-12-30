import request from './index'

export const getHighPotentialLeadList = (params) => {
  return request({
    url: '/highPotential/list',
    method: 'get',
    params
  })
}

export const getStatistics = () => {
  return request({
    url: '/highPotential/statistics',
    method: 'get'
  })
}

export const batchCall = (ids) => {
  return request({
    url: '/highPotential/batchCall',
    method: 'post',
    data: ids
  })
}

export const batchSendSms = (ids) => {
  return request({
    url: '/highPotential/batchSms',
    method: 'post',
    data: ids
  })
}

export const updateLeadStatus = (id, status) => {
  return request({
    url: '/highPotential/status',
    method: 'put',
    params: { id, status }
  })
}

