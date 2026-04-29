<template>
  <div class="robot-management">
    <!-- 顶部统计卡片 -->
    <div class="stats-header">
      <div class="stats-grid">
        <div class="stat-card stat-online">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 6v6l4 2"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ statusStats.online || 0 }}</span>
            <span class="stat-label">在线</span>
          </div>
          <div class="stat-indicator"></div>
        </div>
        
        <div class="stat-card stat-offline">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M8 12h8"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ statusStats.offline || 0 }}</span>
            <span class="stat-label">离线</span>
          </div>
          <div class="stat-indicator"></div>
        </div>
        
        <div class="stat-card stat-busy">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <path d="M9 9h6v6H9z"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ statusStats.busy || 0 }}</span>
            <span class="stat-label">忙碌</span>
          </div>
          <div class="stat-indicator"></div>
        </div>
        
        <div class="stat-card stat-fault">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ statusStats.fault || 0 }}</span>
            <span class="stat-label">故障</span>
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
            v-model="searchForm.keyword" 
            type="text"
            placeholder="搜索机器人编码、名称、部门..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button v-if="searchForm.keyword" class="clear-btn" @click="searchForm.keyword = ''">
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
          <el-option label="在线" value="ONLINE" />
          <el-option label="离线" value="OFFLINE" />
          <el-option label="忙碌" value="BUSY" />
          <el-option label="故障" value="FAULT" />
        </el-select>
        
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
      
      <button class="btn btn-create" @click="handleCreate">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"/>
          <line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        新建机器人
      </button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <div class="table-wrapper">
        <table class="data-table" v-loading="loading">
          <thead>
            <tr>
              <th class="col-code">机器人编码</th>
              <th class="col-name">机器人名称</th>
              <th class="col-type">类型</th>
              <th class="col-status">状态</th>
              <th class="col-ip">IP地址</th>
              <th class="col-port">端口</th>
              <th class="col-dept">所属部门</th>
              <th class="col-owner">负责人</th>
              <th class="col-time">最后心跳</th>
              <th class="col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in robotList" :key="row.id" @mouseenter="row.hovered = true" @mouseleave="row.hovered = false">
              <td class="cell-code">
                <span class="code-tag">{{ row.robotCode }}</span>
              </td>
              <td class="cell-name">
                <div class="name-cell">
                  <div class="robot-avatar">
                    {{ row.robotName?.charAt(0) || 'R' }}
                  </div>
                  <span>{{ row.robotName }}</span>
                </div>
              </td>
              <td class="cell-type">
                <span class="type-badge" :class="row.type === 'ATTENDED' ? 'type-attended' : 'type-unattended'">
                  {{ row.type === 'ATTENDED' ? '有人值守' : '无人值守' }}
                </span>
              </td>
              <td class="cell-status">
                <span class="status-indicator" :class="'status-' + (row.status || 'offline').toLowerCase()">
                  <span class="status-dot"></span>
                  {{ getStatusLabel(row.status) }}
                </span>
              </td>
              <td class="cell-ip">{{ row.ipAddress || '-' }}</td>
              <td class="cell-port">{{ row.port || '-' }}</td>
              <td class="cell-dept">{{ row.departmentName || '-' }}</td>
              <td class="cell-owner">{{ row.ownerName || '-' }}</td>
              <td class="cell-time">{{ formatTime(row.lastHeartbeat) }}</td>
              <td class="cell-action">
                <div class="action-buttons" :class="{ 'show': row.hovered }">
                  <button 
                    v-if="row.status === 'OFFLINE' || row.status === 'FAULT'"
                    class="action-btn enable"
                    @click="handleEnable(row)"
                    title="启用"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polygon points="5 3 19 12 5 21 5 3"/>
                    </svg>
                  </button>
                  <button 
                    v-if="row.status === 'ONLINE' || row.status === 'BUSY'"
                    class="action-btn disable"
                    @click="handleDisable(row)"
                    title="禁用"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="6" y="4" width="4" height="16"/>
                      <rect x="14" y="4" width="4" height="16"/>
                    </svg>
                  </button>
                  <button 
                    class="action-btn detail"
                    @click="handleViewDetail(row)"
                    title="详情"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                  <button 
                    class="action-btn edit"
                    @click="handleEdit(row)"
                    title="编辑"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
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
            <tr v-if="!loading && robotList.length === 0">
              <td colspan="10" class="empty-row">
                <div class="empty-state">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
                    <line x1="8" y1="21" x2="16" y2="21"/>
                    <line x1="12" y1="17" x2="12" y2="21"/>
                  </svg>
                  <span>暂无机器人数据</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="pagination.total > 0">
        <div class="pagination-info">
          共 <span class="total-count">{{ pagination.total }}</span> 条记录
        </div>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle"
      width="600px"
      class="enterprise-dialog"
      @close="handleDialogClose"
    >
      <el-form 
        ref="formRef"
        :model="formData" 
        :rules="formRules"
        label-position="top"
      >
        <div class="form-row">
          <el-form-item label="机器人编码" prop="robotCode">
            <el-input v-model="formData.robotCode" placeholder="请输入机器人编码" />
          </el-form-item>
          <el-form-item label="机器人名称" prop="robotName">
            <el-input v-model="formData.robotName" placeholder="请输入机器人名称" />
          </el-form-item>
        </div>
        
        <el-form-item label="机器人类型" prop="type">
          <el-radio-group v-model="formData.type" class="type-radio-group">
            <el-radio label="UNATTENDED">
              <span class="radio-label">无人值守</span>
              <span class="radio-desc">后台自动运行，无需人工干预</span>
            </el-radio>
            <el-radio label="ATTENDED">
              <span class="radio-label">有人值守</span>
              <span class="radio-desc">需要人工触发或配合执行</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <div class="form-row">
          <el-form-item label="所属部门" prop="departmentName">
            <el-input v-model="formData.departmentName" placeholder="请输入所属部门" />
          </el-form-item>
          <el-form-item label="负责人" prop="ownerName">
            <el-input v-model="formData.ownerName" placeholder="请输入负责人姓名" />
          </el-form-item>
        </div>
        
        <div class="form-row">
          <el-form-item label="IP 地址" prop="ipAddress">
            <el-input v-model="formData.ipAddress" placeholder="请输入 IP 地址" />
          </el-form-item>
          <el-form-item label="端口" prop="port">
            <el-input-number 
              v-model="formData.port" 
              :min="1" 
              :max="65535" 
              :step="1"
              placeholder="端口号"
              style="width: 100%;"
            />
          </el-form-item>
        </div>
        
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
        <div class="dialog-footer">
          <button class="btn btn-secondary" @click="dialogVisible = false">取消</button>
          <button class="btn btn-primary" @click="handleSubmit" :disabled="submitting">
            {{ submitting ? '提交中...' : '确定' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="机器人详情"
      width="900px"
      class="enterprise-dialog detail-dialog"
      @close="handleDetailDialogClose"
    >
      <el-tabs v-if="currentDetailData" class="detail-tabs">
        <!-- 基本信息标签页 -->
        <el-tab-pane label="基本信息">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">机器人编码</span>
              <span class="detail-value code">{{ currentDetailData.robotCode }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">机器人名称</span>
              <span class="detail-value">{{ currentDetailData.robotName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">类型</span>
              <span class="type-badge" :class="currentDetailData.type === 'ATTENDED' ? 'type-attended' : 'type-unattended'">
                {{ currentDetailData.type === 'ATTENDED' ? '有人值守' : '无人值守' }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">状态</span>
              <span class="status-indicator" :class="'status-' + (currentDetailData.status || 'offline').toLowerCase()">
                <span class="status-dot"></span>
                {{ getStatusLabel(currentDetailData.status) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">IP地址</span>
              <span class="detail-value">{{ currentDetailData.ipAddress || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">端口</span>
              <span class="detail-value">{{ currentDetailData.port || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">所属部门</span>
              <span class="detail-value">{{ currentDetailData.departmentName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">负责人</span>
              <span class="detail-value">{{ currentDetailData.ownerName || '-' }}</span>
            </div>
            <div class="detail-item full-width">
              <span class="detail-label">描述</span>
              <span class="detail-value">{{ currentDetailData.description || '暂无描述' }}</span>
            </div>
            <div class="detail-item full-width">
              <span class="detail-label">备注</span>
              <span class="detail-value">{{ currentDetailData.remark || '暂无备注' }}</span>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 运行监控标签页 -->
        <el-tab-pane label="运行监控">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">在线状态</span>
              <span class="status-indicator" :class="currentDetailData.isOnline ? 'status-online' : 'status-offline'">
                <span class="status-dot"></span>
                {{ currentDetailData.isOnline ? '在线' : '离线' }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">健康状态</span>
              <span class="status-indicator" :class="'status-' + getHealthClass(currentDetailData.healthStatus)">
                <span class="status-dot"></span>
                {{ getHealthLabel(currentDetailData.healthStatus) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">当前任务</span>
              <span class="detail-value">{{ currentDetailData.currentTaskName || '无' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">运行时长</span>
              <span class="detail-value">{{ formatDuration(currentDetailData.runningDuration) }}</span>
            </div>
            <div class="detail-item full-width">
              <span class="detail-label">CPU 使用率</span>
              <div class="progress-wrapper">
                <el-progress 
                  :percentage="currentDetailData.cpuUsage || 0" 
                  :stroke-width="8"
                  :status="getProgressStatus(currentDetailData.cpuUsage)"
                />
              </div>
            </div>
            <div class="detail-item full-width">
              <span class="detail-label">内存使用率</span>
              <div class="progress-wrapper">
                <el-progress 
                  :percentage="currentDetailData.memoryUsage || 0" 
                  :stroke-width="8"
                  :status="getProgressStatus(currentDetailData.memoryUsage)"
                />
              </div>
            </div>
            <div class="detail-item full-width">
              <span class="detail-label">磁盘使用率</span>
              <div class="progress-wrapper">
                <el-progress 
                  :percentage="currentDetailData.diskUsage || 0" 
                  :stroke-width="8"
                  :status="getProgressStatus(currentDetailData.diskUsage)"
                />
              </div>
            </div>
            <div class="detail-item">
              <span class="detail-label">线程数</span>
              <span class="detail-value">{{ currentDetailData.threadCount || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">上传速度</span>
              <span class="detail-value">{{ currentDetailData.networkUpload || 0 }} KB/s</span>
            </div>
            <div class="detail-item full-width">
              <span class="detail-label">最后心跳</span>
              <span class="detail-value">{{ formatTime(currentDetailData.lastHeartbeat) }}</span>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 时间信息标签页 -->
        <el-tab-pane label="时间信息">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">创建时间</span>
              <span class="detail-value">{{ formatTime(currentDetailData.createTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">更新时间</span>
              <span class="detail-value">{{ formatTime(currentDetailData.updateTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">最后心跳</span>
              <span class="detail-value">{{ formatTime(currentDetailData.lastHeartbeat) }}</span>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <template #footer>
        <div class="dialog-footer">
          <button class="btn btn-secondary" @click="detailDialogVisible = false">关闭</button>
          <button class="btn btn-primary" @click="handleEditFromDetail">编辑</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRobotList, getRobotById, createRobot, updateRobot, deleteRobot, getRobotStatus, startRobot, stopRobot, getRobotMonitorById } from '@/api/robot'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const submitting = ref(false)
const robotList = ref<any[]>([])
const statusStats = ref({})

const searchForm = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const detailDialogVisible = ref(false)
const currentDetailData = ref(null)
const monitorTimer = ref(null)

const formData = ref({
  id: null,
  robotCode: '',
  robotName: '',
  type: 'UNATTENDED',
  departmentName: '',
  ownerId: null,
  ownerName: '',
  ipAddress: '',
  port: 8080,
  description: '',
  remark: ''
})

const validateIpAddress = (rule, value, callback) => {
  if (!value) return callback()
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

const loadRobotList = async () => {
  loading.value = true
  try {
    const res = await getRobotList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    
    robotList.value = (res.data.records || []).map(item => ({ ...item, hovered: false }))
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载机器人列表失败:', error)
    ElMessage.error('加载机器人列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatusStats = async () => {
  try {
    const res = await getRobotStatus()
    console.log('机器人状态统计响应:', res)
    statusStats.value = res.data || {}
    console.log('状态统计赋值后:', statusStats.value)
  } catch (error) {
    console.error('加载状态统计失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadRobotList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  handleSearch()
}

const handleCreate = () => {
  dialogTitle.value = '新建机器人'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑机器人'
  
  try {
    const res = await getRobotById(row.id)
    const data = res.data
    
    if (!data || !data.id) {
      ElMessage.error('获取机器人数据失败')
      return
    }
    
    formData.value.id = data.id
    formData.value.robotCode = data.robotCode || ''
    formData.value.robotName = data.robotName || ''
    formData.value.type = data.type || 'UNATTENDED'
    formData.value.departmentName = data.departmentName || ''
    formData.value.ownerId = data.ownerId || null
    formData.value.ownerName = data.ownerName || ''
    formData.value.ipAddress = data.ipAddress || ''
    formData.value.port = data.port || 8080
    formData.value.description = data.description || ''
    formData.value.remark = data.remark || ''
    
    dialogVisible.value = true
  } catch (error) {
    console.error('获取机器人详情失败:', error)
    ElMessage.error('获取机器人详情失败')
  }
}

const handleViewDetail = async (row) => {
  try {
    const [detailRes, monitorRes] = await Promise.all([
      getRobotById(row.id),
      getRobotMonitorById(row.id)
    ])
    
    currentDetailData.value = {
      ...detailRes.data,
      ...monitorRes.data
    }
    detailDialogVisible.value = true
    
    if (currentDetailData.value.isOnline) {
      startMonitorRefresh(row.id)
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

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
  }, 10000)
}

const handleEditFromDetail = () => {
  detailDialogVisible.value = false
  if (currentDetailData.value) {
    handleEdit(currentDetailData.value)
  }
}

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
      await deleteRobot(row.id)
      ElMessage.success('删除成功')
      await loadRobotList()
      await loadStatusStats()
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

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const submitData = formData.value
      if (submitData.id) {
        await updateRobot(submitData.id, submitData)
        ElMessage.success('更新成功')
      } else {
        await createRobot(submitData)
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

const handleDialogClose = () => {
  formRef.value?.resetFields()
  formData.value = {
    id: null,
    robotCode: '',
    robotName: '',
    type: 'UNATTENDED',
    departmentName: '',
    ownerId: null,
    ownerName: '',
    ipAddress: '',
    port: 8080,
    description: '',
    remark: ''
  }
}

const handleDetailDialogClose = () => {
  if (monitorTimer.value) {
    clearInterval(monitorTimer.value)
    monitorTimer.value = null
  }
  currentDetailData.value = null
}

const handleSizeChange = () => {
  loadRobotList()
}

const handlePageChange = () => {
  loadRobotList()
}

const getStatusLabel = (status) => {
  const map: Record<string, string> = {
    'ONLINE': '在线',
    'OFFLINE': '离线',
    'BUSY': '忙碌',
    'FAULT': '故障'
  }
  return map[status] || '未知'
}

const getHealthLabel = (health) => {
  const map: Record<string, string> = {
    'HEALTHY': '健康',
    'WARNING': '警告',
    'CRITICAL': '严重'
  }
  return map[health] || '未知'
}

const getHealthClass = (health) => {
  const map: Record<string, string> = {
    'HEALTHY': 'online',
    'WARNING': 'busy',
    'CRITICAL': 'fault'
  }
  return map[health] || 'offline'
}

const getProgressStatus = (value) => {
  if (value >= 80) return 'exception'
  if (value >= 60) return 'warning'
  return 'success'
}

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

const formatDuration = (minutes) => {
  if (!minutes && minutes !== 0) return '-'
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}小时${mins}分钟`
}

onMounted(async () => {
  await loadRobotList()
  await loadStatusStats()
  
  if (route.query.edit) {
    const robotId = route.query.edit
    const row = { id: robotId }
    await handleEdit(row)
  }
})
</script>

<style scoped lang="scss">
.robot-management {
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

.stat-online {
  .stat-icon {
    background: linear-gradient(135deg, #10b981, #059669);
    color: #fff;
  }
  .stat-indicator {
    background: linear-gradient(90deg, #10b981, #34d399);
  }
}

.stat-offline {
  .stat-icon {
    background: linear-gradient(135deg, #6b7280, #4b5563);
    color: #fff;
  }
  .stat-indicator {
    background: linear-gradient(90deg, #6b7280, #9ca3af);
  }
}

.stat-busy {
  .stat-icon {
    background: linear-gradient(135deg, #f59e0b, #d97706);
    color: #fff;
  }
  .stat-indicator {
    background: linear-gradient(90deg, #f59e0b, #fbbf24);
  }
}

.stat-fault {
  .stat-icon {
    background: linear-gradient(135deg, #ef4444, #dc2626);
    color: #fff;
  }
  .stat-indicator {
    background: linear-gradient(90deg, #ef4444, #f87171);
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
    width: 320px;
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
    opacity: 0.6;
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

.btn-create {
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff;
  
  &:hover {
    background: linear-gradient(135deg, #059669, #047857);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
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
      padding: 16px 12px;
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
      
      &:last-child {
        border-bottom: none;
      }
    }
    
    td {
      padding: 16px 12px;
      color: #1f2937;
    }
  }
}

// 表格列宽
.col-code { width: 140px; }
.col-name { width: 180px; }
.col-type { width: 100px; }
.col-status { width: 90px; }
.col-ip { width: 130px; }
.col-port { width: 70px; }
.col-dept { width: 120px; }
.col-owner { width: 100px; }
.col-time { width: 150px; }
.col-action { width: 180px; }

// 单元格样式
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

.name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .robot-avatar {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 14px;
  }
}

.type-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.type-attended {
  background: #ecfdf5;
  color: #059669;
}

.type-unattended {
  background: #eff6ff;
  color: #2563eb;
}

// 状态指示器
.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  
  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
}

.status-online {
  color: #059669;
  .status-dot {
    background: #10b981;
    box-shadow: 0 0 6px rgba(16, 185, 129, 0.5);
    animation: pulse 2s infinite;
  }
}

.status-offline {
  color: #6b7280;
  .status-dot {
    background: #9ca3af;
  }
}

.status-busy {
  color: #d97706;
  .status-dot {
    background: #f59e0b;
    animation: pulse 1.5s infinite;
  }
}

.status-fault {
  color: #dc2626;
  .status-dot {
    background: #ef4444;
    animation: pulse 1s infinite;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

// 操作按钮
.action-buttons {
  display: flex;
  gap: 8px;
  opacity: 0.5;
  transition: opacity 0.2s;
  
  &.show {
    opacity: 1;
  }
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  
  svg {
    width: 16px;
    height: 16px;
  }
  
  &.enable {
    background: #ecfdf5;
    color: #059669;
    &:hover {
      background: #10b981;
      color: #fff;
    }
  }
  
  &.disable {
    background: #fff7ed;
    color: #ea580c;
    &:hover {
      background: #f97316;
      color: #fff;
    }
  }
  
  &.detail {
    background: #eff6ff;
    color: #2563eb;
    &:hover {
      background: #3b82f6;
      color: #fff;
    }
  }
  
  &.edit {
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
  font-size: 14px;
  color: #6b7280;
  
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
  gap: 12px;
}

// 表单样式
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

.type-radio-group {
  display: flex;
  flex-direction: row;
  gap: 16px;
  
  :deep(.el-radio) {
    margin-right: 0;
    flex: 1;
    padding: 16px 16px 12px 16px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    transition: all 0.2s;
    height: auto;
    align-items: flex-start;
    
    &:hover {
      border-color: #3b82f6;
    }
    
    &.is-checked {
      border-color: #3b82f6;
      background: #eff6ff;
    }
    
    :deep(.el-radio__input) {
      margin-top: 2px;
    }
    
    :deep(.el-radio__label) {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      padding-left: 8px;
    }
  }
  
  .radio-label {
    font-weight: 500;
    color: #1f2937;
    display: block;
    font-size: 14px;
    line-height: 1.5;
  }
  
  .radio-desc {
    font-size: 12px;
    color: #6b7280;
    display: block;
    margin-top: 6px;
    line-height: 1.5;
  }
}

// 详情页样式
.detail-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }
  
  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
    padding: 0 20px;
    height: 44px;
    line-height: 44px;
    
    &.is-active {
      color: #3b82f6;
    }
  }
  
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }
  
  :deep(.el-tabs__active-bar) {
    height: 2px;
  }
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.detail-item {
  &.full-width {
    grid-column: 1 / -1;
  }
  
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
  }
}

.progress-wrapper {
  padding-top: 4px;
  
  :deep(.el-progress-bar__outer) {
    border-radius: 4px;
  }
  
  :deep(.el-progress-bar__inner) {
    border-radius: 4px;
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
}
</style>
