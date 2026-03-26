<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-left">
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          class="top-menu"
          router
        >
          <el-menu-item index="/dashboard">首页</el-menu-item>
          <el-menu-item index="/task">RPA 运营管理</el-menu-item>
          <el-menu-item index="/user-info">系统管理</el-menu-item>
        </el-menu>
      </div>
      
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" :icon="UserFilled" />
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
      <el-aside width="200px" class="aside">
        <el-menu
          :default-active="activeSubmenu"
          class="side-menu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <template v-for="menu in menus" :key="menu.index">
            <el-sub-menu v-if="menu.children && menu.children.length" :index="menu.index">
              <template #title>
                <el-icon><component :is="menu.icon" /></el-icon>
                <span>{{ menu.title }}</span>
              </template>
              <el-menu-item
                v-for="child in menu.children"
                :key="child.index"
                :index="child.path"
              >
                {{ child.title }}
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="menu.path">
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UserFilled } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path === '/dashboard') return '/dashboard'
  if (path.startsWith('/task') || path.startsWith('/execution') || path.startsWith('/robot') || path.startsWith('/process')) {
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

const activeSubmenu = computed(() => route.path)

const menus = ref([
  {
    title: '任务管理',
    icon: 'Document',
    index: '1',
    children: [
      { title: '任务列表', path: '/task' },
      { title: '执行记录', path: '/execution' }
    ]
  },
  {
    title: '机器人管理',
    icon: 'VideoCamera',
    index: '2',
    children: [
      { title: '机器人列表', path: '/robot' }
    ]
  },
  {
    title: '流程管理',
    icon: 'Connection',
    index: '3',
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
      { title: '数据查询', path: '/data-query' }
    ]
  },
  {
    title: '系统管理',
    icon: 'Setting',
    index: '5',
    children: [
      { title: '个人信息', path: '/user-info' },
      { title: '用户管理', path: '/user-management' },
      { title: '角色管理', path: '/role-management' },
      { title: '资源管理', path: '/resource-management' }
    ]
  }
])

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
}

.header {
  background: #1e3a8a;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  
  .header-left {
    display: flex;
    align-items: center;
    
    .top-menu {
      background: transparent;
      border: none;
      
      :deep(.el-menu-item) {
        color: rgba(255, 255, 255, 0.8);
        
        &:hover, &.is-active {
          color: #fff;
        }
      }
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #fff;
      cursor: pointer;
      
      .username {
        font-size: 14px;
      }
    }
  }
}

.aside {
  background: #304156;
  
  .side-menu {
    border-right: none;
  }
}

.main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
