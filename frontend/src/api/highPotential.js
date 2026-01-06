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

export const deleteHighPotentialLead = (id) => {
  return request({
    url: `/highPotential/${id}`,
    method: 'delete'
  })
}

export const batchDeleteHighPotentialLeads = (ids) => {
  return request({
    url: '/highPotential/batchDelete',
    method: 'post',
    data: ids
  })
}

