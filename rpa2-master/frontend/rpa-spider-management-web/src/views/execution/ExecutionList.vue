<template>
  <div class="execution-management">
    <!-- 顶部统计卡片 -->
    <div class="stats-header">
      <div class="stats-grid">
        <div class="stat-card stat-running">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ statusStats.running || 0 }}</span>
            <span class="stat-label">运行中</span>
          </div>
          <div class="stat-indicator"></div>
        </div>
        
        <div class="stat-card stat-success">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ statusStats.completed || 0 }}</span>
            <span class="stat-label">执行成功</span>
          </div>
          <div class="stat-indicator"></div>
        </div>
        
        <div class="stat-card stat-failed">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ statusStats.failed || 0 }}</span>
            <span class="stat-label">执行失败</span>
          </div>
          <div class="stat-indicator"></div>
        </div>
        
        <div class="stat-card stat-total">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6"/>
              <line x1="8" y1="12" x2="21" y2="12"/>
              <line x1="8" y1="18" x2="21" y2="18"/>
              <line x1="3" y1="6" x2="3.01" y2="6"/>
              <line x1="3" y1="12" x2="3.01" y2="12"/>
              <line x1="3" y1="18" x2="3.01" y2="18"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ statusStats.total || 0 }}</span>
            <span class="stat-label">总记录数</span>
          </div>
          <div class="stat-indicator"></div>
        </div>
      </div>
    </div>

    <!-- 搜索和操作区域 -->
    <div class="toolbar-section">
      <div class="search-area">
        <div class="search-input-wrapper">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="M21 21l-4.35-4.35"/>
          </svg>
          <input 
            v-model="searchForm.processName" 
            type="text"
            placeholder="搜索流程名称..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button v-if="searchForm.processName" class="clear-btn" @click="searchForm.processName = ''">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        
        <div class="search-input-wrapper">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="20 12 20 22 4 22 4 12"/>
            <rect x="2" y="7" width="20" height="5"/>
            <line x1="12" y1="22" x2="12" y2="7"/>
            <path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"/>
            <path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/>
          </svg>
          <input 
            v-model="searchForm.taskCode" 
            type="text"
            placeholder="任务编码..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button v-if="searchForm.taskCode" class="clear-btn" @click="searchForm.taskCode = ''">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        
        <el-select 
          v-model="searchForm.status" 
          placeholder="全部状态"
          clearable
          class="status-filter"
        >
          <template #prefix>
            <svg class="filter-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
            </svg>
          </template>
          <el-option label="全部状态" value="" />
          <el-option label="运行中" value="RUNNING" />
          <el-option label="成功" value="COMPLETED" />
          <el-option label="失败" value="FAILED" />
          <el-option label="待执行" value="PENDING" />
          <el-option label="排队中" value="QUEUED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        
        <el-date-picker
          v-model="searchForm.dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          class="date-picker"
        />
        
        <button class="btn btn-secondary" @click="handleReset">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="1 4 1 10 7 10"/>
            <path d="M3.51 15a9 9 0 102.13-9.36L1 10"/>
          </svg>
          重置
        </button>
        
        <button class="btn btn-primary" @click="handleSearch">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="M21 21l-4.35-4.35"/>
          </svg>
          查询
        </button>
      </div>
      
      <div class="toolbar-right">
        <button class="btn btn-danger-outline" @click="handleBatchDelete" :disabled="selectedRows.length === 0">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
          </svg>
          批量删除 {{ selectedRows.length > 0 ? `(${selectedRows.length})` : '' }}
        </button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <div class="table-wrapper">
        <table class="data-table" v-loading="loading">
          <thead>
            <tr>
              <th class="col-checkbox">
                <label class="checkbox-wrapper">
                  <input 
                    type="checkbox" 
                    :checked="isAllSelected"
                    :indeterminate="isIndeterminate"
                    @change="handleSelectAll"
                  />
                  <span class="checkmark"></span>
                </label>
              </th>
              <th class="col-index">序号</th>
              <th class="col-code">任务编码</th>
              <th class="col-process">流程名称</th>
              <th class="col-robot">机器人</th>
              <th class="col-status">状态</th>
              <th class="col-start">开始时间</th>
              <th class="col-end">结束时间</th>
              <th class="col-duration">时长</th>
              <th class="col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(row, index) in tableData" 
              :key="row.id" 
              :class="{ 'selected': selectedRows.includes(row.id) }"
              @mouseenter="row.hovered = true" 
              @mouseleave="row.hovered = false"
            >
              <td class="cell-checkbox">
                <label class="checkbox-wrapper" @click.stop>
                  <input 
                    type="checkbox" 
                    :checked="selectedRows.includes(row.id)"
                    @change="handleSelectOne(row.id)"
                  />
                  <span class="checkmark"></span>
                </label>
              </td>
              <td class="cell-index">
                <span class="index-number">{{ (pagination.page - 1) * pagination.pageSize + index + 1 }}</span>
              </td>
              <td class="cell-code">
                <span class="code-tag">{{ row.taskCode || '-' }}</span>
              </td>
              <td class="cell-process">
                <div class="process-cell">
                  <div class="process-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                      <polyline points="14 2 14 8 20 8"/>
                      <line x1="16" y1="13" x2="8" y2="13"/>
                      <line x1="16" y1="17" x2="8" y2="17"/>
                      <polyline points="10 9 9 9 8 9"/>
                    </svg>
                  </div>
                  <span>{{ row.processName || '-' }}</span>
                </div>
              </td>
              <td class="cell-robot">
                <span>{{ row.robotName || '-' }}</span>
              </td>
              <td class="cell-status">
                <span class="status-indicator" :class="'status-' + getStatusClass(row.status)">
                  <span class="status-dot" :class="{ 'pulse': row.status === 'RUNNING' }"></span>
                  {{ getStatusLabel(row.status) }}
                </span>
              </td>
              <td class="cell-time">{{ formatTime(row.startTime) }}</td>
              <td class="cell-time">{{ formatTime(row.endTime) }}</td>
              <td class="cell-duration">
                <span class="duration-value" v-if="row.duration">{{ row.duration }}秒</span>
                <span class="duration-dash" v-else>-</span>
              </td>
              <td class="cell-action">
                <div class="action-buttons" :class="{ 'show': row.hovered }">
                  <button 
                    class="action-btn view"
                    @click="handleView(row)"
                    title="查看"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                  <button 
                    class="action-btn detail"
                    @click="handleViewSteps(row)"
                    title="详情"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="8" y1="6" x2="21" y2="6"/>
                      <line x1="8" y1="12" x2="21" y2="12"/>
                      <line x1="8" y1="18" x2="21" y2="18"/>
                      <line x1="3" y1="6" x2="3.01" y2="6"/>
                      <line x1="3" y1="12" x2="3.01" y2="12"/>
                      <line x1="3" y1="18" x2="3.01" y2="18"/>
                    </svg>
                  </button>
                  <button 
                    class="action-btn delete"
                    @click="handleDelete(row)"
                    title="删除"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                      <line x1="10" y1="11" x2="10" y2="17"/>
                      <line x1="14" y1="11" x2="14" y2="17"/>
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!loading && tableData.length === 0">
              <td colspan="10" class="empty-row">
                <div class="empty-state">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                    <line x1="9" y1="9" x2="15" y2="15"/>
                    <line x1="15" y1="9" x2="9" y2="15"/>
                  </svg>
                  <span>暂无执行记录</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="pagination.total > 0">
        <div class="pagination-info">
          <span class="info-text">已选择 <span class="selected-count">{{ selectedRows.length }}</span> 项，共 <span class="total-count">{{ pagination.total }}</span> 条记录</span>
        </div>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog 
      v-model="viewDialogVisible" 
      title="执行记录详情"
      width="1000px"
      class="enterprise-dialog"
    >
      <div v-if="currentRow" class="detail-content">
        <div class="detail-header">
          <div class="detail-status" :class="'status-' + getStatusClass(currentRow.status)">
            <span class="status-dot"></span>
            {{ getStatusLabel(currentRow.status) }}
          </div>
          <span class="detail-id">ID: {{ currentRow.id }}</span>
        </div>
        
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">任务编码</span>
            <span class="detail-value code">{{ currentRow.taskCode || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">流程编码</span>
            <span class="detail-value">{{ currentRow.processCode || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">流程名称</span>
            <span class="detail-value">{{ currentRow.processName || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">机器人编码</span>
            <span class="detail-value">{{ currentRow.robotCode || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">开始时间</span>
            <span class="detail-value">{{ formatTime(currentRow.startTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">结束时间</span>
            <span class="detail-value">{{ formatTime(currentRow.endTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">执行时长</span>
            <span class="detail-value">{{ currentRow.duration || 0 }} 秒</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">错误信息</span>
            <span class="detail-value error" v-if="currentRow.errorMessage">{{ currentRow.errorMessage }}</span>
            <span class="detail-value" v-else>-</span>
          </div>
        </div>
        
        <div class="steps-section" v-if="currentRow.stepResults && currentRow.stepResults.length > 0">
          <h4 class="steps-title">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6"/>
              <line x1="8" y1="12" x2="21" y2="12"/>
              <line x1="8" y1="18" x2="21" y2="18"/>
              <line x1="3" y1="6" x2="3.01" y2="6"/>
              <line x1="3" y1="12" x2="3.01" y2="12"/>
              <line x1="3" y1="18" x2="3.01" y2="18"/>
            </svg>
            执行步骤
          </h4>
          <div class="steps-timeline">
            <div 
              v-for="(step, index) in currentRow.stepResults" 
              :key="index"
              class="step-item"
              :class="'step-' + (step.status === 'success' ? 'success' : step.status === 'failed' ? 'failed' : 'running')"
            >
              <div class="step-indicator">
                <span class="step-number">{{ index + 1 }}</span>
              </div>
              <div class="step-content">
                <div class="step-header">
                  <span class="step-name">{{ step.stepName }}</span>
                  <span class="step-type">{{ step.scriptType || 'java' }}</span>
                  <span class="step-status" :class="'status-' + (step.status === 'success' ? 'success' : step.status === 'failed' ? 'failed' : 'running')">
                    {{ step.status === 'success' ? '成功' : step.status === 'failed' ? '失败' : '运行中' }}
                  </span>
                </div>
                <div class="step-time" v-if="step.endTime">
                  {{ formatTime(step.endTime) }}
                </div>
                <div class="step-output" v-if="step.output">
                  <span class="output-label">输出:</span>
                  <code>{{ step.output }}</code>
                </div>
                <div class="step-error" v-if="step.errorMessage">
                  <span class="error-label">错误:</span>
                  <span class="error-text">{{ step.errorMessage }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <button class="btn btn-secondary" @click="viewDialogVisible = false">关闭</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExecutionList, getExecutionDetail, deleteExecution } from '@/api/execution'

const router = useRouter()

const loading = ref(false)
const tableData = ref<any[]>([])
const viewDialogVisible = ref(false)
const currentRow = ref(null)
const selectedRows = ref<number[]>([])

const statusStats = ref({
  running: 0,
  completed: 0,
  failed: 0,
  total: 0
})

const searchForm = reactive({
  processName: '',
  taskCode: '',
  status: '',
  dateRange: []
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const isAllSelected = computed(() => {
  return tableData.value.length > 0 && selectedRows.value.length === tableData.value.length
})

const isIndeterminate = computed(() => {
  return selectedRows.value.length > 0 && selectedRows.value.length < tableData.value.length
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    
    if (searchForm.status) {
      params.status = searchForm.status
    }
    if (searchForm.processName) {
      params.processName = searchForm.processName
    }
    if (searchForm.taskCode) {
      params.taskCode = searchForm.taskCode
    }
    
    const res = await getExecutionList(params)
    
    const data = res.data
    
    tableData.value = (data.content || []).map((item) => ({ ...item, hovered: false }))
    pagination.total = data.total || 0
    
    statusStats.value = {
      running: data.runningCount || 0,
      completed: data.completedCount || 0,
      failed: data.failedCount || 0,
      total: data.total || 0
    }
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  console.log('点击查询按钮')
  pagination.page = 1
  selectedRows.value = []
  loadData()
}

const handleReset = () => {
  searchForm.processName = ''
  searchForm.taskCode = ''
  searchForm.status = ''
  searchForm.dateRange = []
  handleSearch()
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

const handleSelectAll = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.checked) {
    selectedRows.value = tableData.value.map((item: any) => item.id)
  } else {
    selectedRows.value = []
  }
}

const handleSelectOne = (id: number) => {
  const index = selectedRows.value.indexOf(id)
  if (index === -1) {
    selectedRows.value.push(id)
  } else {
    selectedRows.value.splice(index, 1)
  }
}

const handleView = async (row: any) => {
  currentRow.value = row
  viewDialogVisible.value = true
}

const handleViewSteps = (row: any) => {
  router.push({ path: '/execution/detail', query: { queueId: row.id } })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除任务"${row.taskCode || row.id}"的执行记录吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      await deleteExecution(row.id)
      ElMessage.success('删除成功')
      selectedRows.value = selectedRows.value.filter(id => id !== row.id)
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的记录')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条执行记录吗？此操作不可恢复。`,
    '批量删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      // 逐个删除
      for (const id of selectedRows.value) {
        await deleteExecution(id)
      }
      ElMessage.success(`成功删除 ${selectedRows.value.length} 条记录`)
      selectedRows.value = []
      loadData()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error(error.response?.data?.message || '批量删除失败')
    }
  }).catch(() => {})
}

const getStatusClass = (status: string) => {
  const map: Record<string, string> = {
    'RUNNING': 'running',
    'COMPLETED': 'success',
    'FAILED': 'failed',
    'PENDING': 'pending',
    'QUEUED': 'queued',
    'CANCELLED': 'cancelled',
    'RETRY': 'retry'
  }
  return map[status] || 'default'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    'RUNNING': '运行中',
    'COMPLETED': '成功',
    'FAILED': '失败',
    'PENDING': '待执行',
    'QUEUED': '排队中',
    'CANCELLED': '已取消',
    'RETRY': '重试中'
  }
  return map[status] || status || '-'
}

const formatTime = (time: string) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.execution-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

// 统计卡片
.stats-header {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    svg {
      width: 24px;
      height: 24px;
    }
  }
  
  .stat-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #1a1a2e;
      line-height: 1;
    }
    
    .stat-label {
      font-size: 14px;
      color: #6b7280;
    }
  }
  
  .stat-indicator {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 3px;
    opacity: 0.8;
  }
}

.stat-running {
  .stat-icon {
    background: linear-gradient(135deg, #f59e0b, #d97706);
    color: #fff;
  }
  .stat-indicator {
    background: linear-gradient(90deg, #f59e0b, #fbbf24);
  }
}

.stat-success {
  .stat-icon {
    background: linear-gradient(135deg, #10b981, #059669);
    color: #fff;
  }
  .stat-indicator {
    background: linear-gradient(90deg, #10b981, #34d399);
  }
}

.stat-failed {
  .stat-icon {
    background: linear-gradient(135deg, #ef4444, #dc2626);
    color: #fff;
  }
  .stat-indicator {
    background: linear-gradient(90deg, #ef4444, #f87171);
  }
}

.stat-total {
  .stat-icon {
    background: linear-gradient(135deg, #6366f1, #4f46e5);
    color: #fff;
  }
  .stat-indicator {
    background: linear-gradient(90deg, #6366f1, #818cf8);
  }
}

// 工具栏
.toolbar-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-area {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  
  .search-icon {
    position: absolute;
    left: 14px;
    width: 18px;
    height: 18px;
    color: #9ca3af;
  }
  
  .search-input {
    width: 200px;
    height: 40px;
    padding: 0 40px 0 42px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    font-size: 14px;
    transition: all 0.2s;
    background: #f9fafb;
    
    &:focus {
      outline: none;
      border-color: #3b82f6;
      background: #fff;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }
    
    &::placeholder {
      color: #9ca3af;
    }
  }
  
  .clear-btn {
    position: absolute;
    right: 10px;
    width: 20px;
    height: 20px;
    border: none;
    background: #e5e7eb;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    transition: all 0.2s;
    
    svg {
      width: 12px;
      height: 12px;
      color: #6b7280;
    }
    
    &:hover {
      background: #d1d5db;
    }
  }
}

.status-filter {
  width: 150px;
  
  .filter-icon {
    width: 16px;
    height: 16px;
    color: #6b7280;
  }
}

.date-picker {
  width: 340px;
}

// 按钮样式
.btn {
  height: 40px;
  padding: 0 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  border: none;
  
  svg {
    width: 16px;
    height: 16px;
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.btn-primary {
  background: #3b82f6;
  color: #fff;
  
  &:hover:not(:disabled) {
    background: #2563eb;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
  }
}

.btn-secondary {
  background: #fff;
  color: #374151;
  border: 1px solid #e5e7eb;
  
  &:hover {
    background: #f9fafb;
    border-color: #d1d5db;
  }
}

.btn-danger-outline {
  background: #fff;
  color: #dc2626;
  border: 1px solid #fecaca;
  
  &:hover:not(:disabled) {
    background: #fef2f2;
    border-color: #f87171;
    color: #ef4444;
  }
}

// 表格容器
.table-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  
  thead {
    background: #f9fafb;
    
    th {
      padding: 14px 12px;
      text-align: left;
      font-weight: 600;
      color: #374151;
      white-space: nowrap;
      border-bottom: 1px solid #e5e7eb;
    }
  }
  
  tbody {
    tr {
      transition: background 0.15s;
      border-bottom: 1px solid #f3f4f6;
      
      &:hover {
        background: #f8fafc;
      }
      
      &.selected {
        background: #eff6ff;
        
        &:hover {
          background: #dbeafe;
        }
      }
      
      &:last-child {
        border-bottom: none;
      }
    }
    
    td {
      padding: 14px 12px;
      color: #1f2937;
    }
  }
}

// 表格列宽
.col-checkbox { width: 50px; text-align: center; }
.col-index { width: 70px; }
.col-code { width: 140px; }
.col-process { min-width: 150px; }
.col-robot { width: 120px; }
.col-status { width: 90px; }
.col-start { width: 170px; }
.col-end { width: 170px; }
.col-duration { width: 90px; }
.col-action { width: 140px; }

// 复选框样式
.checkbox-wrapper {
  position: relative;
  display: inline-flex;
  cursor: pointer;
  
  input {
    position: absolute;
    opacity: 0;
    cursor: pointer;
    height: 0;
    width: 0;
  }
  
  .checkmark {
    height: 18px;
    width: 18px;
    background-color: #fff;
    border: 2px solid #d1d5db;
    border-radius: 4px;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &::after {
      content: '';
      display: none;
      width: 5px;
      height: 9px;
      border: solid white;
      border-width: 0 2px 2px 0;
      transform: rotate(45deg);
      margin-bottom: 2px;
    }
  }
  
  input:checked ~ .checkmark {
    background-color: #3b82f6;
    border-color: #3b82f6;
    
    &::after {
      display: block;
    }
  }
  
  input:indeterminate ~ .checkmark {
    background-color: #3b82f6;
    border-color: #3b82f6;
    
    &::after {
      display: block;
      width: 8px;
      height: 2px;
      background: white;
      border: none;
      transform: none;
      margin: 0;
    }
  }
  
  &:hover input ~ .checkmark {
    border-color: #3b82f6;
  }
}

// 单元格样式
.cell-index {
  .index-number {
    color: #9ca3af;
    font-size: 13px;
  }
}

.cell-code {
  .code-tag {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 13px;
    background: #f3f4f6;
    padding: 4px 8px;
    border-radius: 4px;
    color: #4b5563;
  }
}

.process-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .process-icon {
    width: 28px;
    height: 28px;
    background: #eff6ff;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    svg {
      width: 14px;
      height: 14px;
      color: #3b82f6;
    }
  }
}

.cell-duration {
  .duration-value {
    font-weight: 500;
    color: #374151;
  }
  
  .duration-dash {
    color: #d1d5db;
  }
}

// 状态指示器
.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 6px;
  
  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    
    &.pulse {
      animation: statusPulse 1.5s infinite;
    }
  }
  
  &.status-running {
    background: #fef3c7;
    color: #d97706;
    .status-dot {
      background: #f59e0b;
    }
  }
  
  &.status-success {
    background: #d1fae5;
    color: #059669;
    .status-dot {
      background: #10b981;
    }
  }
  
  &.status-failed {
    background: #fee2e2;
    color: #dc2626;
    .status-dot {
      background: #ef4444;
    }
  }
  
  &.status-pending,
  &.status-queued,
  &.status-cancelled {
    background: #f3f4f6;
    color: #6b7280;
    .status-dot {
      background: #9ca3af;
    }
  }
  
  &.status-retry {
    background: #fef3c7;
    color: #d97706;
    .status-dot {
      background: #f59e0b;
    }
  }
}

@keyframes statusPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

// 操作按钮
.action-buttons {
  display: flex;
  gap: 6px;
  opacity: 0.4;
  transition: opacity 0.2s;
  
  &.show {
    opacity: 1;
  }
}

.action-btn {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  
  svg {
    width: 15px;
    height: 15px;
  }
  
  &.view {
    background: #eff6ff;
    color: #2563eb;
    &:hover {
      background: #3b82f6;
      color: #fff;
    }
  }
  
  &.detail {
    background: #f3f4f6;
    color: #4b5563;
    &:hover {
      background: #6b7280;
      color: #fff;
    }
  }
  
  &.delete {
    background: #fef2f2;
    color: #dc2626;
    &:hover {
      background: #ef4444;
      color: #fff;
    }
  }
}

// 空状态
.empty-row {
  text-align: center;
  padding: 60px 20px !important;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #9ca3af;
  
  svg {
    width: 48px;
    height: 48px;
  }
  
  span {
    font-size: 14px;
  }
}

// 分页
.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-top: 1px solid #f3f4f6;
}

.pagination-info {
  .info-text {
    font-size: 14px;
    color: #6b7280;
  }
  
  .selected-count {
    color: #3b82f6;
    font-weight: 600;
  }
  
  .total-count {
    font-weight: 600;
    color: #1f2937;
  }
}

// 对话框样式
:deep(.enterprise-dialog) {
  border-radius: 12px;
  overflow: hidden;
  
  .el-dialog__header {
    padding: 20px 24px;
    border-bottom: 1px solid #e5e7eb;
    margin: 0;
  }
  
  .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
  }
  
  .el-dialog__body {
    padding: 24px;
    max-height: 70vh;
    overflow-y: auto;
  }
  
  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #e5e7eb;
    margin: 0;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

// 详情内容
.detail-content {
  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e5e7eb;
    
    .detail-status {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 6px 14px;
      border-radius: 8px;
      font-weight: 500;
      
      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
      }
      
      &.status-running {
        background: #fef3c7;
        color: #d97706;
        .status-dot { background: #f59e0b; }
      }
      
      &.status-success {
        background: #d1fae5;
        color: #059669;
        .status-dot { background: #10b981; }
      }
      
      &.status-failed {
        background: #fee2e2;
        color: #dc2626;
        .status-dot { background: #ef4444; }
      }
    }
    
    .detail-id {
      font-size: 13px;
      color: #9ca3af;
      font-family: 'Monaco', 'Menlo', monospace;
    }
  }
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.detail-item {
  .detail-label {
    display: block;
    font-size: 13px;
    color: #6b7280;
    margin-bottom: 6px;
  }
  
  .detail-value {
    font-size: 15px;
    color: #1f2937;
    
    &.code {
      font-family: 'Monaco', 'Menlo', monospace;
      background: #f3f4f6;
      padding: 4px 10px;
      border-radius: 4px;
    }
    
    &.error {
      color: #dc2626;
    }
  }
}

// 执行步骤
.steps-section {
  border-top: 1px solid #e5e7eb;
  padding-top: 24px;
  
  .steps-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #374151;
    margin: 0 0 20px 0;
    
    svg {
      width: 18px;
      height: 18px;
      color: #6b7280;
    }
  }
}

.steps-timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 10px;
  border-left: 3px solid transparent;
  
  &.step-success {
    border-left-color: #10b981;
  }
  
  &.step-failed {
    border-left-color: #ef4444;
  }
  
  &.step-running {
    border-left-color: #f59e0b;
  }
}

.step-indicator {
  flex-shrink: 0;
  
  .step-number {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: #fff;
    border: 2px solid #d1d5db;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 600;
    color: #6b7280;
  }
  
  .step-success & .step-number {
    background: #10b981;
    border-color: #10b981;
    color: #fff;
  }
  
  .step-failed & .step-number {
    background: #ef4444;
    border-color: #ef4444;
    color: #fff;
  }
  
  .step-running & .step-number {
    background: #f59e0b;
    border-color: #f59e0b;
    color: #fff;
  }
}

.step-content {
  flex: 1;
  
  .step-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 6px;
    
    .step-name {
      font-weight: 600;
      color: #1f2937;
    }
    
    .step-type {
      font-size: 12px;
      color: #6b7280;
      background: #e5e7eb;
      padding: 2px 8px;
      border-radius: 4px;
    }
    
    .step-status {
      font-size: 12px;
      font-weight: 500;
      padding: 2px 8px;
      border-radius: 4px;
      
      &.status-success {
        background: #d1fae5;
        color: #059669;
      }
      
      &.status-failed {
        background: #fee2e2;
        color: #dc2626;
      }
      
      &.status-running {
        background: #fef3c7;
        color: #d97706;
      }
    }
  }
  
  .step-time {
    font-size: 13px;
    color: #9ca3af;
    margin-bottom: 8px;
  }
  
  .step-output {
    background: #fff;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    padding: 10px 12px;
    margin-top: 8px;
    
    .output-label {
      font-size: 12px;
      color: #6b7280;
      display: block;
      margin-bottom: 4px;
    }
    
    code {
      font-family: 'Monaco', 'Menlo', monospace;
      font-size: 13px;
      color: #374151;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }
  
  .step-error {
    background: #fef2f2;
    border: 1px solid #fecaca;
    border-radius: 6px;
    padding: 10px 12px;
    margin-top: 8px;
    
    .error-label {
      font-size: 12px;
      color: #dc2626;
      display: block;
      margin-bottom: 4px;
    }
    
    .error-text {
      font-size: 13px;
      color: #991b1b;
    }
  }
}

// 响应式
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .toolbar-section {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .search-area {
    flex-wrap: wrap;
  }
  
  .search-input-wrapper .search-input {
    width: 100%;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
