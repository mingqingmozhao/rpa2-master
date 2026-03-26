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
        <el-table-column prop="taskCode" label="任务编码" />
        <el-table-column prop="taxNo" label="纳税人识别号" />
        <el-table-column prop="enterpriseName" label="企业名称" />
        <el-table-column prop="category" label="分类" />
        <el-table-column prop="dataSource" label="数据来源" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collectionTime" label="采集时间" />
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
      status: searchForm.status
    }
    
    // TODO: 调用 API
    // const res = await getCollectionList(params)
    // tableData.value = res.data.records || []
    // pagination.total = res.data.total || 0
    
    // 临时测试数据
    tableData.value = [
      {
        id: 1,
        taskCode: 'TASK_001',
        taxNo: '91110000123456789X',
        enterpriseName: '某某科技有限公司',
        category: 'A',
        dataSource: '税务局官网',
        status: 'success',
        collectionTime: '2026-03-20 10:00:00'
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

const handleView = (row) => {
  ElMessage.info('查看详情功能待实现')
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该采集记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // TODO: 调用删除 API
      // await deleteCollection(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
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
  }
}
</style>
