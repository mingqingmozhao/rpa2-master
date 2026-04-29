<template>
  <div class="role-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建角色
          </el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.keyword" placeholder="请输入角色名称" clearable />
        </el-form-item>

        <el-form-item label="角色编码">
          <el-input v-model="searchForm.roleCode" placeholder="请输入角色编码" clearable />
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
        border
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="remark" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleAssignPermission(row)">分配权限</el-button>
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
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="formData.remark" type="textarea" placeholder="请输入描述" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="600px"
    >
      <el-tree
        ref="treeRef"
        :data="permissionTreeData"
        :props="{ children: 'children', label: 'name', disabled: 'disabled' }"
        show-checkbox
        node-key="id"
        default-expand-all
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignPermissionSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="角色详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="角色名称">{{ viewData.roleName }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ viewData.remark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'danger'" size="small">
            {{ viewData.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="权限列表">
          <el-tag
            v-for="perm in viewData.permissions"
            :key="perm.id"
            size="small"
            style="margin-right: 5px; margin-bottom: 5px;"
          >
            {{ perm.permName }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, assignPermissions, getRolePermissions, getPermissionTree, getRoleDetail } from '@/api/role'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const isEdit = ref(false)
const currentRoleId = ref(null)

const permissionTreeData = ref([])

const searchForm = reactive({
  keyword: '',
  roleCode: '',
  status: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive({
  id: null,
  roleName: '',
  remark: '',
  status: 1
})

const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const formRef = ref(null)
const treeRef = ref(null)

// 查看详情相关
const viewDialogVisible = ref(false)
const viewData = ref({
  id: null,
  roleName: '',
  remark: '',
  status: 1,
  createTime: '',
  permissions: []
})

// 模拟权限树数据（仅当接口失败时兜底）
const fallbackTreeData = [
  {
    id: 1,
    name: '系统管理',
    children: [
      { id: 11, name: '用户管理' },
      { id: 12, name: '角色管理' },
      { id: 13, name: '资源管理' }
    ]
  },
  {
    id: 2,
    name: '机器人管理',
    children: [
      { id: 21, name: '查看机器人' },
      { id: 22, name: '新建机器人' },
      { id: 23, name: '编辑机器人' },
      { id: 24, name: '删除机器人' }
    ]
  },
  {
    id: 3,
    name: '任务管理',
    children: [
      { id: 31, name: '查看任务' },
      { id: 32, name: '新建任务' },
      { id: 33, name: '执行任务' },
      { id: 34, name: '删除任务' }
    ]
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      roleCode: searchForm.roleCode || undefined,
      status: searchForm.status ?? undefined
    }

    const res = await getRoleList(params)
    // RolePageResponse: { records: [...], total, page, pageSize }
    const pageData = res.data
    tableData.value = pageData?.records || pageData || []
    pagination.total = pageData?.total || (Array.isArray(res.data) ? res.data.length : 0)
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error(error.response?.data?.message || '加载角色列表失败')
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
  searchForm.roleCode = ''
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
    roleName: '',
    remark: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    roleName: row.roleName,
    remark: row.remark,
    status: row.status
  })
  dialogVisible.value = true
}

const handleView = async (row) => {
  try {
    // 获取角色详情（包括权限列表）
    const response = await getRoleDetail(row.id)
    // API 返回的数据在 data 字段中
    const detail = response.data
    viewData.value = {
      id: detail.id,
      roleName: detail.roleName,
      remark: detail.remark || '无',
      status: detail.status,
      createTime: detail.createTime,
      permissions: detail.permissions || []
    }
    viewDialogVisible.value = true
  } catch (error) {
    console.error('获取角色详情失败:', error)
    ElMessage.error('获取角色详情失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该角色吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '删除失败')
    }
  })
}

const handleAssignPermission = async (row) => {
  currentRoleId.value = row.id
  permissionDialogVisible.value = true
  try {
    // 加载权限树
    const treeRes = await getPermissionTree()
    permissionTreeData.value = treeRes.data || []
    // 加载该角色已有权限并回显
    const permRes = await getRolePermissions(row.id)
    const existingPerms = permRes.data || []
    nextTick(() => {
      treeRef.value?.setCheckedKeys(existingPerms)
    })
  } catch (error) {
    console.error('加载权限失败:', error)
    permissionTreeData.value = fallbackTreeData
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateRole(formData.id, formData)
      } else {
        await createRole(formData)
      }
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('操作失败:', error)
      
      // 根据错误类型显示不同的提示信息
      const errorMsg = error.response?.data?.message || error.message || '操作失败'
      
      // 检查是否是角色名称重复等提示
      if (errorMsg.includes('角色名称') || errorMsg.includes('已存在')) {
        ElMessage.warning({
          message: errorMsg,
          duration: 3000
        })
      } else {
        ElMessage.error({
          message: errorMsg,
          duration: 3000
        })
      }
    } finally {
      submitLoading.value = false
    }
  })
}

const handleAssignPermissionSubmit = async () => {
  submitLoading.value = true
  try {
    const checkedKeys = treeRef.value?.getCheckedKeys() || []
    await assignPermissions(currentRoleId.value, checkedKeys)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    console.error('分配权限失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '分配权限失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.role-management {
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
</style>
