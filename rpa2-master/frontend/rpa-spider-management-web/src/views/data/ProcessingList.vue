<template>
  <div class="processing-list">
    <el-card>
      <template #header>
        <span>数据加工列表</span>
      </template>
      
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="id" label="加工 ID" width="80" />
        <el-table-column prop="parsedId" label="解析 ID" width="80" />
        <el-table-column prop="processedData" label="加工数据" show-overflow-tooltip min-width="200" />
        <el-table-column prop="processStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getProcessStatusType(row.processStatus)" size="small">
              {{ getProcessStatusLabel(row.processStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="加工时间" width="170" />
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
import { getProcessedList, getProcessedById } from '@/api/data'

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
    
    const res = await getProcessedList(params)
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
    const res = await getProcessedById(row.id)
    const data = res.data
    
    ElMessageBox.alert(
      `加工 ID: ${data.id || '-'}\n` +
      `解析 ID: ${data.parsedId || '-'}\n` +
      `加工状态：${getProcessStatusLabel(data.processStatus)}\n` +
      `加工时间：${data.createTime || '-'}\n` +
      (data.errorMessage ? `\n错误信息：${data.errorMessage}` : '') +
      `\n\n加工数据:\n${data.processedData || '无'}`,
      '加工记录详情',
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

const getProcessStatusLabel = (status) => {
  const map = {
    0: '待加工',
    1: '加工中',
    2: '加工成功',
    3: '加工失败'
  }
  return map[status] || '未知'
}

const getProcessStatusType = (status) => {
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
.processing-list {
}
</style>
