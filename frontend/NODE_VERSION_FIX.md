# Node.js 版本问题解决方案

## 问题说明

当前 Node.js 版本：v16.20.2
Vite 5.0.0 需要：Node.js 18+

## 解决方案

### 方案一：降级 Vite 到 4.x（已自动修复）

我已经将 Vite 版本从 5.0.0 降级到 4.5.0，这个版本兼容 Node.js 16。

**操作步骤：**
```bash
cd frontend
# 删除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json
# 或者 Windows 上：
# rmdir /s /q node_modules
# del package-lock.json

# 重新安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 方案二：升级 Node.js（推荐，长期方案）

**优点：**
- 可以使用最新版本的 Vite 和工具
- 更好的性能和安全性
- 支持更多现代特性

**操作步骤：**

1. **下载并安装 Node.js 18 LTS 或更高版本**
   - 访问：https://nodejs.org/
   - 下载 LTS 版本（推荐 18.x 或 20.x）
   - 安装后重启终端

2. **验证安装**
   ```bash
   node --version
   # 应该显示 v18.x.x 或更高
   ```

3. **重新安装依赖**
   ```bash
   cd frontend
   rm -rf node_modules package-lock.json
   npm install
   npm run dev
   ```

4. **如果需要恢复 Vite 5.0.0**
   ```bash
   # 修改 package.json 中的 vite 版本为 ^5.0.0
   npm install
   ```

## 当前状态

我已经将 Vite 降级到 4.5.0，你现在可以：
1. 删除 `node_modules` 和 `package-lock.json`
2. 运行 `npm install`
3. 运行 `npm run dev`

## 推荐做法

建议升级到 Node.js 18 或 20 LTS 版本，因为：
- Vite 5.x 有更好的性能
- 更多现代 JavaScript 特性支持
- 更好的安全性
- 长期维护支持

