import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Layout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/lead',
    children: [
      {
        path: '/lead',
        name: 'LeadManagement',
        component: () => import('../views/LeadManagement.vue'),
        meta: { title: '线索管理' }
      },
      {
        path: '/monitor',
        name: 'MonitorIndicator',
        component: () => import('../views/MonitorIndicator.vue'),
        meta: { title: '监控指标管理' }
      },
      {
        path: '/push',
        name: 'PushFrequency',
        component: () => import('../views/PushFrequency.vue'),
        meta: { title: '推送频率管理' }
      },
      {
        path: '/highPotential',
        name: 'HighPotentialLead',
        component: () => import('../views/HighPotentialLead.vue'),
        meta: { title: '高潜线索营销' }
      },
      {
        path: '/testFlow',
        name: 'TestFlow',
        component: () => import('../views/TestFlow.vue'),
        meta: { title: '完整测试流程' }
      },
      {
        path: '/activityRecord',
        name: 'ActivityRecord',
        component: () => import('../views/ActivityRecord.vue'),
        meta: { title: '活跃次数记录' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

