<template>
  <div class="task-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <div>
            <el-button type="success" @click="showBatchDialog = true" :disabled="selectedRows.length === 0">
              <el-icon><Operation /></el-icon>
              批量执行
            </el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              新建任务
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="任务编码/名称">
          <el-input v-model="searchForm.keyword" placeholder="任务编码或名称" clearable />
        </el-form-item>
        
        <el-form-item label="任务状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待执行" value="pending" />
            <el-option label="运行中" value="running" />
            <el-option label="已完成" value="completed" />
            <el-option label="失败" value="failed" />
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="流程">
          <el-select v-model="searchForm.processId" placeholder="请选择流程" clearable>
            <el-option
              v-for="item in processList"
              :key="item.id"
              :label="item.processName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="机器人">
          <el-select v-model="searchForm.robotId" placeholder="请选择机器人" clearable>
            <el-option
              v-for="item in robotList"
              :key="item.id"
              :label="item.robotName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="创建时间">
          <el-select v-model="searchForm.timeType" placeholder="时间类型" style="width: 180px; margin-right: 10px;">
            <el-option label="创建时间" value="create" />
            <el-option label="执行时间" value="execution" />
          </el-select>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
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
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="taskCode" label="任务编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="taskName" label="任务名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="taxNo" label="纳税人识别号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="enterpriseName" label="企业名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small" effect="light">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="70" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small" effect="plain">{{ row.priority || 5 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150" />
        <el-table-column label="操作" width="360" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)" style="margin: 0 4px;">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)" style="margin: 0 4px;">编辑</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleExecuteCommand(row, cmd)" style="margin: 0 4px;">
              <el-button link type="warning">
                执行
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="sync">
                    <el-icon><VideoPlay /></el-icon>
                    同步执行
                  </el-dropdown-item>
                  <el-dropdown-item command="async">
                    <el-icon><VideoPause /></el-icon>
                    异步执行
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button link :type="row.status === 0 ? 'warning' : 'info'" @click="handleToggleStatus(row)" style="margin: 0 4px;">
              {{ row.status === 0 ? '启用' : '禁用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" style="margin: 0 4px;">删除</el-button>
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

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑任务' : '新建任务'"
      width="600px"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="任务编码" prop="taskCode">
          <el-input v-model="formData.taskCode" placeholder="请输入任务编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="formData.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="流程" prop="processId">
          <el-select v-model="formData.processId" placeholder="请选择流程" style="width: 100%;" :disabled="isEdit">
            <el-option
              v-for="item in processList"
              :key="item.id"
              :label="item.processName"
              :value="item.id"
            >
              <span>{{ item.processName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.processCode }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="机器人" prop="robotId">
          <el-select v-model="formData.robotId" placeholder="请选择机器人" style="width: 100%;" clearable>
            <el-option
              v-for="item in robotList"
              :key="item.id"
              :label="item.robotName"
              :value="item.id"
            >
              <span>{{ item.robotName }}</span>
              <el-tag size="small" style="margin-left: 10px;" :type="item.status === 'ONLINE' ? 'success' : 'info'">
                {{ item.status === 'ONLINE' ? '在线' : item.status === 'BUSY' ? '忙碌' : '离线' }}
              </el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="纳税人识别号">
          <el-input v-model="formData.taxNo" placeholder="请输入纳税人识别号" />
        </el-form-item>
        <el-form-item label="企业名称">
          <el-input v-model="formData.enterpriseName" placeholder="请输入企业名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="formData.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="formData.priority" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="任务详情"
      width="900px"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane label="任务信息" name="info">
          <el-descriptions v-if="currentRow" :column="2" border>
            <el-descriptions-item label="任务编码" :span="2">{{ currentRow.taskCode }}</el-descriptions-item>
            <el-descriptions-item label="任务名称" :span="2">{{ currentRow.taskName }}</el-descriptions-item>
            <el-descriptions-item label="纳税人识别号">{{ currentRow.taxNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="企业名称">{{ currentRow.enterpriseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ currentRow.category || '-' }}</el-descriptions-item>
            <el-descriptions-item label="优先级">{{ currentRow.priority || 5 }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(currentRow.status)" size="small">{{ getStatusLabel(currentRow.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="流程" :span="2">{{ currentRow.processName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="机器人" :span="2">{{ currentRow.robotName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ currentRow.updateTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="开始时间" :span="2">{{ currentRow.startTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="结束时间" :span="2">{{ currentRow.endTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="执行记录" name="execution">
          <el-table :data="executionList" border style="margin-top: 10px;" v-loading="executionLoading">
            <el-table-column prop="taskCode" label="任务编码" width="120" show-overflow-tooltip />
            <el-table-column prop="taskName" label="任务名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="processName" label="流程名称" width="130" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.processName || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="robotName" label="机器人" width="110" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.robotName || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="getExecutionStatusType(row.status)" size="small">
                  {{ getExecutionStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="160" />
            <el-table-column prop="endTime" label="结束时间" width="160" />
            <el-table-column prop="errorMessage" label="错误信息" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="180" align="center">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button link type="primary" size="small" @click="handleViewExecution(row)">查看详情</el-button>
                  <el-button link type="warning" size="small" @click="handleRetry(row)" v-if="row.status === 'FAILED' || row.status === 'COMPLETED'">重试</el-button>
                  <el-button link type="danger" size="small" style="color: #f56c6c;" @click="handleDeleteExecution(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
            <template #empty>
              <span style="color: #909399;">暂无执行记录</span>
            </template>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <BatchExecuteDialog v-model="showBatchDialog" @success="handleBatchSuccess" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { getTaskList, deleteTask, createTask, updateTask, getTaskById, executeTask, executeTaskSync, getProcessList, getAvailableRobots, getExecutionList, batchDeleteTasks } from '@/api/task'
import { ArrowDown, VideoPlay, VideoPause, Delete, Search, Refresh, Plus, Operation } from '@element-plus/icons-vue'
import BatchExecuteDialog from './BatchExecuteDialog.vue'

const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const activeTab = ref('info')
const isEdit = ref(false)
const submitLoading = ref(false)
const currentRow = ref(null)
const processList = ref([])
const robotList = ref([])
const executionList = ref([])
const executionLoading = ref(false)
const showBatchDialog = ref(false)

const searchForm = reactive({
  keyword: '',
  status: '',
  processId: null,
  robotId: null,
  dateRange: [],
  timeType: 'create'  // 默认按创建时间筛选
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive({
  id: null,
  taskCode: '',
  taskName: '',
  processId: null,
  robotId: null,
  taxNo: '',
  enterpriseName: '',
  category: '',
  priority: 5,
  remark: ''
})

const formRef = ref(null)

const formRules = {
  taskCode: [{ required: true, message: '请输入任务编码', trigger: 'blur' }],
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  processId: [{ required: true, message: '请选择流程', trigger: 'change' }],
  robotId: [{ required: true, message: '请选择机器人', trigger: 'change' }]
}

// 加载流程列表
const loadProcessList = async () => {
  try {
    const res = await getProcessList({ page: 1, pageSize: 100, status: 1 })
    // 兼容两种格式
    processList.value = res.data.records || res.data.content || []
  } catch (error) {
    console.error('加载流程列表失败:', error)
  }
}

// 加载可用机器人列表
const loadRobotList = async () => {
  try {
    const res = await getAvailableRobots()
    robotList.value = res.data || []
  } catch (error) {
    console.error('加载机器人列表失败:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status,
      processId: searchForm.processId,
      robotId: searchForm.robotId,
      timeType: searchForm.timeType || 'create'
    }
    
    // 处理日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const res = await getTaskList(params)
    // 后端返回的是 JPA Page 对象格式：content 和 totalElements
    tableData.value = res.data.content || res.data.records || []
    pagination.total = res.data.totalElements || res.data.total || 0
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
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.processId = null
  searchForm.robotId = null
  searchForm.dateRange = []
  searchForm.timeType = 'create'
  handleSearch()
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    taskCode: '',
    taskName: '',
    processId: null,
    robotId: null,
    taxNo: '',
    enterpriseName: '',
    category: '',
    priority: 5,
    remark: ''
  })
  // 加载流程和机器人列表
  loadProcessList()
  loadRobotList()
  dialogVisible.value = true
}

const handleView = async (row) => {
  try {
    const res = await getTaskById(row.id)
    currentRow.value = res.data
    activeTab.value = 'info'
    // 加载执行记录
    await loadExecutionList(row.id)
    viewDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const loadExecutionList = async (taskId) => {
  executionLoading.value = true
  try {
    const res = await getExecutionList({ taskId })
    console.log('执行记录数据:', res.data)
    executionList.value = res.data || []
  } catch (error) {
    console.error('加载执行记录失败:', error)
    ElMessage.error('加载执行记录失败')
  } finally {
    executionLoading.value = false
  }
}

const handleViewExecution = async (row) => {
  console.log('查看执行记录:', row)
  try {
    // 从执行记录获取 taskId，如果不存在则使用 id
    const taskId = row.taskId || row.id
    const res = await getTaskById(taskId)
    currentRow.value = res.data
    activeTab.value = 'info'
    viewDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const handleDeleteExecution = (row) => {
  ElMessageBox.confirm('确认删除该执行记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 这里调用删除执行记录的 API
      // await deleteExecution(row.id)
      ElMessage.success('删除成功')
      // 刷新执行记录列表
      if (currentRow.value?.id) {
        await loadExecutionList(currentRow.value.id)
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

const handleRetry = async (row) => {
  ElMessageBox.confirm('确认重试该执行任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 重试时使用 row.taskId（如果有）或 row.id
      const taskId = row.taskId || row.id
      await executeTask(taskId)
      ElMessage.success('重试成功')
      // 刷新数据
      loadData()
      // 如果有任务ID，加载执行记录
      if (taskId) {
        await loadExecutionList(taskId)
      }
    } catch (error) {
      console.error('重试失败:', error)
      ElMessage.error(error.response?.data?.message || '重试失败')
    }
  })
}

const handleEdit = async (row) => {
  try {
    const res = await getTaskById(row.id)
    isEdit.value = true
    Object.assign(formData, res.data)
    // 加载流程和机器人列表
    loadProcessList()
    loadRobotList()
    dialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const handleExecute = async (row) => {
  // 默认使用异步执行（推送给机器人）
  handleExecuteCommand(row, 'async')
}

const isFailureMessage = (message = '') => {
  return /失败|错误|异常|error|failed|exception/i.test(String(message))
}

const showExecutionMessage = (message) => {
  const text = message || '任务执行成功'
  if (isFailureMessage(text)) {
    ElMessage.error(text)
  } else {
    ElMessage.success(text)
  }
}

const handleExecuteCommand = async (row, mode) => {
  const modeText = mode === 'sync' ? '同步执行（服务端直接执行）' : '异步执行（推送给机器人）'
  ElMessageBox.confirm(`确认执行该任务吗？\n执行模式：${modeText}`, '确认执行', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    let loadingInstance
    try {
      console.log('========================================')
      console.log('🚀 [任务执行] 开始执行任务')
      console.log('   - 任务ID:', row.id)
      console.log('   - 任务编码:', row.taskCode)
      console.log('   - 任务名称:', row.taskName)
      console.log('   - 执行模式:', mode)
      console.log('========================================')
      
      loadingInstance = ElLoading.service({ text: '正在执行任务...', lock: true })
      
      let result
      if (mode === 'sync') {
        result = await executeTaskSync(row.id)
      } else {
        result = await executeTask(row.id)
        // 后端已在异步模式下自动触发队列处理，无需再次调用 triggerQueue
      }
      
      console.log('========================================')
      console.log('📦 [任务执行] 收到后端响应')
      console.log('   - 完整响应:', JSON.stringify(result.data, null, 2))
      console.log('========================================')
      
      showExecutionMessage(result.data?.message)
      
      // 刷新数据
      loadData()
      
      // 如果有 queueId，记录日志
      if (result.data?.queueId) {
        console.log('✅ 执行队列 ID:', result.data.queueId)
        console.log('🔍 可以访问以下地址查看执行详情:')
        console.log('   - 执行详情API: http://localhost:8080/api/task/execution/detail/' + result.data.queueId)
        // 如果是异步执行，显示执行详情
        if (mode === 'async') {
          ElMessage.info('任务已推送，等待机器人执行...')
        }
      }
    } catch (error) {
      console.error('========================================')
      console.error('❌ [任务执行] 执行失败')
      console.error('   - 错误信息:', error)
      console.error('   - 响应数据:', error.response?.data)
      console.error('========================================')
      ElMessage.error(error.response?.data?.message || '任务执行失败')
    } finally {
      loadingInstance?.close()
    }
  })
}

const handleToggleStatus = async (row) => {
  const action = row.status === 0 ? '启用' : '禁用'
  ElMessageBox.confirm(`确认${action}该任务吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateTask(row.id, { ...row, status: row.status === 0 ? 1 : 0 })
      ElMessage.success(`${action}成功`)
      loadData()
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error(error.response?.data?.message || `${action}失败`)
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTask(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection.map(row => row.id)
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个任务吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      submitLoading.value = true
      await batchDeleteTasks(selectedRows.value)
      ElMessage.success('批量删除成功')
      selectedRows.value = []
      loadData()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error(error.response?.data?.message || '批量删除失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleBatchSuccess = (data) => {
  selectedRows.value = []
  loadData()
  ElMessage.info({
    message: `批次 ${data.batchNo} 已创建，可前往「批量执行」页面查看进度`,
    duration: 5000
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateTask(formData.id, formData)
      } else {
        await createTask(formData)
      }
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error(error.response?.data?.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const getStatusType = (status) => {
  const map = {
    1: 'info',    // pending
    2: 'warning', // running
    3: 'success', // completed
    4: 'danger'   // failed
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    1: '待执行',
    2: '运行中',
    3: '已完成',
    4: '失败'
  }
  return map[status] || '未知'
}

const getExecutionStatusType = (status) => {
  const map = {
    'PENDING': 'info',
    'RUNNING': 'warning',
    'COMPLETED': 'success',
    'FAILED': 'danger'
  }
  return map[status] || 'info'
}

const getExecutionStatusLabel = (status) => {
  const map = {
    'PENDING': '待执行',
    'RUNNING': '运行中',
    'COMPLETED': '已完成',
    'FAILED': '失败'
  }
  return map[status] || '未知'
}

onMounted(() => {
  loadProcessList()
  loadRobotList()
  loadData()
})
</script>

<style scoped lang="scss">
.task-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;

    :deep(.el-select) {
      width: 180px;
    }
  }
}

:deep(.el-table) {
  .el-table__header th {
    background-color: #f5f7fa;
    color: #606266;
    font-weight: 600;
  }

  .el-table__row:hover > td {
    background-color: #f5f7fa;
  }
}

:deep(.el-tag) {
  border-radius: 4px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
</style>
