<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #667eea;">
              <el-icon :size="40"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalTasks }}</div>
              <div class="stat-label">总任务数</div>
              <div class="stat-trend">今日新增 {{ stats.todayTasks }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f687b3;">
              <el-icon :size="40"><VideoCamera /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalRobots }}</div>
              <div class="stat-label">机器人总数</div>
              <div class="stat-trend" style="color: #48bb78;">在线 {{ stats.onlineRobots }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #4fd1c5;">
              <el-icon :size="40"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalProcesses }}</div>
              <div class="stat-label">流程总数</div>
              <div class="stat-trend">启用 {{ stats.enabledProcesses }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #68d391;">
              <el-icon :size="40"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalData }}</div>
              <div class="stat-label">数据总量</div>
              <div class="stat-trend">今日采集 {{ stats.todayData }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 任务状态概览 -->
    <el-row :gutter="20" class="status-row">
      <el-col :span="18">
        <el-card class="status-card">
          <template #header>
            <div class="card-header">
              <span>任务状态概览</span>
              <el-link type="primary" @click="goToTaskList">查看详情 <el-icon><ArrowRight /></el-icon></el-link>
            </div>
          </template>
          
          <div class="status-chart">
            <div class="status-item">
              <div class="status-dot" style="background: #ed8936;"></div>
              <div class="status-text">
                <span class="status-value">{{ taskStatus.running }}</span>
                <span class="status-label">运行中</span>
              </div>
            </div>
            <div class="status-item">
              <div class="status-dot" style="background: #a0aec0;"></div>
              <div class="status-text">
                <span class="status-value">{{ taskStatus.pending }}</span>
                <span class="status-label">待执行</span>
              </div>
            </div>
            <div class="status-item">
              <div class="status-dot" style="background: #48bb78;"></div>
              <div class="status-text">
                <span class="status-value">{{ taskStatus.completed }}</span>
                <span class="status-label">已完成</span>
              </div>
            </div>
            <div class="status-item">
              <div class="status-dot" style="background: #f56565;"></div>
              <div class="status-text">
                <span class="status-value">{{ taskStatus.failed }}</span>
                <span class="status-label">失败</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="quick-card">
          <template #header>
            <span>快捷入口</span>
          </template>
          
          <div class="quick-list">
            <div class="quick-item" @click="goToCreateTask">
              <div class="quick-icon" style="background: #9f7aea;">
                <el-icon><Plus /></el-icon>
              </div>
              <div class="quick-info">
                <div class="quick-title">创建任务</div>
                <div class="quick-desc">快速创建新的 RPA 任务</div>
              </div>
            </div>
            
            <div class="quick-item" @click="goToProcessList">
              <div class="quick-icon" style="background: #4fd1c5;">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="quick-info">
                <div class="quick-title">流程定义</div>
                <div class="quick-desc">定义和管理 RPA 流程</div>
              </div>
            </div>
            
            <div class="quick-item" @click="goToRobotList">
              <div class="quick-icon" style="background: #f687b3;">
                <el-icon><VideoCamera /></el-icon>
              </div>
              <div class="quick-info">
                <div class="quick-title">机器人列表</div>
                <div class="quick-desc">查看和管理机器人</div>
              </div>
            </div>
            
            <div class="quick-item" @click="goToDataQuery">
              <div class="quick-icon" style="background: #63b3ed;">
                <el-icon><Document /></el-icon>
              </div>
              <div class="quick-info">
                <div class="quick-title">数据查询</div>
                <div class="quick-desc">查看已处理的数据</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最近任务 -->
    <el-row class="recent-row">
      <el-col :span="24">
        <el-card class="recent-card">
          <template #header>
            <div class="card-header">
              <span>最近任务</span>
              <el-link type="primary" @click="goToTaskList">查看全部 <el-icon><ArrowRight /></el-icon></el-link>
            </div>
          </template>
          
          <el-table :data="recentTasks" style="width: 100%">
            <el-table-column prop="taskCode" label="任务编码" />
            <el-table-column prop="processName" label="流程名称" />
            <el-table-column prop="enterpriseName" label="股票名称" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTaskList } from '@/api/task'
import { getRobotList } from '@/api/robot'
import { getProcessList } from '@/api/process'

const router = useRouter()

const stats = reactive({
  totalTasks: 0,
  totalRobots: 0,
  onlineRobots: 0,
  totalProcesses: 0,
  enabledProcesses: 0,
  totalData: 0,
  todayTasks: 0,
  todayData: 0
})

const taskStatus = reactive({
  running: 0,
  pending: 0,
  completed: 0,
  failed: 0
})

const recentTasks = ref([])

// 加载统计数据
const loadStats = async () => {
  try {
    // 加载任务统计
    const taskRes = await getTaskList({ page: 1, pageSize: 100 })
    stats.totalTasks = taskRes.data.total || 0
    stats.todayTasks = 12 // 可以根据实际日期过滤
    
    // 加载任务状态统计
    const runningRes = await getTaskList({ page: 1, pageSize: 100, status: 'running' })
    taskStatus.running = runningRes.data.total || 0
    
    const pendingRes = await getTaskList({ page: 1, pageSize: 100, status: 'pending' })
    taskStatus.pending = pendingRes.data.total || 0
    
    const completedRes = await getTaskList({ page: 1, pageSize: 100, status: 'completed' })
    taskStatus.completed = completedRes.data.total || 0
    
    const failedRes = await getTaskList({ page: 1, pageSize: 100, status: 'failed' })
    taskStatus.failed = failedRes.data.total || 0
    
    // 加载机器人统计
    const robotRes = await getRobotList({ page: 1, pageSize: 100 })
    stats.totalRobots = robotRes.data.total || 0
    stats.onlineRobots = robotRes.data.records?.filter(r => r.status === 'online').length || 0
    
    // 加载流程统计
    const processRes = await getProcessList({ page: 1, pageSize: 100 })
    stats.totalProcesses = processRes.data.total || 0
    stats.enabledProcesses = processRes.data.records?.filter(p => p.status === 'enabled').length || 0
    
    // 数据总量（可以调用数据接口）
    stats.totalData = 12580
    stats.todayData = 156
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载最近任务
const loadRecentTasks = async () => {
  try {
    const res = await getTaskList({ page: 1, pageSize: 5 })
    recentTasks.value = res.data.records?.map(item => ({
      ...item,
      processName: item.processName || '未命名流程',
      enterpriseName: item.enterpriseName || '-',
      taskCode: item.taskCode,
      status: item.status,
      createTime: item.createTime
    })) || []
  } catch (error) {
    console.error('加载最近任务失败:', error)
  }
}

// 跳转函数
const goToTaskList = () => {
  router.push('/task')
}

const goToCreateTask = () => {
  router.push('/task?dialog=create')
}

const goToProcessList = () => {
  router.push('/process')
}

const goToRobotList = () => {
  router.push('/robot')
}

const goToDataQuery = () => {
  router.push('/data-query')
}

const getStatusType = (status) => {
  const map = {
    'running': 'warning',
    'completed': 'success',
    'pending': 'info',
    'failed': 'danger'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadStats()
  loadRecentTasks()
})
</script>

<style scoped lang="scss">
.dashboard {
  .stats-row {
    margin-bottom: 20px;
    
    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .stat-icon {
          width: 80px;
          height: 80px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
        }
        
        .stat-info {
          flex: 1;
          
          .stat-value {
            font-size: 32px;
            font-weight: 600;
            color: #333;
          }
          
          .stat-label {
            font-size: 14px;
            color: #999;
            margin-top: 4px;
          }
          
          .stat-trend {
            font-size: 12px;
            color: #666;
            margin-top: 8px;
          }
        }
      }
    }
  }
  
  .status-row {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .status-card {
      .status-chart {
        display: flex;
        justify-content: space-around;
        padding: 20px 0;
        
        .status-item {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .status-dot {
            width: 12px;
            height: 12px;
            border-radius: 50%;
          }
          
          .status-text {
            display: flex;
            flex-direction: column;
            
            .status-value {
              font-size: 24px;
              font-weight: 600;
              color: #333;
            }
            
            .status-label {
              font-size: 14px;
              color: #999;
            }
          }
        }
      }
    }
    
    .quick-card {
      .quick-list {
        .quick-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;
          cursor: pointer;
          
          &:last-child {
            border-bottom: none;
          }
          
          .quick-icon {
            width: 40px;
            height: 40px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
          }
          
          .quick-info {
            flex: 1;
            
            .quick-title {
              font-size: 14px;
              color: #333;
              font-weight: 500;
            }
            
            .quick-desc {
              font-size: 12px;
              color: #999;
              margin-top: 4px;
            }
          }
        }
      }
    }
  }
  
  .recent-row {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}
</style>
