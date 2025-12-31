<template>
  <div class="test-flow">
    <div class="page-header">
      <h2>完整测试流程</h2>
      <p class="subtitle">可视化展示四个模块的串联流程</p>
    </div>

    <!-- 系统状态卡片 -->
    <div class="status-cards">
      <el-card class="status-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><UserFilled /></el-icon>
            <span>线索管理</span>
          </div>
        </template>
        <div class="card-content">
          <div class="stat-item">
            <span class="label">线索总数：</span>
            <span class="value">{{ flowStatus.leadCount || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="label">状态：</span>
            <el-tag :type="flowStatus.leadCount > 0 ? 'success' : 'info'">
              {{ flowStatus.leadStatus || '未知' }}
            </el-tag>
          </div>
        </div>
      </el-card>

      <el-card class="status-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><DataAnalysis /></el-icon>
            <span>监控指标管理</span>
          </div>
        </template>
        <div class="card-content">
          <div class="stat-item">
            <span class="label">状态：</span>
            <el-tag :type="flowStatus.monitorConfigExists ? 'success' : 'warning'">
              {{ flowStatus.monitorStatus || '未知' }}
            </el-tag>
          </div>
          <div v-if="flowStatus.monitorConfigExists" class="stat-item">
            <span class="label">时间范围：</span>
            <span class="value">{{ flowStatus.monitorTimeRange }}</span>
          </div>
          <div v-if="flowStatus.monitorConfigExists" class="stat-item">
            <span class="label">活跃阈值：</span>
            <span class="value">{{ flowStatus.monitorThreshold }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="status-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Bell /></el-icon>
            <span>推送频率管理</span>
          </div>
        </template>
        <div class="card-content">
          <div class="stat-item">
            <span class="label">状态：</span>
            <el-tag :type="flowStatus.pushConfigExists ? 'success' : 'warning'">
              {{ flowStatus.pushStatus || '未知' }}
            </el-tag>
          </div>
          <div v-if="flowStatus.pushConfigExists" class="stat-item">
            <span class="label">频率类型：</span>
            <span class="value">{{ flowStatus.pushFrequency }}</span>
          </div>
          <div v-if="flowStatus.pushConfigExists" class="stat-item">
            <span class="label">推送间隔：</span>
            <span class="value">{{ flowStatus.pushInterval }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="status-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Promotion /></el-icon>
            <span>高潜线索营销</span>
          </div>
        </template>
        <div class="card-content">
          <div class="stat-item">
            <span class="label">高潜线索数：</span>
            <span class="value">{{ flowStatus.highPotentialCount || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="label">状态：</span>
            <el-tag :type="flowStatus.highPotentialCount > 0 ? 'success' : 'info'">
              {{ flowStatus.highPotentialStatus || '未知' }}
            </el-tag>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 流程图 -->
    <el-card class="flow-diagram-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Connection /></el-icon>
          <span>模块串联流程图</span>
        </div>
      </template>
      <div class="flow-diagram">
        <!-- 步骤1：线索管理 -->
        <div class="flow-step" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
          <div class="step-icon">
            <el-icon v-if="currentStep > 1"><CircleCheck /></el-icon>
            <el-icon v-else><UserFilled /></el-icon>
          </div>
          <div class="step-content">
            <h3>步骤1：线索管理</h3>
            <p>创建和管理线索数据</p>
            <el-tag v-if="currentStep > 1" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="currentStep === 1" type="warning" size="small">执行中</el-tag>
            <el-tag v-else type="info" size="small">待执行</el-tag>
          </div>
        </div>

        <div class="flow-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>

        <!-- 步骤2：监控指标管理 -->
        <div class="flow-step" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
          <div class="step-icon">
            <el-icon v-if="currentStep > 2"><CircleCheck /></el-icon>
            <el-icon v-else><DataAnalysis /></el-icon>
          </div>
          <div class="step-content">
            <h3>步骤2：监控指标管理</h3>
            <p>配置监控规则和阈值</p>
            <el-tag v-if="currentStep > 2" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="currentStep === 2" type="warning" size="small">执行中</el-tag>
            <el-tag v-else type="info" size="small">待执行</el-tag>
          </div>
        </div>

        <div class="flow-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>

        <!-- 步骤3：推送频率管理 -->
        <div class="flow-step" :class="{ active: currentStep >= 3, completed: currentStep > 3 }">
          <div class="step-icon">
            <el-icon v-if="currentStep > 3"><CircleCheck /></el-icon>
            <el-icon v-else><Bell /></el-icon>
          </div>
          <div class="step-content">
            <h3>步骤3：推送频率管理</h3>
            <p>配置推送策略和频率</p>
            <el-tag v-if="currentStep > 3" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="currentStep === 3" type="warning" size="small">执行中</el-tag>
            <el-tag v-else type="info" size="small">待执行</el-tag>
          </div>
        </div>

        <div class="flow-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>

        <!-- 步骤4：数据加工 -->
        <div class="flow-step" :class="{ active: currentStep >= 4, completed: currentStep > 4 }">
          <div class="step-icon">
            <el-icon v-if="currentStep > 4"><CircleCheck /></el-icon>
            <el-icon v-else><Setting /></el-icon>
          </div>
          <div class="step-content">
            <h3>步骤4：数据加工及清洗</h3>
            <p>根据监控指标筛选高潜线索</p>
            <el-tag v-if="currentStep > 4" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="currentStep === 4" type="warning" size="small">执行中</el-tag>
            <el-tag v-else type="info" size="small">待执行</el-tag>
          </div>
        </div>

        <div class="flow-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>

        <!-- 步骤5：高潜线索营销 -->
        <div class="flow-step" :class="{ active: currentStep >= 5, completed: currentStep > 5 }">
          <div class="step-icon">
            <el-icon v-if="currentStep > 5"><CircleCheck /></el-icon>
            <el-icon v-else><Promotion /></el-icon>
          </div>
          <div class="step-content">
            <h3>步骤5：高潜线索营销</h3>
            <p>查看和操作高潜线索</p>
            <el-tag v-if="currentStep > 5" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="currentStep === 5" type="warning" size="small">执行中</el-tag>
            <el-tag v-else type="info" size="small">待执行</el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 执行步骤详情 -->
    <el-card v-if="executionResult && executionResult.steps" class="steps-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><List /></el-icon>
          <span>执行步骤详情</span>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="step in executionResult.steps"
          :key="step.stepNumber"
          :timestamp="step.moduleName"
          :type="getStepType(step.status)"
          placement="top"
        >
          <el-card>
            <h4>{{ step.stepName }}</h4>
            <p>{{ step.message || '执行中...' }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" size="large" @click="refreshStatus" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新状态
      </el-button>
      <el-button type="success" size="large" @click="executeFlow" :loading="executing">
        <el-icon><VideoPlay /></el-icon>
        执行完整流程
      </el-button>
      <el-button type="info" size="large" @click="goToHighPotential">
        <el-icon><Promotion /></el-icon>
        查看高潜线索
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFlowStatus, executeFullFlow } from '../api/testFlow'

const router = useRouter()
const loading = ref(false)
const executing = ref(false)
const currentStep = ref(0)
const flowStatus = ref({
  leadCount: 0,
  leadStatus: '未知',
  monitorConfigExists: false,
  monitorStatus: '未知',
  pushConfigExists: false,
  pushStatus: '未知',
  highPotentialCount: 0,
  highPotentialStatus: '未知'
})
const executionResult = ref(null)

const refreshStatus = async () => {
  loading.value = true
  try {
    const res = await getFlowStatus()
    if (res.code === 200) {
      flowStatus.value = res.data
      ElMessage.success('状态刷新成功')
    }
  } catch (error) {
    ElMessage.error('刷新状态失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const executeFlow = async () => {
  try {
    await ElMessageBox.confirm(
      '将执行完整测试流程，包括：\n1. 创建测试线索\n2. 配置监控指标\n3. 配置推送频率\n4. 执行数据加工\n5. 准备营销数据\n\n是否继续？',
      '执行完整流程',
      {
        type: 'info',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    executing.value = true
    currentStep.value = 0
    executionResult.value = null
    
    // 模拟步骤执行动画
    const stepInterval = setInterval(() => {
      if (currentStep.value < 5) {
        currentStep.value++
      } else {
        clearInterval(stepInterval)
      }
    }, 500)
    
    try {
      const res = await executeFullFlow(20)
      clearInterval(stepInterval)
      currentStep.value = 6
      
      if (res.code === 200) {
        executionResult.value = res.data
        ElMessage.success(res.data.message || '流程执行成功！')
        // 刷新状态
        await refreshStatus()
      } else {
        ElMessage.error(res.message || '执行失败')
      }
    } catch (error) {
      clearInterval(stepInterval)
      ElMessage.error('执行失败: ' + (error.message || '未知错误'))
    } finally {
      executing.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('执行失败: ' + (error.message || '未知错误'))
    }
  }
}

const goToHighPotential = () => {
  router.push('/highPotential')
}

const getStepType = (status) => {
  if (status === '完成') return 'success'
  if (status === '执行中') return 'warning'
  if (status === '失败') return 'danger'
  return 'primary'
}

onMounted(() => {
  refreshStatus()
})
</script>

<style scoped>
.test-flow {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.status-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.card-content {
  padding: 10px 0;
}

.stat-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.stat-item .label {
  color: #909399;
  font-size: 14px;
  margin-right: 8px;
}

.stat-item .value {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.flow-diagram-card {
  margin-bottom: 30px;
  border-radius: 8px;
}

.flow-diagram {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px 20px;
  flex-wrap: wrap;
  gap: 20px;
}

.flow-step {
  flex: 1;
  min-width: 180px;
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
  border: 2px solid #e4e7ed;
  transition: all 0.3s;
}

.flow-step.active {
  background: #ecf5ff;
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.flow-step.completed {
  background: #f0f9ff;
  border-color: #67c23a;
}

.step-icon {
  font-size: 48px;
  color: #909399;
  margin-bottom: 15px;
}

.flow-step.active .step-icon {
  color: #409eff;
}

.flow-step.completed .step-icon {
  color: #67c23a;
}

.step-content h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.step-content p {
  margin: 0 0 10px 0;
  font-size: 12px;
  color: #909399;
}

.flow-arrow {
  font-size: 32px;
  color: #c0c4cc;
  flex-shrink: 0;
}

.steps-card {
  margin-bottom: 30px;
  border-radius: 8px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 1200px) {
  .status-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .flow-diagram {
    flex-direction: column;
  }
  
  .flow-arrow {
    transform: rotate(90deg);
  }
}

@media (max-width: 768px) {
  .status-cards {
    grid-template-columns: 1fr;
  }
}
</style>

