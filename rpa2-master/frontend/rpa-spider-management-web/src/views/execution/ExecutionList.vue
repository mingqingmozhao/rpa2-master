<template>
  <div class="execution-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>执行记录</span>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流程名称">
          <el-input v-model="searchForm.processName" placeholder="流程名称" clearable />
        </el-form-item>
        
        <el-form-item label="任务编码">
          <el-input v-model="searchForm.taskCode" placeholder="任务编码" clearable />
        </el-form-item>
        
        <el-form-item label="执行状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="运行中" value="running" />
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="taskCode" label="任务编码" />
        <el-table-column prop="processName" label="流程名称" />
        <el-table-column prop="robotName" label="机器人名称" />
        <el-table-column prop="status" label="执行状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="endTime" label="结束时间" />
        <el-table-column prop="duration" label="执行时长 (秒)" width="100" />
        <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 执行记录详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="执行记录详情"
      width="1000px"
    >
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="执行 ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="任务编码">
          <el-tag type="primary" size="small">{{ currentRow.taskCode || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="流程编码">{{ currentRow.processCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="机器人编码">{{ currentRow.robotCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行状态">
          <el-tag :type="getStatusType(currentRow.status)" size="small">{{ currentRow.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ currentRow.duration || 0 }}秒</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentRow.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentRow.endTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="center">执行步骤</el-divider>

      <el-timeline v-if="currentRow && currentRow.stepResults && currentRow.stepResults.length > 0">
        <el-timeline-item
          v-for="(step, index) in currentRow.stepResults"
          :key="index"
          :timestamp="step.endTime || ''"
          placement="top"
          :color="step.status === 'success' ? '#67C23A' : (step.status === 'failed' ? '#F56C6C' : '#E6A23C')"
        >
          <div style="padding-left: 10px;">
            <h4 style="margin: 0 0 8px 0; font-size: 14px; font-weight: 600;">{{ step.stepName }} ({{ step.scriptType || 'java' }})</h4>
            <div v-if="step.output" style="background-color: #f5f7fa; padding: 8px; border-radius: 4px; font-size: 12px; font-family: monospace;">
              <div style="color: #909399; margin-bottom: 4px;">输出:</div>
              <div style="color: #606266;">{{ step.output }}</div>
            </div>
            <div v-if="step.errorMessage" style="background-color: #fef0f0; padding: 8px; border-radius: 4px; font-size: 12px; color: #F56C6C; margin-top: 8px;">
              <div style="margin-bottom: 4px;">错误:</div>
              <div>{{ step.errorMessage }}</div>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExecutionList, getExecutionDetail, deleteExecution } from '@/api/execution'

const loading = ref(false)
const tableData = ref([])
const viewDialogVisible = ref(false)
const currentRow = ref(null)

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

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: searchForm.status
    }
    
    const res = await getExecutionList(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
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

const handleView = async (row) => {
  try {
    const res = await getExecutionDetail(row.id)
    currentRow.value = res.data
    viewDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该执行记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteExecution(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

const getStatusType = (status) => {
  const map = {
    'running': 'warning',
    'success': 'success',
    'failed': 'danger'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.execution-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
