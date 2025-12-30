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

      <!-- 预估图表 -->
      <el-form-item label="当前配置覆盖线索预估">
        <div class="chart-container">
          <div id="estimationChart" style="width: 100%; height: 300px;"></div>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMonitorConfig, saveMonitorConfig } from '../api/monitor'
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

const organizationOptions = [
  { label: '华东中心机构', value: '华东中心机构' },
  { label: '上海研发分部', value: '上海研发分部' },
  { label: '北京总部', value: '北京总部' },
  { label: '深圳分公司', value: '深圳分公司' },
  { label: '广州分公司', value: '广州分公司' }
]

let chartInstance = null

onMounted(() => {
  loadConfig()
  setTimeout(() => {
    initChart()
  }, 100)
})

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
})

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
  if (!chartDom) return
  
  chartInstance = echarts.init(chartDom)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['12-19', '12-20', '12-21', '12-22', '12-23']
    },
    yAxis: {
      type: 'value',
      max: 300,
      interval: 50
    },
    series: [{
      data: [150, 220, 200, 200, 280],
      type: 'line',
      smooth: true,
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
      }
    }]
  }
  chartInstance.setOption(option)
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

.action-buttons {
  margin-top: 30px;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

