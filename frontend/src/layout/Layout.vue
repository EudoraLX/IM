<template>
  <div class="layout-container">
    <!-- 左侧导航栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>线索管家</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#1e3a5f"
        text-color="#ffffff"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/lead">
          <el-icon><UserFilled /></el-icon>
          <span>线索管理</span>
        </el-menu-item>
        <el-menu-item index="/monitor">
          <el-icon><DataAnalysis /></el-icon>
          <span>监控指标管理</span>
        </el-menu-item>
        <el-menu-item index="/push">
          <el-icon><Bell /></el-icon>
          <span>推送频率管理</span>
        </el-menu-item>
        <el-menu-item index="/highPotential">
          <el-icon><Promotion /></el-icon>
          <span>高潜线索营销</span>
        </el-menu-item>
        <el-menu-item index="/testFlow">
          <el-icon><Connection /></el-icon>
          <span>完整测试流程</span>
        </el-menu-item>
        <el-menu-item index="/activityRecord">
          <el-icon><Document /></el-icon>
          <span>活跃次数记录</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <p>v2.5.11 2025.12.23更新</p>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <div class="header-bar">
        <div class="header-title">{{ currentTitle }}</div>
        <div class="header-actions">
          <el-icon class="header-icon"><Bell /></el-icon>
          <el-icon class="header-icon"><Setting /></el-icon>
          <div class="user-info">
            <el-avatar :size="32" src="" />
            <span>系统管理员</span>
          </div>
        </div>
      </div>
      <div class="content-wrapper">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '智能营销管理平台')
</script>

<style scoped>
.layout-container {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 240px;
  background-color: #1e3a5f;
  display: flex;
  flex-direction: column;
  color: #ffffff;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h2 {
  color: #ffffff;
  font-size: 20px;
  font-weight: 600;
}

.sidebar-menu {
  flex: 1;
  border: none;
}

.sidebar-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #2c5a8a !important;
}

.sidebar-footer {
  padding: 15px;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  overflow: hidden;
}

.header-bar {
  height: 60px;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  font-size: 20px;
  color: #666666;
  cursor: pointer;
}

.header-icon:hover {
  color: #409eff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-info span {
  color: #333333;
  font-size: 14px;
}

.content-wrapper {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>

