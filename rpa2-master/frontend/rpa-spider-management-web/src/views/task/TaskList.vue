<template>
  <div class="task-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建任务
          </el-button>
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
          </el-select>
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
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="taxNo" label="纳税人识别号" />
        <el-table-column prop="enterpriseName" label="企业名称" />
        <el-table-column prop="status" label="任务状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handleExecute(row)">执行</el-button>
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
      width="800px"
    >
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
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTaskList, deleteTask, createTask, updateTask, getTaskById, executeTask, getProcessList, getAvailableRobots } from '@/api/task'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const currentRow = ref(null)
const processList = ref([])
const robotList = ref([])

const searchForm = reactive({
  keyword: '',
  status: ''
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
      status: searchForm.status
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
    viewDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
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
  ElMessageBox.confirm('确认执行该任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await executeTask(row.id)
      ElMessage.success('任务执行成功')
      loadData()
    } catch (error) {
      console.error('任务执行失败:', error)
      ElMessage.error(error.response?.data?.message || '任务执行失败')
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

onMounted(() => {
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
  }
}
</style>
