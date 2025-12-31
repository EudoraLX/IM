import request from './index'

export const setupTestData = (leadCount = 20) => {
  return request({
    url: '/test/setupTestData',
    method: 'post',
    params: { leadCount }
  })
}

export const createTestLeads = (count = 10) => {
  return request({
    url: '/test/createTestLeads',
    method: 'post',
    params: { count }
  })
}

export const processData = () => {
  return request({
    url: '/dataProcessing/process',
    method: 'post'
  })
}

