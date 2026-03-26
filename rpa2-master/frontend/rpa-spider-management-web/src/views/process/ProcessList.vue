<template>
  <div class="process-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程列表</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建流程
          </el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流程编码/名称">
          <el-input v-model="searchForm.keyword" placeholder="流程编码或名称" clearable />
        </el-form-item>
        
        <el-form-item label="流程状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
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
        <el-table-column prop="processCode" label="流程编码" />
        <el-table-column prop="processName" label="流程名称" />
        <el-table-column prop="category" label="分类" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleDesign(row)">设计</el-button>
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
      :title="isEdit ? '编辑流程' : '新建流程'"
      width="600px"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="流程编码" prop="processCode">
          <el-input v-model="formData.processCode" placeholder="请输入流程编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="流程名称" prop="processName">
          <el-input v-model="formData.processName" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="formData.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="formData.version" placeholder="请输入版本号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" style="width: 100%;">
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
      title="流程详情"
      width="600px"
    >
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="流程编码">{{ currentRow.processCode }}</el-descriptions-item>
        <el-descriptions-item label="流程名称">{{ currentRow.processName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentRow.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ currentRow.version || '1.0.0' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow.status)" size="small">{{ currentRow.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="步骤数">{{ currentRow.stepCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentRow.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 流程设计对话框 -->
    <el-dialog
      v-model="designDialogVisible"
      title="流程设计 - 四步配置"
      width="900px"
      :close-on-click-modal="false"
    >
      <div class="process-design">
        <!-- 步骤列表 -->
        <div class="steps-container">
          <el-card v-for="(step, index) in steps" :key="step.id" class="step-card" shadow="hover">
            <template #header>
              <div class="step-header">
                <div class="step-title">
                  <el-tag type="primary" size="small" style="margin-right: 10px;">{{ index + 1 }}</el-tag>
                  <span class="step-name">{{ step.name }}</span>
                </div>
                <el-button type="primary" size="small" @click="handleEditStep(index)">配置</el-button>
              </div>
            </template>
            <div class="step-content">
              <div class="step-type">
                <el-tag size="small" type="info">{{ step.type === 'java' ? 'Java 爬虫代码' : 'Groovy 爬虫代码' }}</el-tag>
              </div>
              <div class="step-code-preview">
                <pre>{{ step.code ? step.code.substring(0, 100) + '...' : '暂无代码' }}</pre>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 步骤编辑对话框 -->
        <el-dialog
          v-model="stepEditDialogVisible"
          title="步骤配置"
          width="800px"
          append-to-body
        >
          <el-form label-width="120px">
            <el-form-item label="步骤名称">
              <el-input v-model="stepForm.stepName" placeholder="请输入步骤名称" />
            </el-form-item>
            <el-form-item label="步骤类型">
              <el-select v-model="stepForm.stepType" style="width: 100%;">
                <el-option label="Java 爬虫代码" value="java" />
                <el-option label="Groovy 爬虫代码" value="groovy" />
              </el-select>
            </el-form-item>
            <el-form-item label="Java/爬虫代码">
              <el-input
                v-model="stepForm.code"
                type="textarea"
                :rows="20"
                placeholder="请输入 Java/Groovy 代码片段"
                style="font-family: monospace;"
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="stepEditDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSaveStep">保存</el-button>
          </template>
        </el-dialog>
      </div>
      <template #footer>
        <el-button @click="designDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveProcessSteps">保存流程步骤</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProcessList, deleteProcess, createProcess, updateProcess, getProcessDetail } from '@/api/process'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const designDialogVisible = ref(false)
const stepEditDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const currentRow = ref(null)
const currentProcessId = ref(null)

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
  processCode: '',
  processName: '',
  category: '',
  version: '',
  description: '',
  remark: '',
  status: 'inactive',
  stepCount: 0
})

// 步骤配置数据
const stepForm = reactive({
  stepName: '',
  stepType: 'java',
  code: ''
})

const steps = ref([
  { id: 1, name: '采集', type: 'java', code: '', order: 1 },
  { id: 2, name: '解析', type: 'java', code: '', order: 2 },
  { id: 3, name: '加工', type: 'java', code: '', order: 3 },
  { id: 4, name: '落库', type: 'java', code: '', order: 4 }
])

const currentStepIndex = ref(null)

const formRef = ref(null)

const formRules = {
  processCode: [{ required: true, message: '请输入流程编码', trigger: 'blur' }],
  processName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }]
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
    
    const res = await getProcessList(params)
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
    processCode: '',
    processName: '',
    category: '',
    version: '1.0.0',
    description: '',
    remark: '',
    status: 'inactive',
    stepCount: 0
  })
  dialogVisible.value = true
}

const handleView = async (row) => {
  try {
    const res = await getProcessDetail(row.id)
    currentRow.value = res.data
    viewDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const handleEdit = async (row) => {
  try {
    const res = await getProcessDetail(row.id)
    isEdit.value = true
    Object.assign(formData, res.data)
    dialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const handleDeploy = (row) => {
  ElMessage.info('部署功能待实现')
}

const handleDesign = async (row) => {
  try {
    const res = await getProcessDetail(row.id)
    currentProcessId.value = row.id
    // 加载流程的步骤数据
    if (res.data.processData) {
      const processData = JSON.parse(res.data.processData)
      if (processData.steps) {
        steps.value = processData.steps
      }
    }
    designDialogVisible.value = true
  } catch (error) {
    console.error('获取流程详情失败:', error)
    ElMessage.error('获取流程详情失败')
  }
}

const handleEditStep = (index) => {
  currentStepIndex.value = index
  const step = steps.value[index]
  stepForm.stepName = step.name
  stepForm.stepType = step.type
  stepForm.code = step.code
  stepEditDialogVisible.value = true
}

const handleSaveStep = () => {
  if (currentStepIndex.value !== null) {
    steps.value[currentStepIndex.value] = {
      ...steps.value[currentStepIndex.value],
      name: stepForm.stepName,
      type: stepForm.stepType,
      code: stepForm.code
    }
    stepEditDialogVisible.value = false
    ElMessage.success('步骤保存成功')
  }
}

const handleSaveProcessSteps = async () => {
  try {
    const processData = {
      steps: steps.value,
      stepCount: 4
    }
    await updateProcess(currentProcessId.value, {
      processData: JSON.stringify(processData),
      stepCount: 4
    })
    ElMessage.success('流程步骤保存成功')
    designDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存流程步骤失败:', error)
    ElMessage.error('保存流程步骤失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该流程吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteProcess(row.id)
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
        await updateProcess(formData.id, formData)
      } else {
        await createProcess(formData)
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
    'active': 'success',
    'inactive': 'info'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.process-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-form {
    margin-bottom: 20px;
  }
}

.process-design {
  .steps-container {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .step-card {
    .step-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .step-title {
        display: flex;
        align-items: center;
        font-weight: bold;
        font-size: 16px;

        .step-name {
          font-size: 16px;
        }
      }
    }

    .step-content {
      .step-type {
        margin-bottom: 10px;
      }

      .step-code-preview {
        background-color: #f5f7fa;
        border-radius: 4px;
        padding: 10px;
        font-family: 'Courier New', monospace;
        font-size: 12px;
        color: #606266;
        max-height: 100px;
        overflow: hidden;

        pre {
          margin: 0;
          white-space: pre-wrap;
          word-wrap: break-word;
        }
      }
    }
  }
}
</style>
