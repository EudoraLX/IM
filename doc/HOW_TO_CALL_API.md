# 如何调用测试接口 - 多种方式

## 方式一：前端界面按钮（最简单，推荐）

### 步骤

1. **启动前端**
   ```bash
   cd frontend
   npm run dev
   ```

2. **访问系统**
   打开浏览器：http://localhost:3000

3. **点击测试按钮**
   - 进入"线索管理"页面
   - 点击右上角的"创建测试数据"按钮（绿色按钮）
   - 确认后自动创建测试数据并执行数据加工

4. **查看结果**
   - 在"线索管理"页面查看创建的线索
   - 在"高潜线索营销"页面查看筛选出的高潜线索

## 方式二：浏览器开发者工具（适合快速测试）

### 步骤

1. **打开浏览器开发者工具**
   - 按 `F12` 或右键 → "检查"
   - 切换到 "Console"（控制台）标签

2. **执行JavaScript代码**
   在控制台中输入并回车：
   ```javascript
   fetch('http://localhost:8080/api/test/setupTestData?leadCount=20', {
     method: 'POST',
     headers: {
       'Content-Type': 'application/json'
     }
   })
   .then(response => response.json())
   .then(data => console.log('成功:', data))
   .catch(error => console.error('失败:', error))
   ```

3. **查看结果**
   - 控制台会显示返回结果
   - 刷新页面查看数据

## 方式三：使用Postman（专业工具）

### 步骤

1. **下载Postman**
   - 访问：https://www.postman.com/downloads/
   - 下载并安装

2. **创建请求**
   - 打开Postman
   - 选择 `POST` 方法
   - 输入URL：`http://localhost:8080/api/test/setupTestData?leadCount=20`
   - 点击 "Send" 发送

3. **查看响应**
   - 在下方查看返回结果

## 方式四：使用curl命令（命令行）

### Windows PowerShell

```powershell
# 创建测试数据
Invoke-WebRequest -Uri "http://localhost:8080/api/test/setupTestData?leadCount=20" -Method POST
```

### Windows CMD

```cmd
curl -X POST "http://localhost:8080/api/test/setupTestData?leadCount=20"
```

### Linux/Mac

```bash
curl -X POST "http://localhost:8080/api/test/setupTestData?leadCount=20"
```

## 方式五：使用浏览器地址栏（仅GET请求）

**注意**：POST请求不能直接在地址栏输入，但可以创建一个简单的HTML页面：

1. **创建测试页面**
   创建文件 `test.html`：
   ```html
   <!DOCTYPE html>
   <html>
   <head>
       <title>测试接口</title>
   </head>
   <body>
       <h1>测试接口调用</h1>
       <button onclick="createTestData()">创建测试数据</button>
       <div id="result"></div>
       
       <script>
       async function createTestData() {
           try {
               const response = await fetch('http://localhost:8080/api/test/setupTestData?leadCount=20', {
                   method: 'POST'
               });
               const data = await response.json();
               document.getElementById('result').innerHTML = 
                   '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
           } catch (error) {
               document.getElementById('result').innerHTML = '错误: ' + error.message;
           }
       }
       </script>
   </body>
   </html>
   ```

2. **打开页面**
   - 双击 `test.html` 文件
   - 点击按钮执行

## 推荐方式对比

| 方式 | 难度 | 推荐度 | 适用场景 |
|------|------|--------|----------|
| 前端按钮 | ⭐ 最简单 | ⭐⭐⭐⭐⭐ | 日常测试 |
| 浏览器控制台 | ⭐⭐ 简单 | ⭐⭐⭐⭐ | 快速测试 |
| Postman | ⭐⭐⭐ 中等 | ⭐⭐⭐⭐ | 专业测试 |
| curl命令 | ⭐⭐⭐⭐ 较难 | ⭐⭐⭐ | 自动化脚本 |
| HTML页面 | ⭐⭐ 简单 | ⭐⭐⭐ | 独立测试 |

## 快速开始（推荐）

**最简单的方式**：

1. 启动前端：`cd frontend && npm run dev`
2. 访问：http://localhost:3000
3. 进入"线索管理"页面
4. 点击"创建测试数据"按钮
5. 完成！

## 接口说明

### 创建测试数据接口

**URL**: `POST /api/test/setupTestData?leadCount=20`

**参数**:
- `leadCount`（可选）：要创建的线索数量，默认20

**返回**:
```json
{
  "code": 200,
  "message": "测试数据创建完成！\n- 创建线索: 20 条\n- 筛选高潜线索: 10 条\n请在'高潜线索营销'页面查看结果",
  "data": null
}
```

## 其他测试接口

### 1. 仅创建测试线索
```
POST http://localhost:8080/api/test/createTestLeads?count=10
```

### 2. 手动触发数据加工
```
POST http://localhost:8080/api/dataProcessing/process
```

## 常见问题

### Q: 为什么前端按钮点击没反应？
A: 检查：
1. 后端服务是否启动（http://localhost:8080）
2. 浏览器控制台是否有错误
3. 网络请求是否成功（F12 → Network 标签）

### Q: 使用curl报错？
A: 
- Windows可能需要安装curl或使用PowerShell的Invoke-WebRequest
- 确保后端服务已启动

### Q: Postman连接失败？
A: 
- 检查URL是否正确
- 检查后端服务是否启动
- 检查防火墙设置

## 总结

**最简单的方式**：使用前端界面上的"创建测试数据"按钮，一键完成！

