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
        <el-table-column prop="taskCode" label="任务编码" />
        <el-table-column prop="parsedData" label="解析数据" show-overflow-tooltip />
        <el-table-column prop="fieldCount" label="字段数" width="80" />
        <el-table-column prop="parsingRule" label="解析规则" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'parsed' ? 'success' : 'warning'" size="small">
              {{ row.status === 'parsed' ? '已解析' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parsingTime" label="解析时间" />
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

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ taskCode: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const loadData = () => {
  // TODO: 实现 API 调用
  tableData.value = [
    { id: 1, taskCode: 'TASK_001', parsedData: '{"data": "test"}', fieldCount: 5, parsingRule: 'rule1', status: 'parsed', parsingTime: '2026-03-20 10:00:00' }
  ]
}

const handleSearch = () => { loadData() }
const handleSizeChange = () => { loadData() }
const handleCurrentChange = () => { loadData() }

onMounted(() => { loadData() })
</script>

<style scoped lang="scss">
.parsing-list {
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
