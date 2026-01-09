import request from './index'

export const recordActivity = (data) => {
  return request({
    url: '/activityRecord/record',
    method: 'post',
    data
  })
}

export const getActivityRecords = (params) => {
  return request({
    url: '/activityRecord/list',
    method: 'get',
    params
  })
}

export const getActivityCount = (leadNo) => {
  return request({
    url: '/activityRecord/count',
    method: 'get',
    params: { leadNo }
  })
}

