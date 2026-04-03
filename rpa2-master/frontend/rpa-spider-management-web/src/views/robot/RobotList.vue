<template>
  <div class="robot-management">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="关键字">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="机器人编码/名称/部门"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="success" @click="handleCreate">
            <el-icon><Plus /></el-icon> 新建机器人
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 状态统计 -->
    <el-row :gutter="20" class="status-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <div class="status-content">
            <div class="status-icon" style="background: #67c23a;">
              <el-icon :size="30"><VideoCamera /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-value">{{ statusStats.online || 0 }}</div>
              <div class="status-label">在线机器人</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <div class="status-content">
            <div class="status-icon" style="background: #909399;">
              <el-icon :size="30"><VideoCamera /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-value">{{ statusStats.offline || 0 }}</div>
              <div class="status-label">离线机器人</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <div class="status-content">
            <div class="status-icon" style="background: #e6a23c;">
              <el-icon :size="30"><VideoCamera /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-value">{{ statusStats.busy || 0 }}</div>
              <div class="status-label">忙碌机器人</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <div class="status-content">
            <div class="status-icon" style="background: #f56c6c;">
              <el-icon :size="30"><VideoCamera /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-value">{{ statusStats.fault || 0 }}</div>
              <div class="status-label">故障机器人</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 机器人列表 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="robotList" 
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="robotCode" label="机器人编码" width="150" />
        <el-table-column prop="robotName" label="机器人名称" width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'ATTENDED' ? 'success' : 'primary'" size="small">
              {{ row.type === 'ATTENDED' ? '有人值守' : '无人值守' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small" effect="plain">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP 地址" width="150" />
        <el-table-column prop="department" label="所属部门" width="150" />
        <el-table-column prop="ownerName" label="负责人" width="120" />
        <el-table-column prop="lastHeartbeat" label="最后心跳" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastHeartbeat) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="handleView(row)"
            >
              详情
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form 
        ref="formRef"
        :model="formData" 
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="机器人编码" prop="robotCode">
          <el-input v-model="formData.robotCode" placeholder="请输入机器人编码" />
        </el-form-item>
        <el-form-item label="机器人名称" prop="robotName">
          <el-input v-model="formData.robotName" placeholder="请输入机器人名称" />
        </el-form-item>
        <el-form-item label="机器人类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio label="UNATTENDED">无人值守</el-radio>
            <el-radio label="ATTENDED">有人值守</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所属部门" prop="department">
          <el-input v-model="formData.department" placeholder="请输入所属部门" />
        </el-form-item>
        <el-form-item label="负责人 ID" prop="ownerId">
          <el-input-number v-model="formData.ownerId" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="负责人姓名" prop="ownerName">
          <el-input v-model="formData.ownerName" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="IP 地址" prop="ipAddress">
          <el-input v-model="formData.ipAddress" placeholder="请输入 IP 地址" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="formData.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入描述信息"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="formData.remark" 
            type="textarea" 
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, VideoCamera } from '@element-plus/icons-vue'
import { getRobotList, getRobotById, createRobot, updateRobot, deleteRobot, getRobotStatus } from '@/api/robot'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const robotList = ref([])
const statusStats = ref({})

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const formData = reactive({
  id: null,
  robotCode: '',
  robotName: '',
  type: 'UNATTENDED',
  department: '',
  ownerId: null,
  ownerName: '',
  ipAddress: '',
  description: '',
  remark: ''
})

const formRules = {
  robotCode: [{ required: true, message: '请输入机器人编码', trigger: 'blur' }],
  robotName: [{ required: true, message: '请输入机器人名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择机器人类型', trigger: 'change' }]
}

// 加载机器人列表
const loadRobotList = async () => {
  loading.value = true
  try {
    const res = await getRobotList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword
    })
    
    robotList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载机器人列表失败:', error)
    ElMessage.error('加载机器人列表失败')
  } finally {
    loading.value = false
  }
}

// 加载状态统计
const loadStatusStats = async () => {
  try {
    const res = await getRobotStatus()
    statusStats.value = res.data || {}
  } catch (error) {
    console.error('加载状态统计失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadRobotList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  handleSearch()
}

// 新建
const handleCreate = () => {
  dialogTitle.value = '新建机器人'
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogTitle.value = '编辑机器人'
  
  try {
    const res = await getRobotById(row.id)
    const data = res.data
    
    Object.keys(formData).forEach(key => {
      if (data[key] !== undefined) {
        formData[key] = data[key]
      }
    })
    
    dialogVisible.value = true
  } catch (error) {
    console.error('获取机器人详情失败:', error)
    ElMessage.error('获取机器人详情失败')
  }
}

// 查看详情
const handleView = async (row) => {
  router.push(`/robot/detail/${row.id}`)
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除机器人"${row.robotName}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteRobot(row.id)
      ElMessage.success('删除成功')
      loadRobotList()
      loadStatusStats()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (formData.id) {
        await updateRobot(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        await createRobot(formData)
        ElMessage.success('创建成功')
      }
      
      dialogVisible.value = false
      loadRobotList()
      loadStatusStats()
    } catch (error) {
      console.error('提交失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '操作失败'
      ElMessage.error(errorMsg)
    } finally {
      submitting.value = false
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
  Object.keys(formData).forEach(key => {
    formData[key] = null
  })
  formData.type = 'UNATTENDED'
}

// 分页变化
const handleSizeChange = () => {
  loadRobotList()
}

const handlePageChange = () => {
  loadRobotList()
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    'ONLINE': 'success',
    'OFFLINE': 'info',
    'BUSY': 'warning',
    'FAULT': 'danger'
  }
  return map[status] || 'info'
}

// 获取状态标签
const getStatusLabel = (status) => {
  const map = {
    'ONLINE': '在线',
    'OFFLINE': '离线',
    'BUSY': '忙碌',
    'FAULT': '故障'
  }
  return map[status] || '未知'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadRobotList()
  loadStatusStats()
})
</script>

<style scoped lang="scss">
.robot-management {
  padding: 20px;
  
  .search-card {
    margin-bottom: 20px;
  }
  
  .status-cards {
    margin-bottom: 20px;
    
    .status-card {
      .status-content {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .status-icon {
          width: 60px;
          height: 60px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
        }
        
        .status-info {
          flex: 1;
          
          .status-value {
            font-size: 28px;
            font-weight: 700;
            color: #303133;
          }
          
          .status-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }
  
  .table-card {
    .el-pagination {
      display: flex;
    }
  }
}
</style>
