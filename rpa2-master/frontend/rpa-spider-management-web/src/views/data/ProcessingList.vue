<template>
  <div class="processing-list">
    <el-card>
      <template #header>
        <span>数据加工列表</span>
      </template>
      
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="taskCode" label="任务编码" />
        <el-table-column prop="processedData" label="加工数据" show-overflow-tooltip />
        <el-table-column prop="validationResult" label="验证结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.validationResult === 'passed' ? 'success' : 'danger'" size="small">
              {{ row.validationResult === 'passed' ? '通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="processingTime" label="加工时间" />
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
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const loadData = () => {
  tableData.value = [
    { id: 1, taskCode: 'TASK_001', processedData: '{"result": "ok"}', validationResult: 'passed', status: 'processed', processingTime: '2026-03-20 10:00:00' }
  ]
}

onMounted(() => { loadData() })
</script>

<style scoped lang="scss">
.processing-list {
}
</style>
