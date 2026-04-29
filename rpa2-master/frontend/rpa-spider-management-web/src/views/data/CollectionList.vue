<template>
  <div class="collection-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据采集列表</span>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="纳税人识别号">
          <el-input v-model="searchForm.taxNo" placeholder="请输入纳税人识别号" clearable />
        </el-form-item>
        
        <el-form-item label="企业名称">
          <el-input v-model="searchForm.enterpriseName" placeholder="请输入企业名称" clearable />
        </el-form-item>
        
        <el-form-item label="采集状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
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
        <el-table-column prop="id" label="采集 ID" width="80" />
        <el-table-column prop="taskId" label="任务 ID" width="80" />
        <el-table-column prop="sourceUrl" label="数据来源" show-overflow-tooltip />
        <el-table-column prop="collectStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getCollectStatusType(row.collectStatus)" size="small">
              {{ getCollectStatusLabel(row.collectStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="采集时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getCollectionList, deleteCollection, getCollectionById } from '@/api/data'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  taxNo: '',
  enterpriseName: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      taxNo: searchForm.taxNo,
      enterpriseName: searchForm.enterpriseName,
      collectStatus: searchForm.status === 'success' ? 2 : (searchForm.status === 'failed' ? 3 : null)
    }

    const res = await getCollectionList(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载数据采集列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.taxNo = ''
  searchForm.enterpriseName = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

const handleView = async (row) => {
  try {
    const res = await getCollectionById(row.id)
    const data = res.data
    
    ElMessageBox.alert(
      `任务编码：${data.taskCode || '-'}\n` +
      `数据来源：${data.sourceUrl || '-'}\n` +
      `采集状态：${getCollectStatusLabel(data.collectStatus)}\n` +
      `采集时间：${data.createTime || '-'}\n` +
      (data.errorMessage ? `\n错误信息：${data.errorMessage}` : ''),
      '采集记录详情',
      {
        confirmButtonText: '关闭',
        customStyle: {
          whiteSpace: 'pre-line'
        }
      }
    )
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该采集记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCollection(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

const getCollectStatusLabel = (status) => {
  const map = {
    0: '待采集',
    1: '采集中',
    2: '采集成功',
    3: '采集失败'
  }
  return map[status] || '未知'
}

const getCollectStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.collection-list {
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
