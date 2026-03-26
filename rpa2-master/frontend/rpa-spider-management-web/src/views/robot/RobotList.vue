<template>
  <div class="robot-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>机器人列表</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建机器人
          </el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="机器人编码/名称">
          <el-input v-model="searchForm.keyword" placeholder="机器人编码或名称" clearable />
        </el-form-item>
        
        <el-form-item label="机器人状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
            <el-option label="忙碌" value="busy" />
            <el-option label="维护中" value="maintenance" />
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
        <el-table-column prop="robotCode" label="机器人编码" />
        <el-table-column prop="robotName" label="机器人名称" />
        <el-table-column prop="robotType" label="机器人类型" />
        <el-table-column prop="ipAddress" label="IP 地址" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="concurrentTasks" label="并发任务数" width="100" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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
      :title="isEdit ? '编辑机器人' : '新建机器人'"
      width="600px"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="机器人编码" prop="robotCode">
          <el-input v-model="formData.robotCode" placeholder="请输入机器人编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="机器人名称" prop="robotName">
          <el-input v-model="formData.robotName" placeholder="请输入机器人名称" />
        </el-form-item>
        <el-form-item label="机器人类型" prop="robotType">
          <el-select v-model="formData.robotType" placeholder="请选择类型" style="width: 100%;">
            <el-option label="线程" value="thread" />
            <el-option label="进程" value="process" />
          </el-select>
        </el-form-item>
        <el-form-item label="IP 地址">
          <el-input v-model="formData.ipAddress" placeholder="请输入 IP 地址" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input v-model="formData.port" placeholder="请输入端口" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" style="width: 100%;">
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
            <el-option label="忙碌" value="busy" />
            <el-option label="维护中" value="maintenance" />
          </el-select>
        </el-form-item>
        <el-form-item label="并发任务数">
          <el-input-number v-model="formData.concurrentTasks" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
      title="机器人详情"
      width="600px"
    >
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="机器人编码">{{ currentRow.robotCode }}</el-descriptions-item>
        <el-descriptions-item label="机器人名称">{{ currentRow.robotName }}</el-descriptions-item>
        <el-descriptions-item label="机器人类型">{{ currentRow.robotType }}</el-descriptions-item>
        <el-descriptions-item label="IP 地址">{{ currentRow.ipAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="端口">{{ currentRow.port || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow.status)" size="small">{{ currentRow.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="并发任务数">{{ currentRow.concurrentTasks || 1 }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentRow.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentRow.createTime }}</el-descriptions-item>
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
import { getRobotList, deleteRobot, createRobot, updateRobot, getRobotDetail } from '@/api/robot'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const currentRow = ref(null)

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
  robotCode: '',
  robotName: '',
  robotType: '',
  ipAddress: '',
  port: '',
  description: '',
  status: 'offline',
  concurrentTasks: 1
})

const formRef = ref(null)

const formRules = {
  robotCode: [{ required: true, message: '请输入机器人编码', trigger: 'blur' }],
  robotName: [{ required: true, message: '请输入机器人名称', trigger: 'blur' }],
  robotType: [{ required: true, message: '请选择机器人类型', trigger: 'change' }]
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
    
    const res = await getRobotList(params)
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
    robotCode: '',
    robotName: '',
    robotType: '',
    ipAddress: '',
    port: '',
    description: '',
    status: 'offline',
    concurrentTasks: 1
  })
  dialogVisible.value = true
}

const handleView = async (row) => {
  try {
    const res = await getRobotDetail(row.id)
    currentRow.value = res.data
    viewDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const handleEdit = async (row) => {
  try {
    const res = await getRobotDetail(row.id)
    isEdit.value = true
    Object.assign(formData, res.data)
    dialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该机器人吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRobot(row.id)
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
        await updateRobot(formData.id, formData)
      } else {
        await createRobot(formData)
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
    'online': 'success',
    'offline': 'info',
    'busy': 'warning',
    'maintenance': 'danger'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.robot-list {
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
