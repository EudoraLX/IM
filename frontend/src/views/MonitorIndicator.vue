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
          <el-select
            v-model="selectedOrganizations"
            multiple
            placeholder="请选择活跃机构"
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
          <p class="description">仅监控所选机构下属产生的交互线索</p>
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
      <el-button type="primary" @click="handleSave">提交保存</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getMonitorConfig, saveMonitorConfig, getMonitorStatistics } from '../api/monitor'
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

const organizationOptions = [
  { label: '华东中心机构', value: '华东中心机构' },
  { label: '上海研发分部', value: '上海研发分部' },
  { label: '北京总部', value: '北京总部' },
  { label: '深圳分公司', value: '深圳分公司' },
  { label: '广州分公司', value: '广州分公司' }
]

let chartInstance = null
let refreshTimer = null

onMounted(async () => {
  await loadConfig()
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
      left: '50px',
      right: '30px',
      bottom: '50px',
      top: '60px',
      containLabel: false
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
      nameGap: 40,
      min: 0,
      max: 10,
      interval: 2,
      axisLabel: {
        show: true
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
      
      console.log('图表数据:', { dates, leadCounts })
      
      // 更新统计数据
      statistics.value = {
        totalLeads: data.totalLeads || 0,
        estimatedHighPotential: data.estimatedHighPotential || 0
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
      
      // 计算预估高潜线索数（基于阈值）
      const threshold = data.activityThreshold || 3
      const estimatedHighPotential = finalLeadCounts.map(count => {
        // 简化估算：假设活跃次数超过阈值的线索占比为30%
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
            data: estimatedHighPotential
          }
        ]
      }, { notMerge: false })
      
      // 自动调整Y轴最大值
      const allValues = [...finalLeadCounts, ...estimatedHighPotential]
      const maxValue = allValues.length > 0 ? Math.max(...allValues) : 0
      const yMax = maxValue > 0 ? Math.max(10, Math.ceil(maxValue * 1.2)) : 10
      const interval = Math.max(1, Math.ceil(yMax / 5))
      
      chartInstance.setOption({
        yAxis: {
          max: yMax,
          interval: interval,
          min: 0
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

.action-buttons {
  margin-top: 30px;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

