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
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="全部状态"
            clearable
            style="width: 150px"
          >
            <el-option label="在线" value="ONLINE" />
            <el-option label="离线" value="OFFLINE" />
            <el-option label="忙碌" value="BUSY" />
            <el-option label="故障" value="FAULT" />
          </el-select>
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
        min-width="1200"
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
         <el-table-column prop="port" label="端口" width="100" />
         <el-table-column prop="department" label="所属部门" width="150" />
        <el-table-column prop="ownerName" label="负责人" width="120" />
        <el-table-column prop="lastHeartbeat" label="最后心跳" width="180" />
        <el-table-column label="操作" fixed="right" width="280" align="right">
          <template #default="{ row }">
            <div style="display: flex; gap: 8px; justify-content: flex-end; padding-right: 8px;">
              <el-button 
                type="success" 
                link
                size="small" 
                @click="handleEnable(row)"
                v-if="row.status === 'OFFLINE' || row.status === 'FAULT'"
              >
                启用
              </el-button>
              <el-button 
                type="warning" 
                link
                size="small" 
                @click="handleDisable(row)"
                v-if="row.status === 'ONLINE' || row.status === 'BUSY'"
              >
                禁用
              </el-button>
              <el-button 
                type="primary" 
                link
                size="small" 
                @click="handleViewDetail(row)"
              >
                详情
              </el-button>
              <el-button 
                type="primary" 
                link
                size="small" 
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button 
                type="danger" 
                link
                size="small" 
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
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
        <el-form-item label="端口" prop="port">
          <el-input-number 
            v-model="formData.port" 
            :min="1" 
            :max="65535" 
            :step="1"
            placeholder="请输入端口号"
            style="width: 100%;"
          />
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

    <!-- 详情对话框（合并配置和监控） -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="机器人详情"
      width="900px"
      @close="handleDetailDialogClose"
    >
      <el-tabs v-if="currentDetailData">
        <!-- 基本信息标签页 -->
        <el-tab-pane label="基本信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="机器人编码">{{ currentDetailData.robotCode }}</el-descriptions-item>
            <el-descriptions-item label="机器人名称">{{ currentDetailData.robotName }}</el-descriptions-item>
            <el-descriptions-item label="机器人类型">
              <el-tag :type="currentDetailData.type === 'ATTENDED' ? 'success' : 'primary'" size="small">
                {{ currentDetailData.type === 'ATTENDED' ? '有人值守' : '无人值守' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(currentDetailData.status)" size="small">
                {{ getStatusLabel(currentDetailData.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="IP 地址">{{ currentDetailData.ipAddress || '-' }}</el-descriptions-item>
            <el-descriptions-item label="端口">{{ currentDetailData.port || '-' }}</el-descriptions-item>
            <el-descriptions-item label="所属部门">{{ currentDetailData.department || '-' }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ currentDetailData.ownerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ currentDetailData.description || '暂无描述' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ currentDetailData.remark || '暂无备注' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        
        <!-- 运行监控标签页 -->
        <el-tab-pane label="运行监控">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="在线状态">
              <el-tag :type="currentDetailData.isOnline ? 'success' : 'danger'" size="small">
                {{ currentDetailData.isOnline ? '在线' : '离线' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="健康状态">
              <el-tag :type="getHealthType(currentDetailData.healthStatus)" size="small">
                {{ getHealthLabel(currentDetailData.healthStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前任务">{{ currentDetailData.currentTaskName || '无' }}</el-descriptions-item>
            <el-descriptions-item label="运行时长">{{ formatDuration(currentDetailData.runningDuration) }}</el-descriptions-item>
            <el-descriptions-item label="CPU 使用率">
              <el-progress 
                :percentage="currentDetailData.cpuUsage || 0" 
                :status="getHealthStatus(currentDetailData.cpuUsage)"
              />
            </el-descriptions-item>
            <el-descriptions-item label="内存使用率">
              <el-progress 
                :percentage="currentDetailData.memoryUsage || 0" 
                :status="getHealthStatus(currentDetailData.memoryUsage)"
              />
            </el-descriptions-item>
            <el-descriptions-item label="磁盘使用率">
              <el-progress 
                :percentage="currentDetailData.diskUsage || 0" 
                :status="getHealthStatus(currentDetailData.diskUsage)"
              />
            </el-descriptions-item>
            <el-descriptions-item label="线程数">{{ currentDetailData.threadCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="上传速度">{{ currentDetailData.networkUpload || 0 }} KB/s</el-descriptions-item>
            <el-descriptions-item label="下载速度">{{ currentDetailData.networkDownload || 0 }} KB/s</el-descriptions-item>
            <el-descriptions-item label="告警信息" :span="2">
              <el-alert 
                v-if="currentDetailData.alarmMessage" 
                type="warning" 
                :title="currentDetailData.alarmMessage" 
                show-icon 
                :closable="false"
              />
              <span v-else>无</span>
            </el-descriptions-item>
            <el-descriptions-item label="最后心跳" :span="2">{{ formatTime(currentDetailData.lastHeartbeat) }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        
        <!-- 时间信息标签页 -->
        <el-tab-pane label="时间信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="创建时间">{{ formatTime(currentDetailData.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatTime(currentDetailData.updateTime) }}</el-descriptions-item>
            <el-descriptions-item label="最后心跳">{{ formatTime(currentDetailData.lastHeartbeat) }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEditFromDetail">编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, VideoCamera } from '@element-plus/icons-vue'
import { getRobotList, getRobotById, createRobot, updateRobot, deleteRobot, getRobotStatus, updateRobotStatus, startRobot, stopRobot, getRobotMonitorById } from '@/api/robot'

const router = useRouter()
const route = useRoute()

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
const monitorDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentMonitorData = ref(null)
const currentDetailData = ref(null)
const monitorTimer = ref(null)

const formData = reactive({
  id: null,
  robotCode: '',
  robotName: '',
  type: 'UNATTENDED',
  department: '',
  ownerId: null,
  ownerName: '',
  ipAddress: '',
  port: 8080,
  description: '',
  remark: ''
})

// 验证 IP 地址格式
const validateIpAddress = (rule, value, callback) => {
  if (!value) {
    return callback() // 允许为空
  }
  const ipPattern = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/
  if (!ipPattern.test(value)) {
    callback(new Error('请输入有效的 IP 地址格式'))
  } else {
    callback()
  }
}

const formRules = {
  robotCode: [
    { required: true, message: '请输入机器人编码', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  robotName: [
    { required: true, message: '请输入机器人名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择机器人类型', trigger: 'change' }
  ],
  ipAddress: [
    { validator: validateIpAddress, trigger: 'blur' }
  ]
}

// 加载机器人列表
const loadRobotList = async () => {
  loading.value = true
  try {
    const res = await getRobotList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
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
  searchForm.status = ''
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
    
    // 如果后端没有返回 port 字段，使用默认值
    if (!formData.port) {
      formData.port = 8080
    }
    
    dialogVisible.value = true
  } catch (error) {
    console.error('获取机器人详情失败:', error)
    ElMessage.error('获取机器人详情失败')
  }
}

// 查看详情（合并配置信息和监控数据）
const handleViewDetail = async (row) => {
  try {
    // 并行获取基础信息和监控数据
    const [detailRes, monitorRes] = await Promise.all([
      getRobotById(row.id),
      getRobotMonitorById(row.id)
    ])
    
    currentDetailData.value = {
      ...detailRes.data,
      ...monitorRes.data
    }
    detailDialogVisible.value = true
    
    // 如果是监控数据，启动定时器刷新
    if (currentDetailData.value.isOnline) {
      startMonitorRefresh(row.id)
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 启动监控刷新
const startMonitorRefresh = (robotId) => {
  if (monitorTimer.value) {
    clearInterval(monitorTimer.value)
  }
  monitorTimer.value = setInterval(async () => {
    try {
      const res = await getRobotMonitorById(robotId)
      if (currentDetailData.value) {
        currentDetailData.value = {
          ...currentDetailData.value,
          ...res.data
        }
      }
    } catch (error) {
      console.error('刷新监控数据失败:', error)
    }
  }, 10000) // 每 10 秒刷新一次
}

// 从详情对话框编辑
const handleEditFromDetail = () => {
  detailDialogVisible.value = false
  if (currentDetailData.value) {
    handleEdit(currentDetailData.value)
  }
}

// 启用机器人
const handleEnable = async (row) => {
  ElMessageBox.confirm(
    `确定要启用机器人"${row.robotName}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await startRobot(row.id)
      ElMessage.success('启用成功')
      loadRobotList()
      loadStatusStats()
    } catch (error) {
      console.error('启用失败:', error)
      ElMessage.error(error.response?.data?.message || '启用失败')
    }
  }).catch(() => {})
}

// 禁用机器人
const handleDisable = async (row) => {
  ElMessageBox.confirm(
    `确定要禁用机器人"${row.robotName}"吗？禁用后将无法执行任务`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await stopRobot(row.id)
      ElMessage.success('禁用成功')
      loadRobotList()
      loadStatusStats()
    } catch (error) {
      console.error('禁用失败:', error)
      ElMessage.error(error.response?.data?.message || '禁用失败')
    }
  }).catch(() => {})
}

// 查看监控
const handleMonitor = async (row) => {
  try {
    const res = await getRobotMonitorById(row.id)
    currentMonitorData.value = res.data
    monitorDialogVisible.value = true
  } catch (error) {
    console.error('获取监控数据失败:', error)
    ElMessage.error('获取监控数据失败')
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  const newStatus = row.status === 'ONLINE' || row.status === 'BUSY' ? 'OFFLINE' : 'ONLINE'
  const action = newStatus === 'OFFLINE' ? '禁用' : '启用'
  
  ElMessageBox.confirm(
    `确定要${action}机器人"${row.robotName}"吗？${newStatus === 'OFFLINE' ? '禁用后将无法执行任务' : '启用后可以正常执行任务'}`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await updateRobotStatus(row.id, newStatus)
      ElMessage.success(`${action}成功`)
      loadRobotList()
      loadStatusStats()
    } catch (error) {
      console.error('状态切换失败:', error)
      ElMessage.error(error.response?.data?.message || `${action}失败`)
    }
  }).catch(() => {})
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除机器人"${row.robotName}"吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      distinguishCancelAndClose: true
    }
  ).then(async () => {
    try {
      console.log('开始删除机器人，ID:', row.id)
      await deleteRobot(row.id)
      console.log('删除成功，刷新列表')
      ElMessage.success('删除成功')
      // 刷新列表和状态统计
      await loadRobotList()
      await loadStatusStats()
      console.log('列表刷新完成')
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch((action) => {
    if (action === 'cancel') {
      ElMessage.info('已取消删除')
    }
  })
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
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  if (monitorTimer.value) {
    clearInterval(monitorTimer.value)
    monitorTimer.value = null
  }
  currentDetailData.value = null
}

// 从详情对话框编辑重置表单
const resetForm = () => {
  formData.type = 'UNATTENDED'
  formData.port = 8080
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

// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes && minutes !== 0) return '-'
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}小时${mins}分钟`
}

// 获取健康类型
const getHealthType = (health) => {
  const map = {
    'HEALTHY': 'success',
    'WARNING': 'warning',
    'CRITICAL': 'danger'
  }
  return map[health] || 'info'
}

// 获取健康标签
const getHealthLabel = (health) => {
  const map = {
    'HEALTHY': '健康',
    'WARNING': '警告',
    'CRITICAL': '严重'
  }
  return map[health] || '未知'
}

// 获取进度条状态
const getHealthStatus = (value) => {
  if (value >= 80) return 'exception'
  if (value >= 60) return 'warning'
  return 'success'
}

onMounted(async () => {
  await loadRobotList()
  await loadStatusStats()
  
  // 检查是否有编辑参数
  if (route.query.edit) {
    const robotId = route.query.edit
    const row = { id: robotId }
    await handleEdit(row)
  }
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
    
    // 操作按钮样式优化
    .el-button {
      margin: 0 4px;
      font-weight: 500;
      
      &:hover {
        transform: translateY(-1px);
      }
    }
  }
}
</style>
