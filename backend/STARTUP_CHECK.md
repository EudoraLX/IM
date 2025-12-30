# 启动后验证清单

## 1. 检查启动日志

启动成功后，应该看到类似以下日志：
```
Started MarketingApplication in X.XXX seconds (process running for X.XXX)
```

## 2. 检查数据库连接

确认数据库连接成功，日志中应该看到：
- Druid 数据源初始化成功
- MyBatis Mapper 扫描成功

## 3. 测试 API 接口

### 3.1 测试监控指标配置接口
```bash
curl http://localhost:8080/api/monitor/config
```

### 3.2 测试线索列表接口
```bash
curl http://localhost:8080/api/lead/list?current=1&size=10
```

### 3.3 测试高潜线索统计接口
```bash
curl http://localhost:8080/api/highPotential/statistics
```

## 4. 前端连接测试

1. 确保前端已启动（`npm run dev`）
2. 访问 `http://localhost:3000`
3. 检查是否能正常加载页面
4. 测试各个功能模块

## 5. 常见问题排查

### 问题1: 数据库连接失败
- 检查 `application.yml` 中的数据库配置
- 确认数据库 `imdb` 已创建
- 确认数据库用户名密码正确

### 问题2: 端口被占用
- 检查 8080 端口是否被占用
- 修改 `application.yml` 中的端口号

### 问题3: 前端无法连接后端
- 检查后端是否正常启动
- 检查前端代理配置（`vite.config.js`）
- 检查浏览器控制台错误信息

## 6. 下一步操作

1. **导入测试数据**（可选）
   - 可以通过 API 添加测试线索
   - 或直接在数据库中插入测试数据

2. **配置监控指标**
   - 访问前端页面：`http://localhost:3000/monitor`
   - 配置监控指标参数

3. **配置推送频率**
   - 访问前端页面：`http://localhost:3000/push`
   - 配置推送频率策略

4. **查看高潜线索**
   - 访问前端页面：`http://localhost:3000/highPotential`
   - 查看高潜线索列表

