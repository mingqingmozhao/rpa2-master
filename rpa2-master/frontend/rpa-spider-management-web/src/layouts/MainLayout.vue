<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-left">
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          class="top-menu"
          router
          :ellipsis="false"
        >
          <el-menu-item index="/dashboard">首页</el-menu-item>
          <el-menu-item index="/task">RPA 运营管理</el-menu-item>
          <el-menu-item index="/user-info">系统管理</el-menu-item>
        </el-menu>
      </div>
      
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" :src="headerAvatarUrl">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span class="username">{{ userStore.userInfo?.username || '系统管理员' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container>
      <el-aside width="240px" class="aside">
        <div class="menu-list">
          <template v-for="menu in menus" :key="menu.index || menu.title">
            <div 
              v-if="menu.children && menu.children.length" 
              class="menu-group"
              :class="{ 'is-expanded': expandedMenu === menu.title }"
            >
              <div class="group-header" @click="toggleMenu(menu.title)">
                <el-icon class="menu-icon"><component :is="menu.icon" /></el-icon>
                <span class="menu-title">{{ menu.title }}</span>
                <el-icon class="arrow-icon" :class="{ 'is-rotated': expandedMenu === menu.title }">
                  <ArrowDown />
                </el-icon>
              </div>
              <el-collapse-transition>
                <div v-show="expandedMenu === menu.title" class="group-items">
                  <router-link
                    v-for="child in menu.children"
                    :key="child.path"
                    :to="child.path"
                    class="menu-item"
                    :class="{ 'is-active': route.path === child.path }"
                  >
                    {{ child.title }}
                  </router-link>
                </div>
              </el-collapse-transition>
            </div>
            <div v-else class="menu-item-wrapper">
              <el-icon><component :is="menu.icon" /></el-icon>
              <router-link :to="menu.path" class="menu-item" :class="{ 'is-active': route.path === menu.path }">
                {{ menu.title }}
              </router-link>
            </div>
          </template>
        </div>
      </el-aside>
      
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UserFilled, ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前展开的菜单
const expandedMenu = ref('任务管理')

// 根据当前路由自动展开对应的菜单
watch(() => route.path, (newPath) => {
  if (newPath.startsWith('/task') || newPath.startsWith('/execution') || newPath.startsWith('/batch')) {
    expandedMenu.value = '任务管理'
  } else if (newPath.startsWith('/schedule')) {
    expandedMenu.value = '任务管理'
  } else if (newPath.startsWith('/robot')) {
    expandedMenu.value = '机器人管理'
  } else if (newPath.startsWith('/process')) {
    expandedMenu.value = '流程管理'
  } else if (newPath.startsWith('/data')) {
    expandedMenu.value = '数据管理'
  } else if (newPath.startsWith('/user-info') || newPath.startsWith('/user-management') || 
             newPath.startsWith('/role-management') || newPath.startsWith('/resource-management')) {
    expandedMenu.value = '系统管理'
  }
}, { immediate: true })

const activeMenu = computed(() => {
  const path = route.path
  if (path === '/dashboard') return '/dashboard'
  if (path.startsWith('/task') || path.startsWith('/execution') || path.startsWith('/robot') || path.startsWith('/process') || path.startsWith('/batch') || path.startsWith('/schedule')) {
    return '/task'
  }
  if (path.startsWith('/data')) {
    return '/task'
  }
  if (path.startsWith('/user-info') || path.startsWith('/user-management') || path.startsWith('/role-management') || path.startsWith('/resource-management')) {
    return '/user-info'
  }
  return '/dashboard'
})

// 切换菜单展开/收起
const toggleMenu = (menuTitle) => {
  if (expandedMenu.value === menuTitle) {
    expandedMenu.value = null
  } else {
    expandedMenu.value = menuTitle
  }
}

const allMenus = [
  {
    title: '任务管理',
    icon: 'Document',
    path: '/task',
    children: [
      { title: '任务列表', path: '/task' },
      { title: '执行记录', path: '/execution' },
      { title: '批量执行', path: '/batch' },
      { title: '定时计划', path: '/schedule', roles: ['OPERATOR', 'ADMIN'] }
    ]
  },
  {
    title: '机器人管理',
    icon: 'VideoCamera',
    index: '2',
    roles: ['OPERATOR', 'ADMIN'],
    children: [
      { title: '机器人列表', path: '/robot' }
    ]
  },
  {
    title: '流程管理',
    icon: 'Connection',
    index: '3',
    roles: ['OPERATOR', 'ADMIN'],
    children: [
      { title: '流程列表', path: '/process' }
    ]
  },
  {
    title: '数据管理',
    icon: 'DataLine',
    index: '4',
    children: [
      { title: '数据采集', path: '/data-collection' },
      { title: '数据解析', path: '/data-parsing' },
      { title: '数据加工', path: '/data-processing' },
      { title: '数据落库', path: '/data-content' },
      { title: '数据查询', path: '/data-query' }
    ]
  },
  {
    title: '系统管理',
    icon: 'Setting',
    index: '5',
    children: [
      { title: '个人信息', path: '/user-info' },
      { title: '用户管理', path: '/user-management', roles: ['ADMIN'] },
      { title: '角色管理', path: '/role-management', roles: ['ADMIN'] },
      { title: '资源管理', path: '/resource-management', roles: ['ADMIN'] }
    ]
  }
]

const normalizedRoles = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.map(role => String(role).replace(/^ROLE_/, '').toUpperCase())
})

const hasPermission = (requiredRoles) => {
  if (!requiredRoles || requiredRoles.length === 0) return true
  return requiredRoles.some(role => normalizedRoles.value.includes(String(role).toUpperCase()))
}

const menus = computed(() => allMenus
  .filter(menu => hasPermission(menu.roles))
  .map(menu => {
    if (!menu.children) return menu
    return {
      ...menu,
      children: menu.children.filter(child => hasPermission(child.roles))
    }
  })
  .filter(menu => !menu.children || menu.children.length > 0)
)

const headerAvatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar
  if (!avatar) return ''
  if (avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('/')) {
    return avatar
  }
  return avatar
})

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确认退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    })
  } else if (command === 'profile') {
    router.push('/user-info')
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  
  .header-left {
    display: flex;
    align-items: center;
    
    .top-menu {
      background: transparent;
      border: none;
      min-width: 420px;
      
      :deep(.el-menu-item) {
        color: rgba(255, 255, 255, 0.85);
        font-weight: 500;
        border-radius: 6px;
        margin: 0 4px;
        min-width: max-content;
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(255, 255, 255, 0.15);
          color: #fff;
          transform: translateY(-1px);
        }
        
        &.is-active {
          background: rgba(255, 255, 255, 0.25);
          color: #fff;
          box-shadow: 0 2px 8px rgba(255, 255, 255, 0.1);
        }
      }
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      color: #fff;
      cursor: pointer;
      padding: 6px 12px;
      border-radius: 8px;
      background: rgba(255, 255, 255, 0.1);
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(255, 255, 255, 0.2);
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }
      
      .username {
        font-size: 14px;
        font-weight: 500;
      }
    }
  }
}

.aside {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  width: 240px !important;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.1);
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }

  .menu-list {
    padding: 16px 12px;
  }

  .menu-group {
    margin-bottom: 8px;
    padding: 0;
    background: rgba(255, 255, 255, 0.03);
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:hover {
      background: rgba(255, 255, 255, 0.06);
      transform: translateX(2px);
    }
    
    &.is-expanded {
      background: rgba(255, 255, 255, 0.05);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .group-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
      padding: 14px 16px;
      color: #e2e8f0;
      font-size: 13px;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
      background: linear-gradient(90deg, rgba(59, 130, 246, 0.1) 0%, transparent 100%);
      cursor: pointer;
      transition: all 0.3s ease;
      user-select: none;
      
      &:hover {
        background: linear-gradient(90deg, rgba(59, 130, 246, 0.2) 0%, rgba(59, 130, 246, 0.05) 100%);
        
        .menu-icon {
          transform: scale(1.1);
          filter: drop-shadow(0 0 8px rgba(96, 165, 250, 0.6));
        }
      }
      
      .menu-icon {
        font-size: 18px;
        color: #60a5fa;
        width: 20px;
        flex-shrink: 0;
        filter: drop-shadow(0 0 4px rgba(96, 165, 250, 0.4));
        transition: all 0.3s ease;
      }
      
      .menu-title {
        flex: 1;
        min-width: 0;
      }
      
      .arrow-icon {
        font-size: 16px;
        color: #94a3b8;
        width: 18px;
        flex-shrink: 0;
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        
        &.is-rotated {
          transform: rotate(180deg);
          color: #60a5fa;
        }
      }
    }

    .group-items {
      display: flex;
      flex-direction: column;
      gap: 6px;
      padding: 12px;
      background: rgba(0, 0, 0, 0.1);

      .menu-item {
        padding: 10px 14px;
        border-radius: 8px;
        font-size: 13px;
        font-weight: 500;
        text-decoration: none;
        color: #94a3b8;
        background: rgba(255, 255, 255, 0.05);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        white-space: nowrap;
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 3px;
          height: 100%;
          background: #3b82f6;
          opacity: 0;
          transition: opacity 0.3s ease;
        }
        
        &:hover {
          background: linear-gradient(135deg, rgba(59, 130, 246, 0.25) 0%, rgba(147, 51, 234, 0.15) 100%);
          color: #60a5fa;
          transform: translateX(6px);
          box-shadow: 0 4px 12px rgba(59, 130, 246, 0.25);
          
          &::before {
            opacity: 1;
          }
        }

        &.is-active {
          background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
          color: #fff;
          box-shadow: 0 4px 16px rgba(59, 130, 246, 0.4);
          transform: translateX(6px);
          font-weight: 600;
          
          &::before {
            opacity: 1;
          }
        }
      }
    }
  }

  .menu-item-wrapper {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    margin: 8px 0;
    border-radius: 8px;
    color: #94a3b8;
    background: rgba(255, 255, 255, 0.03);
    transition: all 0.3s ease;
    
    &:hover {
      background: rgba(255, 255, 255, 0.06);
      transform: translateX(4px);
    }

    .el-icon {
      font-size: 18px;
      color: #60a5fa;
      transition: all 0.3s ease;
    }
    
    &:hover .el-icon {
      transform: scale(1.1) rotate(5deg);
      filter: drop-shadow(0 0 6px rgba(96, 165, 250, 0.6));
    }

    .menu-item {
      font-size: 14px;
      font-weight: 500;
      text-decoration: none;
      color: #94a3b8;
      transition: all 0.3s ease;
      flex: 1;

      &:hover, &.is-active {
        color: #60a5fa;
        font-weight: 600;
      }
    }
  }
}

.main {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  padding: 24px;
  box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.05);
  min-height: 500px;
  overflow-y: auto;
}
</style>
