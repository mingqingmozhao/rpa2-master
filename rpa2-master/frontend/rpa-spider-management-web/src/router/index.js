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
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
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
  
  // 检查角色权限
  const requiredRoles = to.meta?.roles
  if (requiredRoles && requiredRoles.length > 0) {
    const userRoles = userStore.userInfo?.roles || []
    if (userRoles.length === 0) {
      next('/dashboard')
      return
    }
    const hasPermission = requiredRoles.some(role => userRoles.includes(role))

    if (!hasPermission) {
      next('/dashboard')
      return
    }
  }
  
  next()
})

export default router
