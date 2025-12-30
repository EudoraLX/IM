<template>
  <div class="push-frequency">
    <div class="page-header">
      <div>
        <h2>推送频率管理</h2>
        <p class="current-date">{{ currentDate }}</p>
      </div>
      <div class="header-buttons">
        <el-button @click="handleReset">重置配置</el-button>
        <el-button type="primary" @click="handleSave">保存配置</el-button>
      </div>
    </div>

    <!-- 监控指标选择 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <span>监控指标选择</span>
      </template>
      <el-input
        v-model="indicatorSearch"
        placeholder="Q 快速筛选指标..."
        class="search-input"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div class="indicator-list">
        <div
          v-for="indicator in filteredIndicators"
          :key="indicator.id"
          class="indicator-item"
        >
          <div class="indicator-info">
            <h4>{{ indicator.name }}</h4>
            <p>{{ indicator.description }}</p>
          </div>
          <el-switch v-model="indicator.enabled" />
        </div>
      </div>
    </el-card>

    <!-- 频率策略可视化 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <span>频率策略可视化（每周分布）</span>
      </template>
      <p class="chart-hint">点击区块快速切换:高频(蓝)/中频(橙)/低频(灰)/静默(白)</p>
      <div class="frequency-chart">
        <div class="chart-legend">
          <div class="legend-item">
            <span class="legend-color high"></span>
            <span>高频</span>
          </div>
          <div class="legend-item">
            <span class="legend-color medium"></span>
            <span>中频</span>
          </div>
          <div class="legend-item">
            <span class="legend-color low"></span>
            <span>低频</span>
          </div>
        </div>
        <div class="heatmap-container">
          <div class="heatmap" id="frequencyHeatmap"></div>
        </div>
      </div>
    </el-card>

    <!-- 精细设置规则 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <span>精细设置规则</span>
      </template>
      <el-form :model="formData" label-width="180px">
        <el-form-item label="默认推送间隔">
          <el-input-number v-model="formData.pushInterval" :min="1" :max="1440" />
          <el-select v-model="formData.intervalUnit" style="width: 100px; margin-left: 10px">
            <el-option label="分钟/次" value="minute" />
            <el-option label="小时/次" value="hour" />
          </el-select>
        </el-form-item>
        <el-form-item label="周末外规则">
          <el-switch v-model="formData.weekendRule" />
          <span class="form-desc">周末的推送将自动延长200%或进入静默状态。</span>
        </el-form-item>
        <el-form-item label="最小推送阈值（防爆保护）">
          <el-input-number v-model="formData.minPushThreshold" :min="1" :max="3600" />
          <span class="unit">秒</span>
          <p class="form-desc">防止短时间内产生大量重复推送,设置最小间隔时间。</p>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAddRule">
            <el-icon><Plus /></el-icon>
            添加特定时间段频率规则
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预览与测试 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <span>预览与测试</span>
      </template>
      <div class="preview-section">
        <div class="preview-title">今日推送概览（12次）</div>
        <div class="preview-list">
          <div class="preview-item">
            <span class="dot blue"></span>
            <span>09:15 - Push Sent</span>
          </div>
          <div class="preview-item">
            <span class="dot orange"></span>
            <span>11:45 - Delayed</span>
          </div>
        </div>
        <el-button type="primary" size="large" @click="handleTestPush">
          立即发送测试推送
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPushConfig, savePushConfig, resetPushConfig } from '../api/push'

const currentDate = ref(new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric'
}))

const indicatorSearch = ref('')
const indicators = ref([
  { id: 1, name: '异常登录频率', description: '监控24h内异地登录情况', enabled: true },
  { id: 2, name: '高带宽负载警告', description: '数据中心流量异常波动', enabled: true },
  { id: 3, name: 'API延迟监控', description: '核心接口P90响应时长', enabled: false }
])

const filteredIndicators = computed(() => {
  if (!indicatorSearch.value) return indicators.value
  return indicators.value.filter(item =>
    item.name.includes(indicatorSearch.value) ||
    item.description.includes(indicatorSearch.value))
})

const formData = reactive({
  id: null,
  pushInterval: 15,
  intervalUnit: 'minute',
  weekendRule: true,
  minPushThreshold: 30,
  frequencyStrategy: '{}',
  status: 1
})

onMounted(() => {
  loadConfig()
  initHeatmap()
})

const loadConfig = async () => {
  try {
    const res = await getPushConfig()
    if (res.code === 200 && res.data) {
      Object.assign(formData, res.data)
      if (res.data.frequencyStrategy) {
        try {
          const strategy = JSON.parse(res.data.frequencyStrategy)
          // 可以根据strategy恢复配置
        } catch (e) {
          // ignore
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

const initHeatmap = () => {
  // 简化的热力图实现
  const heatmapContainer = document.getElementById('frequencyHeatmap')
  if (!heatmapContainer) return

  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const hours = ['00', '02', '04', '06', '08', '10', '12', '14', '16', '18', '20', '22']
  
  let html = '<div class="heatmap-grid">'
  days.forEach((day, dayIdx) => {
    hours.forEach((hour, hourIdx) => {
      let className = 'heatmap-cell'
      // 简化的频率分配逻辑
      if (dayIdx < 5) { // 工作日
        if (hourIdx >= 2 && hourIdx <= 5) className += ' high'
        else if (hourIdx === 1 || hourIdx === 6) className += ' medium'
        else className += ' low'
      } else { // 周末
        if (hourIdx >= 2 && hourIdx <= 5) className += ' medium'
        else className += ' low'
      }
      html += `<div class="${className}" data-day="${dayIdx}" data-hour="${hourIdx}"></div>`
    })
  })
  html += '</div>'
  heatmapContainer.innerHTML = html
}

const handleReset = async () => {
  try {
    await resetPushConfig()
    ElMessage.success('重置成功')
    loadConfig()
  } catch (error) {
    ElMessage.error('重置失败')
  }
}

const handleSave = async () => {
  try {
    await savePushConfig(formData)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleAddRule = () => {
  ElMessage.info('添加规则功能待实现')
}

const handleTestPush = () => {
  ElMessage.success('测试推送已发送')
}
</script>

<style scoped>
.push-frequency {
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

.current-date {
  color: #666666;
  font-size: 14px;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.config-card {
  margin-bottom: 20px;
}

.search-input {
  margin-bottom: 20px;
}

.indicator-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.indicator-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
}

.indicator-info h4 {
  margin-bottom: 5px;
  color: #333333;
}

.indicator-info p {
  color: #666666;
  font-size: 12px;
}

.chart-hint {
  color: #666666;
  font-size: 12px;
  margin-bottom: 15px;
}

.frequency-chart {
  padding: 20px;
}

.chart-legend {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 2px;
}

.legend-color.high {
  background-color: #409eff;
}

.legend-color.medium {
  background-color: #e6a23c;
}

.legend-color.low {
  background-color: #909399;
}

.heatmap-container {
  width: 100%;
  overflow-x: auto;
}

.heatmap-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  grid-template-rows: repeat(7, 1fr);
  gap: 2px;
  min-width: 600px;
}

.heatmap-cell {
  width: 40px;
  height: 40px;
  border: 1px solid #e0e0e0;
  cursor: pointer;
  transition: all 0.2s;
}

.heatmap-cell.high {
  background-color: #409eff;
}

.heatmap-cell.medium {
  background-color: #e6a23c;
}

.heatmap-cell.low {
  background-color: #909399;
}

.heatmap-cell:hover {
  opacity: 0.8;
  transform: scale(1.1);
}

.form-desc {
  margin-left: 10px;
  color: #666666;
  font-size: 12px;
}

.unit {
  margin-left: 10px;
  color: #666666;
}

.preview-section {
  padding: 20px;
}

.preview-title {
  font-weight: 600;
  margin-bottom: 15px;
  color: #333333;
}

.preview-list {
  margin-bottom: 20px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  color: #666666;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot.blue {
  background-color: #409eff;
}

.dot.orange {
  background-color: #e6a23c;
}
</style>

