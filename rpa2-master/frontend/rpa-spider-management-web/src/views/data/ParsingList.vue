<template>
  <div class="parsing-list">
    <el-card>
      <template #header>
        <span>数据解析列表</span>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="任务编码">
          <el-input v-model="searchForm.taskCode" placeholder="请输入任务编码" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
      
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="id" label="解析 ID" width="80" />
        <el-table-column prop="collectionId" label="采集 ID" width="80" />
        <el-table-column prop="parsedData" label="解析数据" show-overflow-tooltip min-width="200" />
        <el-table-column prop="parseStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getParseStatusType(row.parseStatus)" size="small">
              {{ getParseStatusLabel(row.parseStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="解析时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
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
import { getParsedList, getParsedById } from '@/api/data'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ taskCode: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
      // 移除 taskCode 参数 - 后端接收的是 taskId (Long)，不是 taskCode (String)
    }
    
    const res = await getParsedList(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

const handleView = async (row) => {
  try {
    const res = await getParsedById(row.id)
    const data = res.data
    
    ElMessageBox.alert(
      `解析 ID: ${data.id || '-'}\n` +
      `采集 ID: ${data.collectionId || '-'}\n` +
      `解析状态：${getParseStatusLabel(data.parseStatus)}\n` +
      `解析时间：${data.createTime || '-'}\n` +
      (data.errorMessage ? `\n错误信息：${data.errorMessage}` : '') +
      `\n\n解析数据:\n${data.parsedData || '无'}`,
      '解析记录详情',
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

const getParseStatusLabel = (status) => {
  const map = {
    0: '待解析',
    1: '解析中',
    2: '解析成功',
    3: '解析失败'
  }
  return map[status] || '未知'
}

const getParseStatusType = (status) => {
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
.parsing-list {
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
