<template>
  <div class="high-potential-lead">
    <div class="page-header">
      <div>
        <h2>高潜线索营销</h2>
        <p class="subtitle">基于自动化规则筛选的高价值潜在客户线索集中处理中心</p>
      </div>
      <div class="header-info">
        <span>当前日期: {{ currentDate }}</span>
        <el-avatar :size="32" />
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-value">{{ statistics.totalMatched || 125 }}</div>
            <div class="stat-label">命中用户总数</div>
            <div class="stat-trend">
              <el-icon><ArrowUp /></el-icon>
              <span>较昨日 +4.2%</span>
            </div>
          </div>
          <el-icon class="stat-icon" :size="40"><User /></el-icon>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-value">{{ statistics.todayNew || 8 }}</div>
            <div class="stat-label">今日新增线索</div>
            <div class="stat-desc">实时更新中</div>
          </div>
          <el-icon class="stat-icon" :size="40"><Plus /></el-icon>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-value">{{ statistics.pendingCount || 42 }}</div>
            <div class="stat-label">待营销处理</div>
            <div class="stat-desc">占总量约 33.6%</div>
          </div>
          <el-icon class="stat-icon orange" :size="40"><Clock /></el-icon>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-value">{{ statistics.conversionRate || 18.5 }}%</div>
            <div class="stat-label">营销转化成功率</div>
            <div class="stat-desc">本月累计数据</div>
          </div>
          <el-icon class="stat-icon purple" :size="40"><Trophy /></el-icon>
        </div>
      </el-card>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" size="large" @click="handleBatchCall">
        <el-icon><Phone /></el-icon>
        批量外呼营销
      </el-button>
      <el-button type="primary" size="large" @click="handleBatchSms">
        <el-icon><Message /></el-icon>
        批量发送短信
      </el-button>
      <div class="search-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户ID/姓名"
          class="search-input"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button @click="handleFilter">
          <el-icon><Filter /></el-icon>
          筛选
        </el-button>
      </div>
    </div>

    <!-- 线索列表 -->
    <el-table
      :data="tableData"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      class="lead-table"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="userId" label="用户ID/姓名" width="150">
        <template #default="{ row }">
          {{ row.userId || row.customerName }}
        </template>
      </el-table-column>
      <el-table-column prop="matchingRule" label="命中规则" width="180" />
      <el-table-column prop="leadSourceSystem" label="线索来源系统" width="200" />
      <el-table-column prop="matchingDate" label="命中日期" width="180">
        <template #default="{ row }">
          {{ formatDate(row.matchingDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="150">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleContact(row)">
            {{ getActionText(row.status) }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getHighPotentialLeadList,
  getStatistics,
  batchCall,
  batchSendSms,
  updateLeadStatus
} from '../api/highPotential'

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')

const statistics = reactive({
  totalMatched: 0,
  todayNew: 0,
  pendingCount: 0,
  conversionRate: 0
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const currentDate = ref(new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric'
}))

onMounted(() => {
  loadStatistics()
  loadData()
})

const loadStatistics = async () => {
  try {
    const res = await getStatistics()
    if (res.code === 200 && res.data) {
      Object.assign(statistics, res.data)
    }
  } catch (error) {
    ElMessage.error('加载统计信息失败')
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getHighPotentialLeadList({
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

const handleFilter = () => {
  ElMessage.info('筛选功能待实现')
}

const handleBatchCall = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要外呼的线索')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要对选中的 ${selectedIds.value.length} 条线索进行外呼吗？`, '提示', {
      type: 'warning'
    })
    await batchCall(selectedIds.value)
    ElMessage.success('外呼任务已提交')
    selectedIds.value = []
    loadData()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('外呼失败')
    }
  }
}

const handleBatchSms = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要发送短信的线索')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要对选中的 ${selectedIds.value.length} 条线索发送短信吗？`, '提示', {
      type: 'warning'
    })
    await batchSendSms(selectedIds.value)
    ElMessage.success('短信发送任务已提交')
    selectedIds.value = []
    loadData()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发送失败')
    }
  }
}

const handleContact = async (row) => {
  if (row.status === '待营销处理') {
    await updateLeadStatus(row.id, '营销进行中')
    ElMessage.success('状态已更新')
    loadData()
  } else {
    ElMessage.info('该线索已处理')
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

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusType = (status) => {
  const typeMap = {
    '待营销处理': '',
    '营销进行中': 'warning',
    '营销已转化': 'success',
    '已联系失效': 'danger'
  }
  return typeMap[status] || ''
}

const getActionText = (status) => {
  const textMap = {
    '待营销处理': '单独联系',
    '营销进行中': '跟进进度',
    '营销已转化': '已完成',
    '已联系失效': '再次激活'
  }
  return textMap[status] || '查看'
}
</script>

<style scoped>
.high-potential-lead {
  background: #f5f5f5;
}

.page-header {
  background: #ffffff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  font-size: 20px;
  color: #333333;
  margin-bottom: 5px;
}

.subtitle {
  color: #666666;
  font-size: 14px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #666666;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 4px;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666666;
  margin-bottom: 5px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #67c23a;
}

.stat-desc {
  font-size: 12px;
  color: #999999;
}

.stat-icon {
  color: #409eff;
}

.stat-icon.orange {
  color: #e6a23c;
}

.stat-icon.purple {
  color: #9c27b0;
}

.action-buttons {
  background: #ffffff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-actions {
  margin-left: auto;
  display: flex;
  gap: 10px;
}

.search-input {
  width: 250px;
}

.lead-table {
  background: #ffffff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.pagination {
  background: #ffffff;
  padding: 20px;
  border-radius: 4px;
  display: flex;
  justify-content: flex-end;
}
</style>

