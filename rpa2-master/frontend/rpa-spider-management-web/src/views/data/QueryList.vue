<template>
  <div class="query-list">
    <el-card>
      <template #header>
        <span>数据查询列表</span>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="纳税人识别号">
          <el-input v-model="searchForm.taxNo" placeholder="请输入纳税人识别号" clearable />
        </el-form-item>
        <el-form-item label="企业名称">
          <el-input v-model="searchForm.enterpriseName" placeholder="请输入企业名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="taskCode" label="任务编码" />
        <el-table-column prop="taxNo" label="纳税人识别号" />
        <el-table-column prop="enterpriseName" label="企业名称" />
        <el-table-column prop="categoryId" label="分类 ID" />
        <el-table-column prop="dataStatus" label="数据状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.dataStatus === 'available' ? 'success' : 'danger'" size="small">
              {{ row.dataStatus === 'available' ? '可用' : '不可用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="queryData" label="查询数据" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" />
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ taxNo: '', enterpriseName: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const loadData = () => {
  tableData.value = [
    { id: 1, taskCode: 'TASK_001', taxNo: '91110000123456789X', enterpriseName: '某某公司', categoryId: 'CAT_001', dataStatus: 'available', queryData: '{"query": "test"}', createTime: '2026-03-20 10:00:00' }
  ]
}

const handleSearch = () => { loadData() }
const handleReset = () => { searchForm.taxNo = ''; searchForm.enterpriseName = ''; loadData() }

onMounted(() => { loadData() })
</script>

<style scoped lang="scss">
.query-list {
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
