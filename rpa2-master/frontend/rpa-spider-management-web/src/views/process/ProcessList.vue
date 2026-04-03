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
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
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
        <el-table-column prop="category" label="分类" show-overflow-tooltip />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
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
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
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
      width="800px"
    >
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="流程编码" :span="2">{{ currentRow.processCode }}</el-descriptions-item>
        <el-descriptions-item label="流程名称" :span="2">{{ currentRow.processName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentRow.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ currentRow.version || '1.0.0' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow.status)" size="small">
            {{ currentRow.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="步骤数">{{ currentRow.steps || 0 }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentRow.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ currentRow.createUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentRow.updateTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 流程设计对话框 -->
    <el-dialog
      v-model="designDialogVisible"
      title="流程设计 - 四步配置"
      width="1000px"
      :close-on-click-modal="false"
    >
      <div class="process-design">
        <el-alert
          title="流程配置说明"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <p>请依次配置以下四个环节：</p>
          <ol style="margin: 10px 0; padding-left: 20px;">
            <li><strong>采集环节</strong>：负责数据采集，支持 HTTP 请求、数据库查询等方式</li>
            <li><strong>解析环节</strong>：负责解析采集的原始数据，提取关键字段</li>
            <li><strong>加工环节</strong>：负责数据转换、计算、校验等处理</li>
            <li><strong>落库环节</strong>：负责将处理后的数据保存到数据库</li>
          </ol>
        </el-alert>

        <!-- 步骤列表 -->
        <div class="steps-container">
          <el-card v-for="(step, index) in steps" :key="step.id" class="step-card" shadow="hover">
            <template #header>
              <div class="step-header">
                <div class="step-title">
                  <el-tag :type="getStepTagType(index)" size="small" style="margin-right: 10px;">{{ index + 1 }}</el-tag>
                  <span class="step-name">{{ step.name }}</span>
                  <el-tag v-if="step.configured" type="success" size="small" style="margin-left: 10px;">已配置</el-tag>
                  <el-tag v-else type="warning" size="small" style="margin-left: 10px;">未配置</el-tag>
                </div>
                <div class="step-actions">
                  <el-button type="primary" size="small" @click="handleEditStep(index)">配置</el-button>
                  <el-button type="info" size="small" @click="handlePreviewStep(index)">预览</el-button>
                </div>
              </div>
            </template>
            <div class="step-content">
              <div class="step-info">
                <div class="step-row">
                  <span class="step-label">环节类型：</span>
                  <el-tag size="small">{{ getStepTypeName(step.type) }}</el-tag>
                </div>
                <div class="step-row">
                  <span class="step-label">脚本长度：</span>
                  <span>{{ step.code ? step.code.length : 0 }} 字符</span>
                </div>
                <div class="step-row">
                  <span class="step-label">更新时间：</span>
                  <span>{{ step.updateTime || '未保存' }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 步骤编辑对话框 -->
        <el-dialog
          v-model="stepEditDialogVisible"
          :title="currentStepName + '配置'"
          width="900px"
          append-to-body
          :close-on-click-modal="false"
        >
          <el-form label-width="100px">
            <el-form-item label="环节说明">
              <el-alert
                :title="currentStepDescription"
                type="info"
                :closable="false"
                style="margin-bottom: 15px;"
              />
            </el-form-item>
            
            <el-form-item label="脚本类型">
              <el-radio-group v-model="stepForm.stepType">
                <el-radio label="groovy">Groovy 脚本</el-radio>
                <el-radio label="json">JSON 配置</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item v-if="stepForm.stepType === 'groovy'" label="Groovy 脚本">
              <el-input
                v-model="stepForm.code"
                type="textarea"
                :rows="25"
                placeholder="请输入 Groovy 脚本代码"
                style="font-family: 'Courier New', monospace; font-size: 13px;"
              />
              <div style="margin-top: 8px;">
                <el-button type="primary" size="small" @click="handleFormatGroovy">格式化代码</el-button>
                <el-button type="warning" size="small" @click="handleCheckSyntax">语法检查</el-button>
                <el-button type="info" size="small" @click="handleLoadTemplate">加载模板</el-button>
              </div>
            </el-form-item>

            <el-form-item v-else label="JSON 配置">
              <el-input
                v-model="stepForm.jsonConfig"
                type="textarea"
                :rows="25"
                placeholder='请输入 JSON 配置，例如：{"url": "https://api.example.com", "method": "GET"}'
                style="font-family: 'Courier New', monospace; font-size: 13px;"
              />
              <div style="margin-top: 8px;">
                <el-button type="primary" size="small" @click="handleFormatJson">格式化 JSON</el-button>
                <el-button type="warning" size="small" @click="handleValidateJson">JSON 校验</el-button>
              </div>
            </el-form-item>

            <el-form-item label="配置说明">
              <el-input
                v-model="stepForm.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入配置说明（选填）"
              />
            </el-form-item>
          </el-form>

          <template #footer>
            <el-button @click="stepEditDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSaveStep" :loading="stepSaving">保存配置</el-button>
          </template>
        </el-dialog>

        <!-- 配置预览对话框 -->
        <el-dialog
          v-model="previewDialogVisible"
          title="环节配置预览"
          width="800px"
          append-to-body
        >
          <el-descriptions :column="1" border>
            <el-descriptions-item label="环节名称">{{ previewData.name }}</el-descriptions-item>
            <el-descriptions-item label="环节类型">{{ getStepTypeName(previewData.type) }}</el-descriptions-item>
            <el-descriptions-item label="脚本类型">
              <el-tag size="small">{{ previewData.stepType === 'groovy' ? 'Groovy 脚本' : 'JSON 配置' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="脚本内容">
              <pre style="background: #f5f7fa; padding: 10px; border-radius: 4px; max-height: 400px; overflow: auto;">{{ previewData.code || previewData.jsonConfig || '暂无内容' }}</pre>
            </el-descriptions-item>
            <el-descriptions-item label="配置说明" :span="2">{{ previewData.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
          <template #footer>
            <el-button @click="previewDialogVisible = false">关闭</el-button>
          </template>
        </el-dialog>
      </div>
      <template #footer>
        <el-button @click="designDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="handleValidateAll">校验所有环节</el-button>
        <el-button type="primary" @click="handleSaveProcessSteps" :loading="savingAll">保存全部配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProcessList, deleteProcess, createProcess, updateProcess, getProcessDetail, getProcessScripts, updateProcessScripts, validateGroovyScript, validateAllProcessSteps } from '@/api/process'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const designDialogVisible = ref(false)
const stepEditDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const stepSaving = ref(false)
const savingAll = ref(false)
const currentRow = ref(null)
const currentProcessId = ref(null)
const currentStepIndex = ref(null)

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
  version: '1.0.0',
  description: '',
  remark: '',
  status: 1,
  steps: 0,
  collectScript: '',
  parseScript: '',
  processScript: '',
  saveScript: ''
})

// 步骤配置数据
const stepForm = reactive({
  stepName: '',
  stepType: 'groovy',
  code: '',
  jsonConfig: '',
  remark: ''
})

const steps = ref([
  { 
    id: 1, 
    name: '采集环节', 
    type: 'collect',
    stepType: 'groovy',
    code: '', 
    jsonConfig: '',
    remark: '',
    configured: false,
    order: 1 
  },
  { 
    id: 2, 
    name: '解析环节', 
    type: 'parse',
    stepType: 'groovy',
    code: '', 
    jsonConfig: '',
    remark: '',
    configured: false,
    order: 2 
  },
  { 
    id: 3, 
    name: '加工环节', 
    type: 'process',
    stepType: 'groovy',
    code: '', 
    jsonConfig: '',
    remark: '',
    configured: false,
    order: 3 
  },
  { 
    id: 4, 
    name: '落库环节', 
    type: 'save',
    stepType: 'groovy',
    code: '', 
    jsonConfig: '',
    remark: '',
    configured: false,
    order: 4 
  }
])

// 预览数据
const previewData = reactive({
  name: '',
  type: '',
  stepType: '',
  code: '',
  jsonConfig: '',
  remark: ''
})

const formRef = ref(null)

const formRules = {
  processCode: [{ required: true, message: '请输入流程编码', trigger: 'blur' }],
  processName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }]
}

// 计算属性：当前环节名称和描述
const currentStepName = computed(() => {
  if (currentStepIndex.value === null) return ''
  return steps.value[currentStepIndex.value].name
})

const currentStepDescription = computed(() => {
  const descriptions = {
    0: '采集环节负责从各种数据源（如 HTTP 接口、数据库、文件等）采集原始数据。请编写 Groovy 脚本实现数据采集逻辑。',
    1: '解析环节负责对采集的原始数据进行解析，提取关键字段。请编写 Groovy 脚本解析 JSON、XML、HTML 等格式的数据。',
    2: '加工环节负责对解析后的数据进行转换、计算、校验等处理。请编写 Groovy 脚本实现数据加工逻辑。',
    3: '落库环节负责将处理后的数据保存到数据库。请编写 Groovy 脚本实现数据插入、更新等操作。'
  }
  return descriptions[currentStepIndex.value] || ''
})

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
    status: 1,
    steps: 0
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
    console.log('=== 编辑流程，获取到的数据 ===')
    console.log('res.data:', res.data)
    console.log('category:', res.data.category)
    
    isEdit.value = true
    Object.assign(formData, res.data)
    console.log('赋值后的 formData:', formData)
    console.log('formData.category:', formData.category)
    
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
    currentProcessId.value = row.id
    // 先获取流程详情
    const res = await getProcessDetail(row.id)
    
    // 重置步骤配置
    steps.value.forEach(step => {
      step.configured = false
      step.code = ''
      step.jsonConfig = ''
      step.remark = ''
      step.updateTime = ''
    })
    
    // 获取流程脚本
    try {
      const scriptRes = await getProcessScripts(row.id)
      const scripts = scriptRes.data
      
      // 获取当前时间
      const currentTime = new Date().toLocaleString()
      
      if (scripts.collectScript) {
        steps.value[0].code = scripts.collectScript
        steps.value[0].configured = true
        steps.value[0].updateTime = currentTime
      }
      if (scripts.parseScript) {
        steps.value[1].code = scripts.parseScript
        steps.value[1].configured = true
        steps.value[1].updateTime = currentTime
      }
      if (scripts.processScript) {
        steps.value[2].code = scripts.processScript
        steps.value[2].configured = true
        steps.value[2].updateTime = currentTime
      }
      if (scripts.saveScript) {
        steps.value[3].code = scripts.saveScript
        steps.value[3].configured = true
        steps.value[3].updateTime = currentTime
      }
    } catch (e) {
      console.log('获取脚本失败，使用空配置')
    }
    
    designDialogVisible.value = true
  } catch (error) {
    console.error('获取流程详情失败:', error)
    ElMessage.error('获取流程详情失败')
  }
}

// 获取环节标签类型
const getStepTagType = (index) => {
  const types = ['success', 'warning', 'primary', 'info']
  return types[index] || 'info'
}

// 获取环节类型名称
const getStepTypeName = (type) => {
  const names = {
    'collect': '采集环节',
    'parse': '解析环节',
    'process': '加工环节',
    'save': '落库环节'
  }
  return names[type] || type
}

const handleEditStep = (index) => {
  currentStepIndex.value = index
  const step = steps.value[index]
  stepForm.stepName = step.name
  stepForm.stepType = step.stepType || 'groovy'
  stepForm.code = step.code || ''
  stepForm.jsonConfig = step.jsonConfig || ''
  stepForm.remark = step.remark || ''
  stepEditDialogVisible.value = true
}

const handlePreviewStep = (index) => {
  const step = steps.value[index]
  previewData.name = step.name
  previewData.type = step.type
  previewData.stepType = step.stepType
  previewData.code = step.code
  previewData.jsonConfig = step.jsonConfig
  previewData.remark = step.remark
  previewDialogVisible.value = true
}

// 格式化 Groovy 代码
const handleFormatGroovy = () => {
  if (!stepForm.code) {
    ElMessage.warning('代码为空')
    return
  }
  // 简单的格式化：去除多余空行
  stepForm.code = stepForm.code.replace(/\n\s*\n/g, '\n').trim()
  ElMessage.success('代码格式化完成')
}

// 检查 Groovy 语法
const handleCheckSyntax = () => {
  if (!stepForm.code) {
    ElMessage.warning('代码为空，请输入 Groovy 脚本')
    return
  }
  
  const errors = []
  const warnings = []
  
  // 1. 括号匹配检查
  const brackets = { '(': 0, '{': 0, '[': 0 }
  const bracketLines = { '(': [], '{': [], '[': [] }
  const lines = stepForm.code.split('\n')
  
  for (let lineIndex = 0; lineIndex < lines.length; lineIndex++) {
    const line = lines[lineIndex]
    for (let charIndex = 0; charIndex < line.length; charIndex++) {
      const char = line[charIndex]
      if (brackets.hasOwnProperty(char)) {
        brackets[char]++
        bracketLines[char].push({ line: lineIndex + 1, col: charIndex + 1 })
      }
      if (char === ')') {
        brackets['(']--
        if (brackets['('] < 0) {
          errors.push(`第 ${lineIndex + 1} 行：多余的右圆括号`)
        } else {
          bracketLines['('].pop()
        }
      }
      if (char === '}') {
        brackets['{']--
        if (brackets['{'] < 0) {
          errors.push(`第 ${lineIndex + 1} 行：多余的右花括号`)
        } else {
          bracketLines['{'].pop()
        }
      }
      if (char === ']') {
        brackets['[']--
        if (brackets['['] < 0) {
          errors.push(`第 ${lineIndex + 1} 行：多余的右方括号`)
        } else {
          bracketLines['['].pop()
        }
      }
    }
  }
  
  if (brackets['{'] > 0) {
    const last = bracketLines['{'][bracketLines['{'].length - 1]
    errors.push(`第 ${last.line} 行：缺少右花括号 (共缺少 ${brackets['{']} 个)`)
  }
  if (brackets['('] > 0) {
    const last = bracketLines['('][bracketLines['('].length - 1]
    errors.push(`第 ${last.line} 行：缺少右圆括号 (共缺少 ${brackets['(']} 个)`)
  }
  if (brackets['['] > 0) {
    const last = bracketLines['['][bracketLines['['].length - 1]
    errors.push(`第 ${last.line} 行：缺少右方括号 (共缺少 ${brackets['[']} 个)`)
  }
  
  // 2. 字符串引号匹配
  const stringRegex = /["'`]/g
  let match
  const stringPositions = []
  while ((match = stringRegex.exec(stepForm.code)) !== null) {
    const lineNum = stepForm.code.substring(0, match.index).split('\n').length
    stringPositions.push({ quote: match[0], line: lineNum, index: match.index })
  }
  
  const quoteStack = []
  for (let i = 0; i < stringPositions.length; i++) {
    const current = stringPositions[i]
    if (quoteStack.length === 0) {
      quoteStack.push(current)
    } else {
      const last = quoteStack[quoteStack.length - 1]
      if (current.quote === last.quote) {
        quoteStack.pop()
      } else {
        quoteStack.push(current)
      }
    }
  }
  
  if (quoteStack.length > 0) {
    errors.push(`第 ${quoteStack[0].line} 行：字符串引号未闭合`)
  }
  
  // 3. 常见语法错误
  lines.forEach((line, index) => {
    const lineNum = index + 1
    const trimmedLine = line.trim()
    
    // 跳过注释和空行
    if (trimmedLine.startsWith('//') || trimmedLine.startsWith('/*') || trimmedLine === '') {
      return
    }
    
    // 检查 def 关键字后是否有函数名
    if (/^\s*def\s+$/.test(line)) {
      warnings.push(`第 ${lineNum} 行：def 关键字后缺少函数名`)
    }
    
    // 检查 if/for/while 后是否有条件
    if (/^\s*(if|while|for)\s*\(\s*\)\s*\{?\s*$/.test(line)) {
      warnings.push(`第 ${lineNum} 行：${line.match(/^\s*(if|while|for)/)[1]} 语句缺少条件表达式`)
    }
    
    // 检查 return 是否在函数内（简单检查）
    if (/^\s*return\s+/.test(line) && !stepForm.code.includes('def ')) {
      warnings.push(`第 ${lineNum} 行：return 语句应在函数内`)
    }
  })
  
  // 4. 检查是否包含有效的 Groovy 代码结构
  const hasFunctionDef = /def\s+\w+\s*\(/.test(stepForm.code)
  const hasVariableDef = /\bdef\s+\w+\s*=/.test(stepForm.code)
  // 改进方法调用检测：匹配 methodname(...) 格式，包含逗号等参数
  const hasMethodCall = /[a-zA-Z_]\w*\s*\([^)]*\)/.test(stepForm.code)
  const hasComment = /\/\/|\/\*|\*\//.test(stepForm.code)
  const hasString = /["'`][^"'`]*["'`]/.test(stepForm.code)
  const hasKeyword = /\b(def|if|else|for|while|return|try|catch|throw|new|class|import)\b/.test(stepForm.code)
  
  // 5. 检查代码是否只是随意输入的无效文本
  const codeWithoutComments = stepForm.code.replace(/\/\/.*$/gm, '').replace(/\/\*[\s\S]*?\*\//g, '').trim()
  
  // 调试输出
  console.log('=== 语法检查调试 ===')
  console.log('代码:', stepForm.code)
  console.log('hasFunctionDef:', hasFunctionDef)
  console.log('hasVariableDef:', hasVariableDef)
  console.log('hasMethodCall:', hasMethodCall)
  console.log('hasKeyword:', hasKeyword)
  console.log('hasString:', hasString)
  console.log('codeWithoutComments:', codeWithoutComments)
  
  if (codeWithoutComments) {
    // 检查是否只是一堆字母数字组合（随意输入的文本）
    const linesWithoutComments = codeWithoutComments.split('\n').filter(l => l.trim())
    const allLinesAreGibberish = linesWithoutComments.every(l => {
      const trimmed = l.trim()
      // 如果一行只是字母数字组合，没有空格、运算符等，很可能是乱输入的
      return /^[a-zA-Z0-9_]+$/.test(trimmed) && trimmed.length < 50
    })
    
    console.log('linesWithoutComments:', linesWithoutComments)
    console.log('allLinesAreGibberish:', allLinesAreGibberish)
    
    if (allLinesAreGibberish && linesWithoutComments.length > 0) {
      errors.push('代码看起来像随意输入的文本，不是有效的 Groovy 语法')
      warnings.push('请编写有效的 Groovy 代码，如：def functionName() { ... }')
    }
    
    // 如果代码既没有关键字，也没有函数定义、变量定义、方法调用、字符串，那很可能是无效代码
    if (!hasKeyword && !hasFunctionDef && !hasVariableDef && !hasMethodCall && !hasString) {
      if (!allLinesAreGibberish) {
        // 如果不是乱码但仍然没有有效语法，给出警告
        warnings.push('未检测到有效的 Groovy 语法结构')
      }
    }
  }
  
  // 6. RPA 场景建议
  if (!hasFunctionDef && codeWithoutComments) {
    warnings.push('未检测到函数定义，建议定义如 def collect() { ... } 的函数')
  }
  
  // 显示结果
  if (errors.length > 0) {
    ElMessageBox.alert(
      errors.join('<br>') + (warnings.length > 0 ? '<br><br>警告：<br>' + warnings.join('<br>') : ''),
      '语法检查失败',
      { 
        type: 'error',
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确定'
      }
    )
  } else if (warnings.length > 0) {
    ElMessageBox.alert(
      warnings.join('<br>'),
      '语法检查完成（有警告）',
      { 
        type: 'warning',
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确定'
      }
    )
  } else {
    ElMessage.success('语法检查通过（基础检查）')
  }
}

// 加载模板
const handleLoadTemplate = () => {
  const templates = {
    'collect': `// 采集环节模板
def collect() {
    // HTTP 请求示例
    def url = "https://api.example.com/data"
    def params = [page: 1, size: 10]
    
    // 发送请求
    def response = httpRequest(url, params)
    
    // 返回采集的数据
    return response.data
}`,
    'parse': `// 解析环节模板
def parse(data) {
    // 解析 JSON 数据示例
    def result = []
    
    if (data instanceof List) {
        data.each { item ->
            result.add([
                id: item.id,
                name: item.name,
                value: item.value
            ])
        }
    }
    
    return result
}`,
    'process': `// 加工环节模板
def process(data) {
    // 数据转换示例
    data.each { item ->
        // 添加计算字段
        item.total = item.price * item.quantity
        
        // 数据校验
        if (!item.name) {
            throw new Exception("名称不能为空")
        }
    }
    
    return data
}`,
    'save': `// 落库环节模板
def save(data) {
    // 批量插入示例
    data.each { item ->
        // 构建 SQL
        def sql = """
            INSERT INTO target_table 
            (id, name, value, create_time) 
            VALUES (?, ?, ?, NOW())
        """
        
        // 执行插入
        executeSql(sql, [item.id, item.name, item.value])
    }
    
    return true
}`
  }
  
  const currentType = steps.value[currentStepIndex.value].type
  stepForm.code = templates[currentType] || ''
  ElMessage.success('模板已加载')
}

// 格式化 JSON
const handleFormatJson = () => {
  if (!stepForm.jsonConfig) {
    ElMessage.warning('JSON 配置为空')
    return
  }
  try {
    const obj = JSON.parse(stepForm.jsonConfig)
    stepForm.jsonConfig = JSON.stringify(obj, null, 2)
    ElMessage.success('JSON 格式化完成')
  } catch (e) {
    ElMessage.error('JSON 格式错误：' + e.message)
  }
}

// 验证 JSON
const handleValidateJson = () => {
  if (!stepForm.jsonConfig) {
    ElMessage.warning('JSON 配置为空')
    return
  }
  try {
    JSON.parse(stepForm.jsonConfig)
    ElMessage.success('JSON 格式验证通过')
  } catch (e) {
    ElMessage.error('JSON 格式错误：' + e.message)
  }
}

const handleSaveStep = async () => {
  if (!stepForm.code && !stepForm.jsonConfig) {
    ElMessage.warning('请输入脚本内容或 JSON 配置')
    return
  }
  
  // 如果是 Groovy 脚本，先进行后端语法校验
  if (stepForm.stepType === 'groovy' && stepForm.code && stepForm.code.trim()) {
    try {
      console.log('=== 正在校验单个环节 Groovy 脚本语法 ===')
      console.log('脚本内容:', stepForm.code)
      
      const validationResponse = await validateGroovyScript({
        script: stepForm.code,
        stepName: steps.value[currentStepIndex.value].name
      })
      
      console.log('校验响应:', validationResponse)
      console.log('校验响应 data:', validationResponse.data)
      
      // 灵活处理不同的返回格式
      // 格式 1: {code: 200, data: {valid: true, message: '...', errors: []}}
      // 格式 2: {valid: true, message: '...', errors: []}
      let valid, message, errors
      
      if (validationResponse.data && validationResponse.data.data) {
        // 格式 1：标准 ApiResponse 格式
        valid = validationResponse.data.data.valid
        message = validationResponse.data.data.message || ''
        errors = validationResponse.data.data.errors || []
      } else if (validationResponse.data && validationResponse.data.valid !== undefined) {
        // 格式 2：直接返回校验结果
        valid = validationResponse.data.valid
        message = validationResponse.data.message || ''
        errors = validationResponse.data.errors || []
      } else {
        throw new Error('校验 API 返回数据格式错误：' + JSON.stringify(validationResponse))
      }
      
      console.log('校验详情:', { valid, message, errors })
      
      if (valid !== true) {
        // 语法校验失败，阻止保存
        console.error('语法校验失败，阻止保存')
        let errorMsg = '语法校验失败'
        if (message) {
          errorMsg += ': ' + message
        }
        if (errors && errors.length > 0) {
          errorMsg += '<br><br><b>错误详情：</b><br>' + errors.join('<br>')
        }
        
        ElMessageBox.alert(errorMsg, '❌ 语法校验失败', {
          type: 'error',
          dangerouslyUseHTMLString: true,
          confirmButtonText: '确定'
        })
        return  // 阻止保存
      }
      
      console.log('✓ Groovy 脚本语法校验通过，允许保存')
    } catch (error) {
      console.error('Groovy 脚本语法校验异常:', error)
      let errorMsg = '语法校验失败'
      if (error.response?.data?.message) {
        errorMsg += ': ' + error.response.data.message
      } else if (error.message) {
        errorMsg += ': ' + error.message
      }
      
      ElMessageBox.alert(errorMsg, '❌ 语法校验失败', {
        type: 'error',
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确定'
      })
      return  // 阻止保存
    }
  }
  
  // 校验通过（或不是 Groovy 脚本），继续保存
  stepSaving.value = true
  try {
    const index = currentStepIndex.value
    const step = steps.value[index]
    
    // 更新前端内存
    steps.value[index] = {
      ...steps.value[index],
      stepType: stepForm.stepType,
      code: stepForm.code,
      jsonConfig: stepForm.jsonConfig,
      remark: stepForm.remark,
      configured: true,
      updateTime: new Date().toLocaleString()
    }
    
    // 调用后端 API 保存到数据库
    const scripts = {
      collectScript: steps.value[0].stepType === 'groovy' ? steps.value[0].code : steps.value[0].jsonConfig,
      parseScript: steps.value[1].stepType === 'groovy' ? steps.value[1].code : steps.value[1].jsonConfig,
      processScript: steps.value[2].stepType === 'groovy' ? steps.value[2].code : steps.value[2].jsonConfig,
      saveScript: steps.value[3].stepType === 'groovy' ? steps.value[3].code : steps.value[3].jsonConfig
    }
    
    console.log('保存流程脚本到数据库，流程 ID:', currentProcessId.value)
    console.log('脚本数据:', scripts)
    
    await updateProcessScripts(currentProcessId.value, scripts)
    
    ElMessage.success(`${step.name}保存成功，已同步到数据库`)
    stepEditDialogVisible.value = false
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败：' + (error.response?.data?.message || error.message))
  } finally {
    stepSaving.value = false
  }
}

// 校验所有环节（调用后端 API 进行 Groovy 语法校验）
const handleValidateAll = async () => {
  // 1. 先检查是否所有环节都已配置
  const unconfigured = steps.value.filter(s => !s.configured)
  if (unconfigured.length > 0) {
    ElMessageBox.alert(
      `以下环节未配置：${unconfigured.map(s => s.name).join('、')}，请先配置后再校验`,
      '校验失败',
      { type: 'warning' }
    )
    return
  }
  
  // 2. 检查是否有 Groovy 脚本需要校验
  const hasGroovyScript = steps.value.some(s => s.stepType === 'groovy' && s.code && s.code.trim())
  if (!hasGroovyScript) {
    ElMessageBox.alert(
      '没有需要校验的 Groovy 脚本（所有环节都是 JSON 配置或空脚本）',
      '提示',
      { type: 'info' }
    )
    return
  }
  
  try {
    // 3. 调用后端 API 校验所有环节
    console.log('=== 开始校验所有环节 ===')
    const response = await validateAllProcessSteps(currentProcessId.value)
    
    console.log('校验结果:', response.data)
    
    // 灵活处理不同的返回格式
    let allValid, message, stepResults
    
    if (response.data && response.data.data) {
      // 格式 1：标准 ApiResponse 格式
      allValid = response.data.data.allValid
      message = response.data.data.message || ''
      stepResults = response.data.data.stepResults || {}
    } else if (response.data && response.data.allValid !== undefined) {
      // 格式 2：直接返回校验结果
      allValid = response.data.allValid
      message = response.data.message || ''
      stepResults = response.data.stepResults || {}
    } else {
      throw new Error('校验 API 返回数据格式错误')
    }
    
    console.log('校验结果详情:', { allValid, message, stepResults })
    
    if (allValid === true) {
      ElMessage.success('✅ 所有环节语法校验通过！')
    } else {
      // 校验失败，显示错误详情
      let errorDetail = ''
      if (message) {
        errorDetail = message
      }
      if (stepResults && Object.keys(stepResults).length > 0) {
        errorDetail += '<br><br><b>各环节校验结果：</b><br>'
        Object.entries(stepResults).forEach(([key, value]) => {
          if (!value.valid) {
            errorDetail += `<br>❌ ${value.stepName}: ${value.message || '语法错误'}`
          }
        })
      }
      
      ElMessageBox.alert(
        errorDetail || '语法校验失败',
        '❌ 语法校验失败',
        { 
          type: 'error',
          dangerouslyUseHTMLString: true
        }
      )
    }
  } catch (error) {
    console.error('语法校验失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '语法校验失败'
    ElMessageBox.alert(errorMsg, '❌ 语法校验失败', { type: 'error' })
  }
}

const handleSaveProcessSteps = async () => {
  // 1. 先检查是否所有环节都已配置
  const unconfigured = steps.value.filter(s => !s.configured)
  if (unconfigured.length > 0) {
    ElMessageBox.confirm(
      `以下环节未配置：${unconfigured.map(s => s.name).join('、')}，是否继续保存？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      // 用户确认继续，但还是要先校验语法
      validateAndSave()
    }).catch(() => {
      // 用户取消
    })
    return
  }
  
  // 2. 所有环节已配置，进行语法校验
  await validateAndSave()
}

// 校验语法并保存
const validateAndSave = async () => {
  console.log('=== 开始校验并保存流程 ===')
  
  try {
    // 调用后端 API 校验所有环节
    console.log('调用校验 API...')
    const validationResponse = await validateAllProcessSteps(currentProcessId.value)
    
    console.log('校验响应:', validationResponse)
    console.log('校验响应 data:', validationResponse.data)
    
    // 灵活处理不同的返回格式
    let allValid, message, errors
    
    if (validationResponse.data && validationResponse.data.data) {
      // 格式 1：标准 ApiResponse 格式
      allValid = validationResponse.data.data.allValid
      message = validationResponse.data.data.message || ''
      errors = validationResponse.data.data.errors || []
    } else if (validationResponse.data && validationResponse.data.allValid !== undefined) {
      // 格式 2：直接返回校验结果
      allValid = validationResponse.data.allValid
      message = validationResponse.data.message || ''
      errors = validationResponse.data.errors || []
    } else {
      throw new Error('校验 API 返回数据格式错误：' + JSON.stringify(validationResponse))
    }
    
    console.log('校验结果详情:', { allValid, message, errors })
    
    if (allValid === true) {
      // 语法校验通过，保存到后端
      console.log('校验通过，开始保存...')
      await saveToBackend()
    } else {
      // 语法校验失败，阻止保存
      console.error('校验失败，阻止保存')
      let errorMsg = '语法校验失败'
      if (message) {
        errorMsg += ': ' + message
      }
      if (errors && errors.length > 0) {
        errorMsg += '<br><br><b>错误详情：</b><br>' + errors.join('<br>')
      }
      
      ElMessageBox.alert(errorMsg, '❌ 语法校验失败', { 
        type: 'error',
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确定'
      })
    }
  } catch (error) {
    console.error('校验过程异常:', error)
    let errorMsg = '语法校验失败'
    if (error.response?.data?.message) {
      errorMsg += ': ' + error.response.data.message
    } else if (error.message) {
      errorMsg += ': ' + error.message
    }
    
    ElMessageBox.alert(errorMsg, '❌ 语法校验失败', { 
      type: 'error',
      confirmButtonText: '确定'
    })
    // 发生异常时，绝对不要保存
    return
  }
}

const saveToBackend = async () => {
  savingAll.value = true
  try {
    // 构建脚本数据
    const scripts = {
      collectScript: steps.value[0].stepType === 'groovy' ? steps.value[0].code : steps.value[0].jsonConfig,
      parseScript: steps.value[1].stepType === 'groovy' ? steps.value[1].code : steps.value[1].jsonConfig,
      processScript: steps.value[2].stepType === 'groovy' ? steps.value[2].code : steps.value[2].jsonConfig,
      saveScript: steps.value[3].stepType === 'groovy' ? steps.value[3].code : steps.value[3].jsonConfig
    }
    
    // 调用后端 API 保存
    await updateProcessScripts(currentProcessId.value, scripts)
    
    ElMessage.success('流程配置保存成功')
    designDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存流程配置失败:', error)
    ElMessage.error(error.response?.data?.message || '保存流程配置失败')
  } finally {
    savingAll.value = false
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
      console.log('=== 提交流程数据 ===')
      console.log('formData:', formData)
      console.log('category:', formData.category)
      
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
  return status === 1 ? 'success' : 'info'
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

      .step-actions {
        display: flex;
        gap: 8px;
      }
    }

    .step-content {
      .step-info {
        .step-row {
          display: flex;
          align-items: center;
          margin-bottom: 8px;
          font-size: 14px;

          &:last-child {
            margin-bottom: 0;
          }

          .step-label {
            color: #909399;
            margin-right: 10px;
            min-width: 80px;
          }
        }
      }
    }
  }
}
</style>
