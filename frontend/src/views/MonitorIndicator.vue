<template>
  <div class="monitor-indicator">
    <div class="page-header">
      <h2>监控条件配置</h2>
      <p class="subtitle">设置后系统将根据指定的活跃度指标自动筛选高价值线索</p>
    </div>

    <el-form :model="formData" label-width="200px" class="config-form">
      <!-- 监控时间范围 -->
      <el-form-item label="监控时间范围间隔">
        <div class="form-item-content">
          <el-input-number
            v-model="formData.timeRangeDays"
            :min="1"
            :max="365"
            controls-position="right"
          />
          <span class="unit">天</span>
          <p class="description">线索在指定天数内的行为将被纳入监控统计</p>
        </div>
      </el-form-item>

      <!-- 活跃机构选择 -->
      <el-form-item label="活跃机构选择">
        <div class="form-item-content">
          <div class="org-select-wrapper">
            <el-select
              v-model="selectedOrganizations"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入活跃机构"
              style="width: 100%"
              @change="handleOrganizationChange"
            >
              <el-option
                v-for="org in organizationOptions"
                :key="org.value"
                :label="org.label"
                :value="org.value"
              />
            </el-select>
            <el-button type="primary" @click="showOrgManageDialog = true" style="margin-left: 10px">
              <el-icon><Setting /></el-icon>
              管理机构
            </el-button>
          </div>
          <p class="description">仅监控所选机构下属产生的交互线索。可以输入新的机构名称</p>
        </div>
      </el-form-item>

      <!-- 活跃次数阈值 -->
      <el-form-item label="活跃次数阈值">
        <div class="form-item-content">
          <el-input-number
            v-model="formData.activityThreshold"
            :min="1"
            :max="100"
            controls-position="right"
          />
          <span class="unit">次</span>
          <p class="description">单个线索在上述时间范围内行为频率超过此数值则标记为"高活跃"</p>
        </div>
      </el-form-item>

      <!-- 实时监控图表 -->
      <el-form-item label="实时监控数据">
        <div class="chart-container">
          <div class="chart-header">
            <span class="chart-title">最近7天线索趋势</span>
            <el-button text type="primary" size="small" @click="loadChartData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          <div id="estimationChart" style="width: 100%; height: 350px;"></div>
          <div class="chart-footer">
            <span class="stat-item">总线索数: <strong>{{ statistics.totalLeads || 0 }}</strong></span>
            <span class="stat-item">预估高潜线索: <strong>{{ statistics.estimatedHighPotential || 0 }}</strong></span>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <div class="action-buttons">
      <el-button @click="handleCancel">取消配置</el-button>
      <el-button type="success" @click="handleRefreshMonitor" :loading="refreshing">
        <el-icon><Refresh /></el-icon>
        刷新监控
      </el-button>
      <el-button type="primary" @click="handleSave">提交保存</el-button>
    </div>

    <!-- 机构管理对话框 -->
    <el-dialog
      v-model="showOrgManageDialog"
      title="机构管理"
      width="600px"
      @close="handleOrgDialogClose"
    >
      <div class="org-manage-content">
        <div class="org-manage-header">
          <el-input
            v-model="orgSearchKeyword"
            placeholder="搜索机构..."
            clearable
            style="width: 200px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleAddOrg">
            <el-icon><Plus /></el-icon>
            新增机构
          </el-button>
        </div>
        
        <el-table :data="filteredOrganizationOptions" style="width: 100%; margin-top: 15px" max-height="400">
          <el-table-column prop="label" label="机构名称" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEditOrg(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button link type="danger" @click="handleDeleteOrg(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <el-button @click="showOrgManageDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleSaveOrganizations">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑机构对话框 -->
    <el-dialog
      v-model="showOrgEditDialog"
      :title="orgEditMode === 'add' ? '新增机构' : '编辑机构'"
      width="400px"
    >
      <el-form :model="orgEditForm" label-width="100px">
        <el-form-item label="机构名称" required>
          <el-input v-model="orgEditForm.name" placeholder="请输入机构名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showOrgEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveOrg">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMonitorConfig, saveMonitorConfig, getMonitorStatistics, getOrganizations, refreshMonitor } from '../api/monitor'
import * as echarts from 'echarts'
import { onBeforeUnmount } from 'vue'

const formData = reactive({
  id: null,
  timeRangeDays: 7,
  activeOrganizations: '[]',
  activityThreshold: 3,
  status: 1
})

const selectedOrganizations = ref(['华东中心机构', '上海研发分部'])
const statistics = ref({
  totalLeads: 0,
  estimatedHighPotential: 0
})

const refreshing = ref(false)

const organizationOptions = ref([
  { label: '华东中心机构', value: '华东中心机构' },
  { label: '上海研发分部', value: '上海研发分部' },
  { label: '北京总部', value: '北京总部' },
  { label: '深圳分公司', value: '深圳分公司' },
  { label: '广州分公司', value: '广州分公司' }
])

// 机构管理相关
const showOrgManageDialog = ref(false)
const showOrgEditDialog = ref(false)
const orgSearchKeyword = ref('')
const orgEditMode = ref('add') // 'add' 或 'edit'
const orgEditForm = reactive({
  name: '',
  index: -1
})

// 过滤后的机构列表（用于搜索）
const filteredOrganizationOptions = computed(() => {
  if (!orgSearchKeyword.value) {
    return organizationOptions.value
  }
  const keyword = orgSearchKeyword.value.toLowerCase()
  return organizationOptions.value.filter(org => 
    org.label.toLowerCase().includes(keyword) || 
    org.value.toLowerCase().includes(keyword)
  )
})

let chartInstance = null
let refreshTimer = null

onMounted(async () => {
  await loadConfig()
  await loadOrganizations()
  // 等待DOM渲染完成
  await nextTick()
  setTimeout(() => {
    initChart()
    // 初始化后立即加载数据
    loadChartData()
    // 每30秒自动刷新图表数据
    refreshTimer = setInterval(() => {
      loadChartData()
    }, 30000)
  }, 300)
})

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

// 监听配置变化，实时更新图表
watch(() => [formData.timeRangeDays, formData.activityThreshold, selectedOrganizations.value], () => {
  if (chartInstance) {
    loadChartData()
  }
}, { deep: true })

const loadConfig = async () => {
  try {
    const res = await getMonitorConfig()
    if (res.code === 200 && res.data) {
      Object.assign(formData, res.data)
      if (res.data.activeOrganizations) {
        try {
          selectedOrganizations.value = JSON.parse(res.data.activeOrganizations)
        } catch (e) {
          selectedOrganizations.value = []
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

const handleOrganizationChange = () => {
  formData.activeOrganizations = JSON.stringify(selectedOrganizations.value)
}

// 机构管理相关方法
const handleAddOrg = () => {
  orgEditMode.value = 'add'
  orgEditForm.name = ''
  orgEditForm.index = -1
  showOrgEditDialog.value = true
}

const handleEditOrg = (org) => {
  orgEditMode.value = 'edit'
  orgEditForm.name = org.label
  // 找到在原始列表中的索引
  const index = organizationOptions.value.findIndex(o => o.value === org.value)
  orgEditForm.index = index
  showOrgEditDialog.value = true
}

const handleDeleteOrg = async (org) => {
  try {
    await ElMessageBox.confirm('确定要删除这个机构吗？', '提示', {
      type: 'warning'
    })
    
    // 找到在原始列表中的索引
    const index = organizationOptions.value.findIndex(o => o.value === org.value)
    if (index === -1) {
      ElMessage.error('机构不存在')
      return
    }
    
    // 如果该机构已被选中，从选中列表中移除
    const selectedIndex = selectedOrganizations.value.indexOf(org.value)
    if (selectedIndex > -1) {
      selectedOrganizations.value.splice(selectedIndex, 1)
      handleOrganizationChange()
    }
    
    // 从机构列表中删除
    organizationOptions.value.splice(index, 1)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSaveOrg = () => {
  if (!orgEditForm.name || orgEditForm.name.trim() === '') {
    ElMessage.warning('请输入机构名称')
    return
  }
  
  const name = orgEditForm.name.trim()
  
  if (orgEditMode.value === 'add') {
    // 检查是否已存在
    const exists = organizationOptions.value.some(org => org.value === name)
    if (exists) {
      ElMessage.warning('该机构已存在')
      return
    }
    // 新增
    organizationOptions.value.push({
      label: name,
      value: name
    })
    ElMessage.success('新增成功')
  } else {
    // 编辑
    if (orgEditForm.index >= 0 && orgEditForm.index < organizationOptions.value.length) {
      const oldValue = organizationOptions.value[orgEditForm.index].value
      const newValue = name
      
      // 更新机构选项
      organizationOptions.value[orgEditForm.index] = {
        label: name,
        value: name
      }
      
      // 如果该机构已被选中，更新选中列表
      const selectedIndex = selectedOrganizations.value.indexOf(oldValue)
      if (selectedIndex > -1) {
        selectedOrganizations.value[selectedIndex] = newValue
        handleOrganizationChange()
      }
      
      ElMessage.success('编辑成功')
    }
  }
  
  showOrgEditDialog.value = false
}

const handleSaveOrganizations = () => {
  // 机构列表已实时更新，这里只需要关闭对话框
  showOrgManageDialog.value = false
  ElMessage.success('机构列表已更新')
}

const handleOrgDialogClose = () => {
  orgSearchKeyword.value = ''
}

// 加载机构列表（从数据库）
const loadOrganizations = async () => {
  try {
    const res = await getOrganizations()
    if (res.code === 200 && res.data) {
      // 将机构列表转换为选项格式
      const dbOrgs = res.data.map(org => ({
        label: org,
        value: org
      }))
      
      // 合并数据库中的机构和现有机构（去重）
      const existingValues = new Set(organizationOptions.value.map(org => org.value))
      dbOrgs.forEach(org => {
        if (!existingValues.has(org.value)) {
          organizationOptions.value.push(org)
        }
      })
    }
  } catch (error) {
    console.error('加载机构列表失败', error)
    // 失败时使用默认列表
  }
}

const initChart = () => {
  const chartDom = document.getElementById('estimationChart')
  if (!chartDom) {
    console.error('图表容器未找到')
    return
  }
  
  chartInstance = echarts.init(chartDom)
  
  // 初始化时显示默认数据（7个空日期），确保图表框架显示
  const today = new Date()
  const defaultDates = []
  const defaultData = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    defaultDates.push(`${month}-${day}`)
    defaultData.push(0)
  }
  
  // 初始配置，确保图表框架完整显示
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        let result = params[0].name + '<br/>'
        params.forEach(function(item) {
          result += item.marker + item.seriesName + ': ' + item.value + ' 条<br/>'
        })
        return result
      }
    },
    legend: {
      data: ['新增线索数', '预估高潜线索数'],
      top: 10
    },
    grid: {
      left: '70px', // 增加左边距，确保Y轴名称完整显示
      right: '30px',
      bottom: '50px',
      top: '60px',
      containLabel: true // 改为true，确保标签不被裁剪
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: defaultDates,
      axisLabel: {
        rotate: 0,
        interval: 0,
        show: true
      },
      axisLine: {
        show: true
      },
      axisTick: {
        show: true
      }
    },
    yAxis: {
      type: 'value',
      name: '线索数量',
      nameLocation: 'middle',
      nameGap: 50, // 增加名称与轴线的距离
      nameTextStyle: {
        padding: [0, 0, 0, 0] // 确保文字不被裁剪
      },
      min: 0,
      max: 10,
      interval: 2,
      axisLabel: {
        show: true,
        margin: 8 // 增加标签与轴线的距离
      },
      axisLine: {
        show: true
      },
      axisTick: {
        show: true
      },
      splitLine: {
        show: true
      }
    },
    series: [
      {
        name: '新增线索数',
        type: 'line',
        smooth: true,
        data: defaultData,
        symbol: 'circle',
        symbolSize: 6,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ]
          }
        },
        lineStyle: {
          color: '#409eff'
        },
        itemStyle: {
          color: '#409eff'
        }
      },
      {
        name: '预估高潜线索数',
        type: 'line',
        smooth: true,
        data: defaultData,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          color: '#67c23a'
        },
        itemStyle: {
          color: '#67c23a'
        }
      }
    ]
  }
  chartInstance.setOption(option)
}

const loadChartData = async () => {
  if (!chartInstance) {
    console.warn('图表实例未初始化，尝试重新初始化')
    initChart()
    if (!chartInstance) {
      console.error('图表初始化失败')
      return
    }
  }
  
  try {
    const res = await getMonitorStatistics()
    console.log('统计数据响应:', res)
    
    if (res.code === 200 && res.data) {
      const data = res.data
      const dates = data.dates || []
      const leadCounts = data.leadCounts || []
      const highPotentialCounts = data.highPotentialCounts || [] // 每天新增的高潜线索数
      
      console.log('图表数据:', { dates, leadCounts, highPotentialCounts })
      
      // 更新统计数据
      statistics.value = {
        totalLeads: data.totalLeads || 0,
        estimatedHighPotential: data.estimatedHighPotential || 0 // 总高潜线索数
      }
      
      // 如果日期为空，使用默认日期
      let finalDates = dates
      if (dates.length === 0) {
        const today = new Date()
        finalDates = []
        for (let i = 6; i >= 0; i--) {
          const date = new Date(today)
          date.setDate(date.getDate() - i)
          const month = String(date.getMonth() + 1).padStart(2, '0')
          const day = String(date.getDate()).padStart(2, '0')
          finalDates.push(`${month}-${day}`)
        }
      }
      
      // 确保数据长度匹配
      const finalLeadCounts = leadCounts.length === finalDates.length 
        ? leadCounts 
        : new Array(finalDates.length).fill(0)
      
      // 使用后端返回的每天新增高潜线索数，如果后端没有返回则使用估算
      const finalHighPotentialCounts = highPotentialCounts.length === finalDates.length
        ? highPotentialCounts
        : finalLeadCounts.map(count => {
            // 如果后端没有返回，使用估算：假设活跃次数超过阈值的线索占比为30%
            const threshold = data.activityThreshold || 3
            return Math.round(count * 0.3)
          })
      
      // 更新图表
      chartInstance.setOption({
        xAxis: {
          data: finalDates
        },
        series: [
          {
            name: '新增线索数',
            data: finalLeadCounts
          },
          {
            name: '预估高潜线索数',
            data: finalHighPotentialCounts
          }
        ]
      }, { notMerge: false })
      
      // 自动调整Y轴最大值
      const allValues = [...finalLeadCounts, ...finalHighPotentialCounts]
      const maxValue = allValues.length > 0 ? Math.max(...allValues) : 0
      const yMax = maxValue > 0 ? Math.max(10, Math.ceil(maxValue * 1.2)) : 10
      const interval = Math.max(1, Math.ceil(yMax / 5))
      
      chartInstance.setOption({
        grid: {
          left: '70px' // 确保左边距足够，Y轴名称完整显示
        },
        yAxis: {
          max: yMax,
          interval: interval,
          min: 0,
          name: '线索数量',
          nameGap: 50,
          nameTextStyle: {
            padding: [0, 0, 0, 0]
          }
        }
      })
      
      // 确保图表重新渲染
      setTimeout(() => {
        chartInstance.resize()
      }, 100)
    } else {
      console.warn('统计数据格式错误:', res)
    }
  } catch (error) {
    console.error('加载图表数据失败', error)
    ElMessage.error('加载图表数据失败: ' + (error.message || '未知错误'))
  }
}

const handleCancel = () => {
  loadConfig()
  ElMessage.info('已取消配置')
}

const handleSave = async () => {
  formData.activeOrganizations = JSON.stringify(selectedOrganizations.value)
  try {
    await saveMonitorConfig(formData)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleRefreshMonitor = async () => {
  try {
    await ElMessageBox.confirm(
      '刷新监控将重新执行数据加工，根据当前配置筛选高潜线索。是否继续？',
      '刷新监控',
      {
        type: 'info',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    refreshing.value = true
    const loadingMessage = ElMessage({
      message: '正在刷新监控...',
      type: 'info',
      duration: 0
    })
    
    try {
      const res = await refreshMonitor()
      loadingMessage.close()
      if (res.code === 200) {
        ElMessage.success(res.message || `刷新完成，筛选出 ${res.data || 0} 条高潜线索`)
        // 刷新图表数据
        await loadChartData()
      } else {
        ElMessage.error(res.message || '刷新失败')
      }
    } catch (error) {
      loadingMessage.close()
      throw error
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('刷新监控失败: ' + (error.message || '未知错误'))
    }
  } finally {
    refreshing.value = false
  }
}
</script>

<style scoped>
.monitor-indicator {
  background: #ffffff;
  padding: 30px;
  border-radius: 4px;
}

.page-header {
  margin-bottom: 30px;
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

.config-form {
  max-width: 900px;
}

.form-item-content {
  width: 100%;
}

.unit {
  margin-left: 10px;
  color: #666666;
}

.description {
  margin-top: 8px;
  color: #999999;
  font-size: 12px;
}

.chart-container {
  width: 100%;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 4px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.chart-footer {
  display: flex;
  gap: 30px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
}

.stat-item {
  font-size: 14px;
  color: #606266;
}

.stat-item strong {
  color: #409eff;
  font-size: 16px;
  margin-left: 5px;
}

.org-select-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.org-manage-content {
  padding: 10px 0;
}

.org-manage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-buttons {
  margin-top: 30px;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

