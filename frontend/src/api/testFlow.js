import request from './index'

export const getFlowStatus = () => {
  return request({
    url: '/testFlow/status',
    method: 'get'
  })
}

export const getFlowPreview = (leadCount = 20) => {
  return request({
    url: '/testFlow/preview',
    method: 'get',
    params: { leadCount }
  })
}

export const executeFullFlow = (leadCount = 20) => {
  return request({
    url: '/testFlow/execute',
    method: 'post',
    params: { leadCount }
  })
}

