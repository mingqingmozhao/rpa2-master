<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建用户
          </el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleId" placeholder="请选择角色" clearable>
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
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
        border
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="roleName" label="角色" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button 
              link 
              :type="row.status === 1 ? 'danger' : 'success'" 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="formData.roleId" placeholder="请选择角色" style="width: 100%;">
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="重置密码"
      width="400px"
    >
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="80px">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="passwordForm.password" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPasswordSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, resetPassword, updateUserStatus, assignUserRole, getRoleList } from '@/api/user'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const isEdit = ref(false)
const currentUserId = ref(null)
const roleOptions = ref([])

const searchForm = reactive({
  username: '',
  realName: '',
  roleId: null,
  status: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  roleId: null,
  remark: ''
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur', validator: (rule, value, callback) => {
        if (!isEdit.value && !value) {
          callback(new Error('请输入密码'))
        } else {
          callback()
        }
      }
    },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const passwordForm = reactive({
  password: ''
})

const passwordRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
}

const formRef = ref(null)
const passwordFormRef = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      realName: searchForm.realName || undefined,
      roleId: searchForm.roleId || undefined,
      status: searchForm.status ?? undefined
    }

    const res = await getUserList(params)
    tableData.value = res.data.records || res.data?.content || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadRoleOptions = async () => {
  try {
    const res = await getRoleList({ page: 1, pageSize: 100 })
    const roles = res.data?.records || res.data?.content || res.data || []
    roleOptions.value = roles
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.roleId = null
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
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    roleId: null,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.userId,
    username: row.username,
    password: '',  // 编辑时不显示密码
    realName: row.realName,
    email: row.email,
    phone: row.phone,
    roleId: row.roleId,
    remark: row.remark
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessageBox.alert(
    `<div style="line-height: 2;">
      <p><strong>用户名：</strong>${row.username}</p>
      <p><strong>姓名：</strong>${row.realName}</p>
      <p><strong>邮箱：</strong>${row.email || '未设置'}</p>
      <p><strong>手机号：</strong>${row.phone || '未设置'}</p>
      <p><strong>角色：</strong>${row.roleName || '未设置'}</p>
      <p><strong>状态：</strong>${row.status === 1 ? '启用' : '禁用'}</p>
      <p><strong>创建时间：</strong>${row.createTime}</p>
    </div>`,
    '用户详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  )
}

const handleToggleStatus = (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确认${action}该用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateUserStatus(row.userId, row.status === 1 ? 0 : 1)
      ElMessage.success(`${action}成功`)
      loadData()
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该用户吗？此操作将永久删除，无法恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.userId)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

const handleResetPassword = (row) => {
  currentUserId.value = row.userId
  passwordForm.password = ''
  passwordDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEdit.value) {
        const data = { ...formData }
        delete data.id
        delete data.password
        await updateUser(formData.id, data)
        // 角色变更单独处理
        await assignUserRole(formData.id, formData.roleId)
      } else {
        await createUser(formData)
      }
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('操作失败:', error)
      
      // 根据错误类型显示不同的提示信息
      const errorMsg = error.response?.data?.message || error.message || '操作失败'
      
      // 检查是否是用户名重复、手机号重复等提示
      if (errorMsg.includes('用户名') || errorMsg.includes('已存在')) {
        ElMessage.warning({
          message: errorMsg,
          duration: 3000
        })
      } else if (errorMsg.includes('手机号')) {
        ElMessage.warning({
          message: errorMsg,
          duration: 3000
        })
      } else if (errorMsg.includes('邮箱')) {
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

const handleResetPasswordSubmit = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      await resetPassword(currentUserId.value, passwordForm.password)
      ElMessage.success('密码重置成功')
      passwordDialogVisible.value = false
    } catch (error) {
      console.error('重置密码失败:', error)
      
      // 根据错误类型显示不同的提示信息
      const errorMsg = error.response?.data?.message || error.message || '重置密码失败'
      
      if (errorMsg.includes('密码') || errorMsg.includes('长度')) {
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

onMounted(() => {
  loadData()
  loadRoleOptions()
})
</script>

<style scoped lang="scss">
.user-management {
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
