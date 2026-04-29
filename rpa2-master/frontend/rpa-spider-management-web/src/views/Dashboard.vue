<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goToTaskList">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon :size="40"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalTasks }}</div>
              <div class="stat-label">总任务数</div>
              <div class="stat-trend">
                <el-icon><Top /></el-icon>
                今日新增 {{ stats.todayTasks }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goToRobotList">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f687b3 0%, #f093fb 100%);">
              <el-icon :size="40"><VideoCamera /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalRobots }}</div>
              <div class="stat-label">机器人总数</div>
              <div class="stat-trend" style="color: #48bb78;">
                <el-icon><VideoCamera /></el-icon>
                在线 {{ stats.onlineRobots }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goToProcessList">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4fd1c5 0%, #81e6d9 100%);">
              <el-icon :size="40"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalProcesses }}</div>
              <div class="stat-label">流程总数</div>
              <div class="stat-trend">
                <el-icon><Check /></el-icon>
                启用 {{ stats.enabledProcesses }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goToDataList">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #68d391 0%, #c6f68d 100%);">
              <el-icon :size="40"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.totalData) }}</div>
              <div class="stat-label">数据总量</div>
              <div class="stat-trend">
                <el-icon><Download /></el-icon>
                今日采集 {{ stats.todayData }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 任务状态概览 -->
    <el-row :gutter="20" class="status-row">
      <el-col :span="18">
        <el-card class="status-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">任务状态概览</span>
              <el-link type="primary" @click="goToExecution">查看详情 <el-icon><ArrowRight /></el-icon></el-link>
            </div>
          </template>
          
          <v-chart
            class="trend-chart"
            :option="trendOption"
            :autoresize="true"
          />
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="quick-card" shadow="hover">
          <template #header>
            <span class="card-title">快捷入口</span>
          </template>
          
          <div class="quick-list">
            <div class="quick-item" @click="goToCreateTask">
              <div class="quick-icon" style="background: linear-gradient(135deg, #9f7aea 0%, #d69ee2 100%);">
                <el-icon><Plus /></el-icon>
              </div>
              <div class="quick-info">
                <div class="quick-title">创建任务</div>
                <div class="quick-desc">快速创建新的 RPA 任务</div>
              </div>
            </div>
            
            <div class="quick-item" @click="goToProcessList">
              <div class="quick-icon" style="background: linear-gradient(135deg, #4fd1c5 0%, #81e6d9 100%);">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="quick-info">
                <div class="quick-title">流程定义</div>
                <div class="quick-desc">定义和管理 RPA 流程</div>
              </div>
            </div>
            
            <div class="quick-item" @click="goToRobotList">
              <div class="quick-icon" style="background: linear-gradient(135deg, #f687b3 0%, #f093fb 100%);">
                <el-icon><VideoCamera /></el-icon>
              </div>
              <div class="quick-info">
                <div class="quick-title">机器人列表</div>
                <div class="quick-desc">查看和管理机器人</div>
              </div>
            </div>
            
            <div class="quick-item" @click="goToUserProfile">
              <div class="quick-icon" style="background: linear-gradient(135deg, #63b3ed 0%, #90cdf4 100%);">
                <el-icon><User /></el-icon>
              </div>
              <div class="quick-info">
                <div class="quick-title">个人信息</div>
                <div class="quick-desc">查看和编辑个人信息</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最近任务 -->
    <el-row class="recent-row">
      <el-col :span="24">
        <el-card class="recent-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">最近任务</span>
              <el-link type="primary" @click="goToTaskList">查看全部 <el-icon><ArrowRight /></el-icon></el-link>
            </div>
          </template>
          
          <el-table :data="recentTasks" style="width: 100%" :header-cell-style="{background:'#f5f7fa',color:'#606266'}">
            <el-table-column prop="taskCode" label="任务编码" />
            <el-table-column prop="taskName" label="任务名称" show-overflow-tooltip />
            <el-table-column prop="enterpriseName" label="企业名称" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small" effect="plain">{{ getStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Document, VideoCamera, Connection, DataLine, Top, Check, Download, ArrowRight, Plus, User } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getTaskList } from '@/api/task'
import { getRobotList } from '@/api/robot'
import { getProcessList } from '@/api/process'
import request from '@/utils/request'

use([BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

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

const trendOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderColor: '#e8e8e8',
    borderWidth: 1,
    textStyle: { color: '#2d3748', fontSize: 13 },
    axisPointer: { type: 'cross', crossStyle: { color: '#ddd' } }
  },
  legend: {
    data: ['任务数'],
    bottom: 0,
    textStyle: { color: '#718096', fontSize: 12 }
  },
  grid: {
    left: '3%',
    right: '4%',
    top: '10%',
    bottom: '18%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: ['待执行', '运行中', '已完成', '失败'],
    axisLine: { lineStyle: { color: '#e8e8e8' } },
    axisLabel: { color: '#718096', fontSize: 12 }
  },
  yAxis: {
    type: 'value',
    min: 0,
    splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
    axisLine: { show: false },
    axisLabel: { color: '#718096', fontSize: 12 }
  },
  series: [
    {
      name: '任务数',
      type: 'bar',
      barWidth: 36,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: (params) => ['#909399', '#e6a23c', '#67c23a', '#f56c6c'][params.dataIndex]
      },
      data: [taskStatus.pending, taskStatus.running, taskStatus.completed, taskStatus.failed]
    }
  ]
}))

const recentTasks = ref([])
let statsTimer = null
let recentTasksTimer = null

// 格式化大数字
const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 获取状态标签
const getStatusLabel = (status) => {
  const map = {
    1: '待执行',
    2: '运行中',
    3: '已完成',
    4: '失败',
    'PENDING': '待执行',
    'RUNNING': '运行中',
    'COMPLETED': '已完成',
    'FAILED': '失败',
    'QUEUED': '排队中'
  }
  return map[status] || '未知'
}

// 加载统计数据
const loadStats = async () => {
  try {
    console.log('开始加载统计数据...')
    
    // 加载任务统计
    const taskRes = await getTaskList({ page: 1, pageSize: 1 })
    stats.totalTasks = taskRes.data.total || 0
    
    // 计算今日任务（简化处理，实际应该按日期过滤）
    const today = new Date().toISOString().split('T')[0]
    const todayTaskRes = await getTaskList({ page: 1, pageSize: 100 })
    const todayTasks = todayTaskRes.data.records?.filter(task => {
      return task.createTime && task.createTime.startsWith(today)
    }) || []
    stats.todayTasks = todayTasks.length
    
    // 加载任务状态统计，保持和任务列表使用同一张任务表
    const statusStatsRes = await request.get('/api/task/status-stats')
    taskStatus.running = statusStatsRes.running || 0
    taskStatus.pending = statusStatsRes.pending || 0
    taskStatus.completed = statusStatsRes.completed || 0
    taskStatus.failed = statusStatsRes.failed || 0
    
    console.log('任务状态统计:', taskStatus)
    
    // 加载机器人统计
    const robotRes = await getRobotList({ page: 1, pageSize: 100 })
    stats.totalRobots = robotRes.data.total || 0
    stats.onlineRobots = robotRes.data.records?.filter(r => r.status === 'ONLINE' || r.status === 'BUSY').length || 0
    
    console.log('机器人统计:', stats.totalRobots, '在线:', stats.onlineRobots)
    
    // 加载流程统计
    const processRes = await getProcessList({ page: 1, pageSize: 100 })
    stats.totalProcesses = processRes.data.total || 0
    stats.enabledProcesses = processRes.data.records?.filter(p => p.status === 1).length || 0
    
    console.log('流程统计:', stats.totalProcesses, '启用:', stats.enabledProcesses)
    
    // 加载数据产品统计
    const dataStatsRes = await request.get('/api/data/stats')
    stats.totalData = dataStatsRes.total || 0
    stats.todayData = dataStatsRes.todayCollected || 0
    
    console.log('统计数据加载完成')
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载最近任务
const loadRecentTasks = async () => {
  try {
    const res = await getTaskList({ page: 1, pageSize: 50 })
    const records = res.data?.records || res.data?.content || []

    recentTasks.value = records
      .map(item => {
        const rawCreateTime = item.createTime || item.create_time
        return {
          taskCode: item.taskCode || item.task_code || '-',
          taskName: item.taskName || item.task_name || '未命名任务',
          enterpriseName: item.enterpriseName || item.enterprise_name || '-',
          status: item.status,
          rawCreateTime,
          createTime: formatTime(rawCreateTime)
        }
      })
      .sort((a, b) => parseTime(b.rawCreateTime) - parseTime(a.rawCreateTime))
      .slice(0, 5)

    console.log('最近任务:', recentTasks.value)
  } catch (error) {
    console.error('加载最近任务失败:', error)
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(String(time).replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) return '-'
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const parseTime = (time) => {
  if (!time) return 0
  const date = new Date(String(time).replace(' ', 'T'))
  return Number.isNaN(date.getTime()) ? 0 : date.getTime()
}

// 跳转函数
const goToTaskList = () => {
  router.push('/task')
}

const goToExecution = () => {
  router.push('/execution')
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

const goToDataList = () => {
  router.push('/data-query')
}

const goToUserProfile = () => {
  router.push('/user-info')
}

const getStatusType = (status) => {
  const map = {
    1: 'info',      // 待执行
    2: 'warning',   // 运行中
    3: 'success',   // 已完成
    4: 'danger',    // 失败
    'PENDING': 'info',
    'RUNNING': 'warning',
    'COMPLETED': 'success',
    'FAILED': 'danger',
    'QUEUED': 'info'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadStats()
  loadRecentTasks()

  statsTimer = setInterval(loadStats, 5000)
  recentTasksTimer = setInterval(loadRecentTasks, 10000)
})

onBeforeUnmount(() => {
  if (statsTimer) {
    clearInterval(statsTimer)
  }
  if (recentTasksTimer) {
    clearInterval(recentTasksTimer)
  }
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 20px;
  
  .stats-row {
    margin-bottom: 20px;
    
    .stat-card {
      border-radius: 12px;
      transition: all 0.3s ease;
      cursor: pointer;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }
      
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .stat-icon {
          width: 80px;
          height: 80px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          flex-shrink: 0;
        }
        
        .stat-info {
          flex: 1;
          
          .stat-value {
            font-size: 36px;
            font-weight: 700;
            color: #2d3748;
            line-height: 1;
          }
          
          .stat-label {
            font-size: 14px;
            color: #718096;
            margin-top: 8px;
            font-weight: 500;
          }
          
          .stat-trend {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 13px;
            color: #4a5568;
            margin-top: 12px;
            font-weight: 500;
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
      
      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #2d3748;
      }
    }
    
    .status-card {
      border-radius: 12px;

      .trend-chart {
        width: 100%;
        height: 280px;
      }
    }
    
    .quick-card {
      border-radius: 12px;
      
      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #2d3748;
      }
      
      .quick-list {
        .quick-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 16px 0;
          border-bottom: 1px solid #edf2f7;
          cursor: pointer;
          transition: all 0.3s ease;
          
          &:hover {
            transform: translateX(8px);
            
            .quick-icon {
              transform: scale(1.1);
            }
          }
          
          &:last-child {
            border-bottom: none;
          }
          
          .quick-icon {
            width: 44px;
            height: 44px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
            flex-shrink: 0;
            transition: all 0.3s ease;
          }
          
          .quick-info {
            flex: 1;
            
            .quick-title {
              font-size: 15px;
              color: #2d3748;
              font-weight: 600;
            }
            
            .quick-desc {
              font-size: 12px;
              color: #718096;
              margin-top: 4px;
            }
          }
        }
      }
    }
  }
  
  .recent-row {
    .recent-card {
      border-radius: 12px;
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .card-title {
          font-size: 16px;
          font-weight: 600;
          color: #2d3748;
        }
      }
    }
  }
}
</style>
