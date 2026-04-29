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
        <el-table-column prop="id" label="发票 ID" width="80" />
        <el-table-column prop="invoiceCode" label="发票代码" width="120" />
        <el-table-column prop="invoiceNo" label="发票号码" width="120" />
        <el-table-column prop="invoiceTitle" label="发票标题" show-overflow-tooltip />
        <el-table-column prop="invoiceAmount" label="发票金额" width="100" />
        <el-table-column prop="invoiceDate" label="开票日期" width="120" />
        <el-table-column prop="buyerName" label="购买方" width="150" />
        <el-table-column prop="sellerName" label="销售方" width="150" />
        <el-table-column prop="createTime" label="入库时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
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
import { ElMessage } from 'element-plus'
import { getInvoiceList } from '@/api/data'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ 
  taxNo: '', 
  enterpriseName: '',
  invoiceCode: '',
  invoiceNo: ''
})
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      taxNo: searchForm.taxNo,
      enterpriseName: searchForm.enterpriseName,
      invoiceCode: searchForm.invoiceCode,
      invoiceNo: searchForm.invoiceNo
    }
    
    const res = await getInvoiceList(params)
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

const handleReset = () => {
  searchForm.taxNo = ''
  searchForm.enterpriseName = ''
  searchForm.invoiceCode = ''
  searchForm.invoiceNo = ''
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
    const res = await getInvoiceById(row.id)
    const data = res.data
    
    ElMessageBox.alert(
      `发票代码：${data.invoiceCode || '-'}\n` +
      `发票号码：${data.invoiceNo || '-'}\n` +
      `发票标题：${data.invoiceTitle || '-'}\n` +
      `发票金额：${data.invoiceAmount || '-'}\n` +
      `开票日期：${data.invoiceDate || '-'}\n` +
      `校验码：${data.checkCode || '-'}\n` +
      `机器编号：${data.machineNo || '-'}\n` +
      `购买方名称：${data.buyerName || '-'}\n` +
      `购买方税号：${data.buyerTaxNo || '-'}\n` +
      `销售方名称：${data.sellerName || '-'}\n` +
      `销售方税号：${data.sellerTaxNo || '-'}\n` +
      `备注：${data.remark || '-'}`,
      '发票详情',
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

const getInvoiceById = async (id) => {
  return request({
    url: `/data/invoice/${id}`,
    method: 'get'
  })
}

const request = (config) => {
  return import('@/utils/request').then(module => module.default(config))
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.query-list {
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
