import request from './index'

export const getLeadList = (params) => {
  return request({
    url: '/lead/list',
    method: 'get',
    params
  })
}

export const getLeadById = (id) => {
  return request({
    url: `/lead/${id}`,
    method: 'get'
  })
}

export const getLeadByLeadNo = (leadNo) => {
  return request({
    url: `/lead/byLeadNo/${leadNo}`,
    method: 'get'
  })
}

export const addLead = (data) => {
  return request({
    url: '/lead/add',
    method: 'post',
    data
  })
}

export const updateLead = (data) => {
  return request({
    url: '/lead/update',
    method: 'put',
    data
  })
}

export const deleteLead = (id) => {
  return request({
    url: `/lead/${id}`,
    method: 'delete'
  })
}

export const batchDeleteLeads = (ids) => {
  return request({
    url: '/lead/batchDelete',
    method: 'post',
    data: ids
  })
}

export const batchImportLeads = (leads) => {
  return request({
    url: '/lead/batchImport',
    method: 'post',
    data: leads
  })
}

