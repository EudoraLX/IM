import request from './index'

export const getPushConfig = () => {
  return request({
    url: '/push/config',
    method: 'get'
  })
}

export const savePushConfig = (data) => {
  return request({
    url: '/push/save',
    method: 'post',
    data
  })
}

export const resetPushConfig = () => {
  return request({
    url: '/push/reset',
    method: 'post'
  })
}

