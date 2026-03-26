<template>
  <div class="resource-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>资源管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建资源
          </el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="资源名称">
          <el-input v-model="searchForm.keyword" placeholder="请输入资源名称" clearable />
        </el-form-item>
        
        <el-form-item label="资源编码">
          <el-input v-model="searchForm.resourceCode" placeholder="请输入资源编码" clearable />
        </el-form-item>
        
        <el-form-item label="资源类型">
          <el-select v-model="searchForm.resourceType" placeholder="请选择" clearable>
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
            <el-option label="接口" value="api" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
        row-key="id"
        border
        style="width: 100%"
        :tree-props="{ children: 'children' }"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="resourceName" label="资源名称" />
        <el-table-column prop="resourceCode" label="资源编码" />
        <el-table-column prop="resourceType" label="资源类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getResourceTypeTag(row.resourceType)" size="small">
              {{ getResourceTypeText(row.resourceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径/URL" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
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
      :title="dialogTitle"
      width="500px"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="父级资源">
          <el-tree-select
            v-model="formData.parentId"
            :data="treeData"
            :props="{ children: 'children', label: 'resourceName', value: 'id' }"
            check-strictly
            placeholder="选择父级资源（可选）"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="资源名称" prop="resourceName">
          <el-input v-model="formData.resourceName" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="资源编码" prop="resourceCode">
          <el-input v-model="formData.resourceCode" placeholder="请输入资源编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="formData.resourceType" placeholder="请选择资源类型" style="width: 100%;">
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
            <el-option label="接口" value="api" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径/URL" prop="path">
          <el-input v-model="formData.path" placeholder="请输入路径或 URL" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="formData.icon" placeholder="请输入图标 class（如：User）" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" placeholder="请输入描述" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({
  keyword: '',
  resourceCode: '',
  resourceType: '',
  status: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive({
  id: null,
  parentId: null,
  resourceName: '',
  resourceCode: '',
  resourceType: 'menu',
  path: '',
  icon: '',
  sortOrder: 0,
  status: 1,
  description: ''
})

const formRules = {
  resourceName: [{ required: true, message: '请输入资源名称', trigger: 'blur' }],
  resourceCode: [{ required: true, message: '请输入资源编码', trigger: 'blur' }],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
}

const formRef = ref(null)

// 模拟树形数据
const treeData = [
  {
    id: 1,
    resourceName: '系统管理',
    resourceCode: 'SYSTEM',
    children: [
      { id: 11, resourceName: '用户管理', resourceCode: 'USER_MANAGE' },
      { id: 12, resourceName: '角色管理', resourceCode: 'ROLE_MANAGE' },
      { id: 13, resourceName: '资源管理', resourceCode: 'RESOURCE_MANAGE' }
    ]
  },
  {
    id: 2,
    resourceName: '机器人管理',
    resourceCode: 'ROBOT',
    children: []
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      resourceCode: searchForm.resourceCode,
      resourceType: searchForm.resourceType,
      status: searchForm.status
    }
    
    // TODO: 调用 API
    // const res = await getResourceList(params)
    // tableData.value = res.data.records || []
    // pagination.total = res.data.total || 0
    
    // 临时测试数据
    tableData.value = [
      {
        id: 1,
        parentId: null,
        resourceName: '系统管理',
        resourceCode: 'SYSTEM',
        resourceType: 'menu',
        path: '/system',
        icon: 'Setting',
        sortOrder: 1,
        status: 1,
        description: '系统管理模块',
        children: [
          {
            id: 11,
            parentId: 1,
            resourceName: '用户管理',
            resourceCode: 'USER_MANAGE',
            resourceType: 'menu',
            path: '/system/user',
            icon: 'User',
            sortOrder: 1,
            status: 1,
            description: ''
          },
          {
            id: 12,
            parentId: 1,
            resourceName: '角色管理',
            resourceCode: 'ROLE_MANAGE',
            resourceType: 'menu',
            path: '/system/role',
            icon: 'Avatar',
            sortOrder: 2,
            status: 1,
            description: ''
          }
        ]
      }
    ]
    pagination.total = 1
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
  searchForm.resourceCode = ''
  searchForm.resourceType = ''
  searchForm.status = null
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
    parentId: null,
    resourceName: '',
    resourceCode: '',
    resourceType: 'menu',
    path: '',
    icon: '',
    sortOrder: 0,
    status: 1,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    parentId: row.parentId,
    resourceName: row.resourceName,
    resourceCode: row.resourceCode,
    resourceType: row.resourceType,
    path: row.path,
    icon: row.icon,
    sortOrder: row.sortOrder,
    status: row.status,
    description: row.description
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info('查看详情功能待实现')
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该资源吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // TODO: 调用删除 API
      // await deleteResource(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      // TODO: 调用创建或更新 API
      // if (isEdit.value) {
      //   await updateResource(formData.id, formData)
      // } else {
      //   await createResource(formData)
      // }
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('操作失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

const getResourceTypeTag = (type) => {
  const map = {
    menu: '',
    button: 'warning',
    api: 'danger'
  }
  return map[type] || 'info'
}

const getResourceTypeText = (type) => {
  const map = {
    menu: '菜单',
    button: '按钮',
    api: '接口'
  }
  return map[type] || type
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.resource-management {
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
