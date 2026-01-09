<template>
  <div class="activity-record">
    <div class="page-header">
      <h2>活跃次数记录</h2>
      <p class="subtitle">根据新线索唯一编号记录活跃次数，系统将自动判断并转成高潜用户</p>
    </div>

    <div class="record-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>记录活跃次数</span>
          </div>
        </template>
        <el-form :model="recordForm" label-width="120px" :rules="recordRules" ref="recordFormRef">
          <el-form-item label="线索编号" prop="leadNo">
            <el-input
              v-model="recordForm.leadNo"
              placeholder="请输入线索编号（如：L1234567890）"
              clearable
              style="width: 300px"
              @blur="handleLeadNoBlur"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearchLead" style="margin-left: 10px">
              查询线索
            </el-button>
          </el-form-item>
          
          <el-form-item v-if="leadInfo" label="线索信息">
            <div class="lead-info">
              <span>客户姓名：{{ leadInfo.customerName }}</span>
              <span>联系电话：{{ maskPhone(leadInfo.contactPhone) }}</span>
              <span>来源渠道：{{ leadInfo.sourceChannel || '-' }}</span>
              <span>当前状态：{{ leadInfo.status }}</span>
            </div>
          </el-form-item>

          <el-form-item label="活跃类型" prop="activityType">
            <el-select v-model="recordForm.activityType" placeholder="请选择活跃类型" style="width: 300px">
              <el-option label="测试" value="测试" />
              <el-option label="人脸验证" value="人脸验证" />
              <el-option label="OCR验证" value="OCR验证" />
              <el-option label="登录" value="登录" />
              <el-option label="数据调用" value="数据调用" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>

          <el-form-item label="活跃机构" prop="organization">
            <el-select
              v-model="recordForm.organization"
              placeholder="请选择或输入活跃机构"
              filterable
              allow-create
              default-first-option
              style="width: 300px"
            >
              <el-option
                v-for="org in organizationOptions"
                :key="org"
                :label="org"
                :value="org"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="活跃时间" prop="activityTime">
            <el-date-picker
              v-model="recordForm.activityTime"
              type="datetime"
              placeholder="选择活跃时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 300px"
            />
            <el-button
              type="text"
              @click="recordForm.activityTime = getCurrentDateTime()"
              style="margin-left: 10px"
            >
              使用当前时间
            </el-button>
          </el-form-item>

          <el-form-item label="备注">
            <el-input
              v-model="recordForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注信息（可选）"
              style="width: 500px"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleRecord" :loading="recording">
              记录活跃次数
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <div class="history-section" v-if="currentLeadNo">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>活跃记录历史（线索编号：{{ currentLeadNo }}）</span>
            <div>
              <span class="activity-count">总活跃次数：<strong>{{ totalActivityCount }}</strong></span>
              <el-button type="primary" size="small" @click="loadHistory" style="margin-left: 10px">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
        </template>
        <el-table :data="historyData" v-loading="historyLoading" style="width: 100%">
          <el-table-column prop="activityType" label="活跃类型" width="120" />
          <el-table-column prop="organization" label="活跃机构" width="150" />
          <el-table-column prop="activityTime" label="活跃时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.activityTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" />
          <el-table-column prop="createTime" label="记录时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination" v-if="historyPagination.total > 0">
          <el-pagination
            v-model:current-page="historyPagination.current"
            v-model:page-size="historyPagination.size"
            :total="historyPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleHistorySizeChange"
            @current-change="handleHistoryCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { recordActivity, getActivityRecords, getActivityCount } from '../api/activityRecord'
import { getLeadByLeadNo } from '../api/lead'
import { getOrganizations } from '../api/monitor'

const recordFormRef = ref(null)
const recording = ref(false)
const leadInfo = ref(null)
const currentLeadNo = ref('')
const historyData = ref([])
const historyLoading = ref(false)
const totalActivityCount = ref(0)
const organizationOptions = ref([])

const recordForm = reactive({
  leadNo: '',
  activityType: '测试',
  organization: '',
  activityTime: '',
  remark: ''
})

const recordRules = {
  leadNo: [{ required: true, message: '请输入线索编号', trigger: 'blur' }],
  activityType: [{ required: true, message: '请选择活跃类型', trigger: 'change' }]
}

const historyPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

onMounted(async () => {
  await loadOrganizations()
  // 设置默认活跃时间为当前时间
  recordForm.activityTime = getCurrentDateTime()
})

const loadOrganizations = async () => {
  try {
    const res = await getOrganizations()
    if (res.code === 200 && res.data) {
      organizationOptions.value = res.data
    } else {
      organizationOptions.value = [
        '华东中心机构',
        '上海研发分部',
        '北京总部',
        '深圳分公司',
        '广州分公司'
      ]
    }
  } catch (error) {
    console.error('加载机构列表失败', error)
    organizationOptions.value = [
      '华东中心机构',
      '上海研发分部',
      '北京总部',
      '深圳分公司',
      '广州分公司'
    ]
  }
}

const handleLeadNoBlur = async () => {
  if (recordForm.leadNo) {
    await handleSearchLead()
  }
}

const handleSearchLead = async () => {
  if (!recordForm.leadNo || recordForm.leadNo.trim() === '') {
    ElMessage.warning('请输入线索编号')
    return
  }

  try {
    const res = await getLeadByLeadNo(recordForm.leadNo.trim())
    
    if (res.code === 200 && res.data) {
      // 查询成功，显示线索信息
      leadInfo.value = res.data
      currentLeadNo.value = res.data.leadNo
      ElMessage.success('查询成功')
      // 加载历史记录
      await loadHistory()
      // 加载活跃次数
      await loadActivityCount()
    } else if (res.code === 404) {
      // 线索不存在，这是正常的业务情况
      ElMessage.warning(res.message || '未找到该线索编号')
      leadInfo.value = null
      currentLeadNo.value = ''
    } else {
      // 其他错误
      ElMessage.error(res.message || '查询失败')
      leadInfo.value = null
      currentLeadNo.value = ''
    }
  } catch (error) {
    // 网络错误或服务器错误
    ElMessage.error('查询线索失败: ' + (error.message || '未知错误'))
    leadInfo.value = null
    currentLeadNo.value = ''
  }
}

const loadActivityCount = async () => {
  if (!currentLeadNo.value) {
    totalActivityCount.value = 0
    return
  }
  
  try {
    const res = await getActivityCount(currentLeadNo.value)
    if (res && res.code === 200) {
      totalActivityCount.value = res.data || 0
    } else {
      totalActivityCount.value = 0
    }
  } catch (error) {
    // 静默处理，不显示错误（记录为0是正常情况）
    console.warn('获取活跃次数失败', error)
    totalActivityCount.value = 0
  }
}

const loadHistory = async () => {
  if (!currentLeadNo.value) {
    historyData.value = []
    historyPagination.total = 0
    return
  }
  
  historyLoading.value = true
  try {
    const res = await getActivityRecords({
      leadNo: currentLeadNo.value,
      current: historyPagination.current,
      size: historyPagination.size
    })
    
    // 无论记录数量多少，只要 code=200 就是成功
    if (res && res.code === 200) {
      // 成功时，即使记录为0也是正常的，不显示任何错误
      historyData.value = (res.data && res.data.records) ? res.data.records : []
      historyPagination.total = (res.data && res.data.total) ? res.data.total : 0
      // 静默处理，不显示任何消息（记录为0是正常情况）
    } else {
      // 业务错误（如404等），也不显示错误，只记录日志
      console.warn('加载历史记录返回非200状态:', res)
      historyData.value = []
      historyPagination.total = 0
    }
  } catch (error) {
    // 只有真正的网络错误或服务器错误才显示错误
    // 但这里不应该进入，因为响应拦截器已经处理了
    console.error('加载历史记录异常:', error)
    // 不显示错误消息，避免干扰用户（记录为0时不应该显示错误）
    historyData.value = []
    historyPagination.total = 0
  } finally {
    historyLoading.value = false
  }
}

const handleRecord = async () => {
  if (!recordFormRef.value) return
  
  await recordFormRef.value.validate(async (valid) => {
    if (valid) {
      if (!leadInfo.value) {
        ElMessage.warning('请先查询并确认线索信息')
        return
      }
      
      recording.value = true
      try {
        const data = {
          leadNo: recordForm.leadNo.trim(),
          activityType: recordForm.activityType,
          organization: recordForm.organization || null,
          activityTime: recordForm.activityTime || getCurrentDateTime(),
          remark: recordForm.remark || null
        }
        
        const res = await recordActivity(data)
        if (res.code === 200) {
          ElMessage.success('活跃次数记录成功！')
          // 刷新历史记录和活跃次数
          await loadHistory()
          await loadActivityCount()
          // 重置表单（保留线索编号）
          handleReset(true)
        } else {
          ElMessage.error(res.message || '记录失败')
        }
      } catch (error) {
        ElMessage.error('记录失败: ' + (error.message || '未知错误'))
      } finally {
        recording.value = false
      }
    }
  })
}

const handleReset = (keepLeadNo = false) => {
  if (!keepLeadNo) {
    recordForm.leadNo = ''
    leadInfo.value = null
    currentLeadNo.value = ''
    historyData.value = []
    totalActivityCount.value = 0
  }
  recordForm.activityType = '测试'
  recordForm.organization = ''
  recordForm.activityTime = getCurrentDateTime()
  recordForm.remark = ''
  if (recordFormRef.value) {
    recordFormRef.value.clearValidate()
  }
}

const handleHistorySizeChange = () => {
  loadHistory()
}

const handleHistoryCurrentChange = () => {
  loadHistory()
}

const getCurrentDateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  try {
    const date = new Date(dateTime)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    return dateTime
  }
}

const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}
</script>

<style scoped>
.activity-record {
  background: #ffffff;
  padding: 20px;
  border-radius: 4px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  color: #333333;
  margin-bottom: 10px;
}

.subtitle {
  color: #666666;
  font-size: 14px;
}

.record-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.lead-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.lead-info span {
  font-size: 14px;
  color: #606266;
}

.history-section {
  margin-top: 20px;
}

.activity-count {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}

.activity-count strong {
  color: #409eff;
  font-size: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

