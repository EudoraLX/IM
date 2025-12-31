<template>
  <div class="lead-management">
    <div class="page-header">
      <h2>线索列表</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="姓名/电话搜索..."
          class="search-input"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="filterStatus" placeholder="全部状态" class="status-filter" @change="handleSearch">
          <el-option label="全部状态" value="" />
          <el-option label="新线索" value="新线索" />
          <el-option label="跟进中" value="跟进中" />
          <el-option label="已转化" value="已转化" />
          <el-option label="已失效" value="已失效" />
        </el-select>
      </div>
    </div>

    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增线索
      </el-button>
      <el-button @click="handleImport">
        <el-icon><Upload /></el-icon>
        批量导入
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon>
        导出数据
      </el-button>
      <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
      <el-button type="success" @click="handleCreateTestData" style="margin-left: 10px">
        <el-icon><MagicStick /></el-icon>
        创建测试数据
      </el-button>
    </div>

    <el-table
      :data="tableData"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      class="lead-table"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="leadNo" label="线索编号" width="150" />
      <el-table-column prop="customerName" label="客户姓名" width="120" />
      <el-table-column prop="contactPhone" label="联系电话" width="150">
        <template #default="{ row }">
          {{ maskPhone(row.contactPhone) }}
        </template>
      </el-table-column>
      <el-table-column prop="sourceChannel" label="来源渠道" width="150" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">
            <el-icon><View /></el-icon>
          </el-button>
          <el-button link type="primary" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="formData.customerName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="formData.contactPhone" />
        </el-form-item>
        <el-form-item label="来源渠道" prop="sourceChannel">
          <el-select v-model="formData.sourceChannel" placeholder="请选择">
            <el-option label="官网预约" value="官网预约" />
            <el-option label="抖音推广" value="抖音推广" />
            <el-option label="线下活动" value="线下活动" />
            <el-option label="熟人转介绍" value="熟人转介绍" />
            <el-option label="搜索引擎" value="搜索引擎" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择">
            <el-option label="新线索" value="新线索" />
            <el-option label="跟进中" value="跟进中" />
            <el-option label="已转化" value="已转化" />
            <el-option label="已失效" value="已失效" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLeadList, addLead, updateLead, deleteLead, batchDeleteLeads } from '../api/lead'
import { setupTestData } from '../api/test'

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增线索')
const formRef = ref(null)
const isEdit = ref(false)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  customerName: '',
  contactPhone: '',
  sourceChannel: '',
  status: '新线索'
})

const rules = {
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getLeadList({
      current: pagination.current,
      size: pagination.size,
      keyword: searchKeyword.value,
      status: filterStatus.value
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增线索'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑线索'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info('查看功能待实现')
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条线索吗？', '提示', {
      type: 'warning'
    })
    await deleteLead(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的线索')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条线索吗？`, '提示', {
      type: 'warning'
    })
    await batchDeleteLeads(selectedIds.value)
    ElMessage.success('删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleImport = () => {
  ElMessage.info('批量导入功能待实现')
}

const handleExport = () => {
  ElMessage.info('导出功能待实现')
}

const handleCreateTestData = async () => {
  try {
    await ElMessageBox.confirm(
      '将创建20条测试线索（10条高潜 + 10条普通），并自动执行数据加工筛选高潜线索。是否继续？',
      '创建测试数据',
      {
        type: 'info',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    const loadingMessage = ElMessage({
      message: '正在创建测试数据...',
      type: 'info',
      duration: 0
    })
    
    try {
      const res = await setupTestData(20)
      loadingMessage.close()
      if (res.code === 200) {
        ElMessage.success(res.message || '测试数据创建成功！')
        // 刷新列表
        loadData()
        // 提示查看高潜线索
        setTimeout(() => {
          ElMessage.info('请在"高潜线索营销"页面查看筛选出的高潜线索')
        }, 1000)
      }
    } catch (error) {
      loadingMessage.close()
      throw error
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('创建测试数据失败: ' + (error.message || '未知错误'))
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateLead(formData)
          ElMessage.success('更新成功')
        } else {
          await addLead(formData)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '新增失败')
      }
    }
  })
}

const handleDialogClose = () => {
  resetForm()
}

const resetForm = () => {
  formData.id = null
  formData.customerName = ''
  formData.contactPhone = ''
  formData.sourceChannel = ''
  formData.status = '新线索'
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const getStatusType = (status) => {
  const typeMap = {
    '新线索': '',
    '跟进中': 'warning',
    '已转化': 'success',
    '已失效': 'danger'
  }
  return typeMap[status] || ''
}
</script>

<style scoped>
.lead-management {
  background: #ffffff;
  padding: 20px;
  border-radius: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  color: #333333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-input {
  width: 250px;
}

.status-filter {
  width: 150px;
}

.action-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.lead-table {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}
</style>

