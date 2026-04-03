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
          <el-input v-model="searchForm.permName" placeholder="请输入资源名称" clearable />
        </el-form-item>
        <el-form-item label="资源标识">
          <el-input v-model="searchForm.permKey" placeholder="请输入资源标识" clearable />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="searchForm.permType" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="菜单" :value="1" />
            <el-option label="按钮" :value="2" />
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
        <el-table-column prop="permName" label="资源名称" />
        <el-table-column prop="permKey" label="资源标识" />
        <el-table-column prop="permType" label="资源类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.permType === 1 ? '' : 'warning'" size="small">
              {{ row.permType === 1 ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parentId" label="父级 ID" width="90">
          <template #default="{ row }">
            {{ row.parentId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
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
            :props="{ children: 'children', label: 'permName', value: 'id' }"
            check-strictly
            placeholder="选择父级资源（可选，最顶级不选）"
            clearable
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="资源名称" prop="permName">
          <el-input v-model="formData.permName" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="资源标识" prop="permKey">
          <el-input v-model="formData.permKey" placeholder="请输入资源标识，如 sys:user:add" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="资源类型" prop="permType">
          <el-radio-group v-model="formData.permType">
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="资源详情"
      width="450px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="资源名称">{{ detailData.permName }}</el-descriptions-item>
        <el-descriptions-item label="资源标识">{{ detailData.permKey }}</el-descriptions-item>
        <el-descriptions-item label="资源类型">
          <el-tag :type="detailData.permType === 1 ? '' : 'warning'" size="small">
            {{ detailData.permType === 1 ? '菜单' : '按钮' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="父级 ID">{{ detailData.parentId || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getResourceList,
  getResourceTree,
  createResource,
  updateResource,
  deleteResource,
  getResourceDetail
} from '@/api/resource'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const treeData = ref([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const detailData = reactive({
  permName: '',
  permKey: '',
  permType: null,
  parentId: null,
  createTime: ''
})

const searchForm = reactive({
  permName: '',
  permKey: '',
  permType: null
})

const formData = reactive({
  id: null,
  parentId: null,
  permName: '',
  permKey: '',
  permType: 1
})

const formRules = {
  permName: [{ required: true, message: '请输入资源名称', trigger: 'blur' }],
  permKey: [
    { required: true, message: '请输入资源标识', trigger: 'blur' },
    { pattern: /^[\w:]+$/, message: '标识只能包含字母、数字、下划线和冒号', trigger: 'blur' }
  ],
  permType: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      permName: searchForm.permName || undefined,
      permKey: searchForm.permKey || undefined,
      permType: searchForm.permType ?? undefined
    }
    const res = await getResourceList(params)
    tableData.value = res.data || []
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载资源列表失败')
  } finally {
    loading.value = false
  }
}

const loadTreeData = async () => {
  try {
    const res = await getResourceTree()
    treeData.value = res.data || []
  } catch (error) {
    console.error('加载权限树失败:', error)
  }
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  searchForm.permName = ''
  searchForm.permKey = ''
  searchForm.permType = null
  loadData()
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    parentId: null,
    permName: '',
    permKey: '',
    permType: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    parentId: row.parentId || null,
    permName: row.permName,
    permKey: row.permKey,
    permType: row.permType
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  Object.assign(detailData, {
    permName: row.permName,
    permKey: row.permKey,
    permType: row.permType,
    parentId: row.parentId,
    createTime: row.createTime || '-'
  })
  detailDialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    row.children?.length
      ? `"${row.permName}" 存在子资源，删除将自动删除所有子资源，确认删除吗？`
      : `确认删除资源「${row.permName}」吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteResource(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateResource(formData.id, {
          permName: formData.permName,
          parentId: formData.parentId || null
        })
      } else {
        await createResource({
          permName: formData.permName,
          permKey: formData.permKey,
          permType: formData.permType,
          parentId: formData.parentId || null
        })
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

onMounted(() => {
  loadData()
  loadTreeData()
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
