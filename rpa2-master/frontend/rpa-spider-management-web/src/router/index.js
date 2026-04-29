import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'task',
        name: 'Task',
        component: () => import('@/views/task/TaskList.vue'),
        meta: { requiresAuth: true, roles: ['BUSINESS', 'OPERATOR', 'ADMIN'] }
      },
      {
        path: 'execution',
        name: 'Execution',
        component: () => import('@/views/execution/ExecutionList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'execution/detail',
        name: 'ExecutionDetail',
        component: () => import('@/views/execution/ExecutionDetail.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'robot',
        name: 'Robot',
        component: () => import('@/views/robot/RobotList.vue'),
        meta: { requiresAuth: true, roles: ['OPERATOR', 'ADMIN'] }
      },
      {
        path: 'robot/detail/:id',
        name: 'RobotDetail',
        component: () => import('@/views/robot/RobotDetail.vue'),
        meta: { requiresAuth: true, roles: ['OPERATOR', 'ADMIN'] }
      },
      {
        path: 'robot/websocket',
        name: 'RobotWebSocket',
        component: () => import('@/views/robot/RobotWebSocket.vue'),
        meta: { requiresAuth: true, roles: ['OPERATOR', 'ADMIN'] }
      },
      {
        path: 'process',
        name: 'Process',
        component: () => import('@/views/process/ProcessList.vue'),
        meta: { requiresAuth: true, roles: ['OPERATOR', 'ADMIN'] }
      },
      {
        path: 'data-collection',
        name: 'DataCollection',
        component: () => import('@/views/data/CollectionList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'data-parsing',
        name: 'DataParsing',
        component: () => import('@/views/data/ParsingList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'data-processing',
        name: 'DataProcessing',
        component: () => import('@/views/data/ProcessingList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'data-content',
        name: 'DataContent',
        component: () => import('@/views/data/ContentList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'data-query',
        name: 'DataQuery',
        component: () => import('@/views/data/QueryList.vue'),
        meta: { requiresAuth: true }
      },

      {
        path: 'user-info',
        name: 'UserInfo',
        component: () => import('@/views/system/UserInfo.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'user-management',
        name: 'UserManagement',
        component: () => import('@/views/system/UserManagement.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'role-management',
        name: 'RoleManagement',
        component: () => import('@/views/system/RoleManagement.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'resource-management',
        name: 'ResourceManagement',
        component: () => import('@/views/system/ResourceManagement.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'batch',
        name: 'Batch',
        component: () => import('@/views/execution/BatchExecutionList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/task/ScheduleList.vue'),
        meta: { requiresAuth: true, roles: ['OPERATOR', 'ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let userInfoLoading = null

async function ensureUserInfo(userStore) {
  if (userStore.userInfo) return true
  if (!userStore.token) return false

  if (!userInfoLoading) {
    userInfoLoading = fetch('/auth/user-info', {
      headers: {
        Authorization: `Bearer ${userStore.token}`
      }
    })
      .then(async response => {
        if (!response.ok) {
          throw new Error('获取用户信息失败')
        }

        const res = await response.json()
        if (Object.prototype.hasOwnProperty.call(res, 'code') && res.code !== 200) {
          throw new Error(res.message || '获取用户信息失败')
        }

        userStore.setUserInfo(res.data || res)
      })
      .finally(() => {
        userInfoLoading = null
      })
  }

  try {
    await userInfoLoading
    return Boolean(userStore.userInfo)
  } catch (error) {
    console.error('[Router] Failed to restore user info:', error)
    return false
  }
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 不需要登录的页面
  if (to.path === '/login') {
    if (userStore.token) {
      next('/')
    } else {
      next()
    }
    return
  }
  
  // 需要登录的页面
  if (!userStore.token) {
    next('/login')
    return
  }
  
  const hasUserInfo = await ensureUserInfo(userStore)
  if (!hasUserInfo) {
    userStore.logout()
    next('/login')
    return
  }
  
  next()
})

export default router
