# 智能营销管理平台

## 项目简介

本项目是一套智能营销管理平台，结合第三方数据，对存量用户进行可视化分类管理，以实现高潜线索（存客）的高效转化，提升转化ROI。

## 技术栈

### 后端
- Java 1.8
- Spring Boot 2.7.14
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- Maven

### 前端
- Vue 3.3.4
- Vue Router 4.2.5
- Element Plus 2.4.2
- Axios 1.6.0
- ECharts 5.4.3
- Vite 5.0.0

## 项目结构

```
IM/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/hangyin/marketing/
│   │   │   │       ├── MarketingApplication.java    # 启动类
│   │   │   │       ├── common/                      # 通用类
│   │   │   │       ├── config/                      # 配置类
│   │   │   │       ├── controller/                   # 控制器
│   │   │   │       ├── entity/                       # 实体类
│   │   │   │       ├── mapper/                       # Mapper接口
│   │   │   │       └── service/                      # 服务层
│   │   │   └── resources/
│   │   │       ├── application.yml                   # 应用配置
│   │   │       ├── db/
│   │   │       │   └── schema.sql                    # 数据库脚本
│   │   │       └── mapper/                           # MyBatis XML
│   │   └── test/
│   └── pom.xml
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/                 # API接口
│   │   ├── layout/              # 布局组件
│   │   ├── router/              # 路由配置
│   │   ├── views/               # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## 功能模块

### 1. 线索（存客）名单管理
- 线索导入、新增、删除
- 线索列表查询（支持搜索、筛选）
- 线索状态管理（新线索、跟进中、已转化、已失效）

### 2. 监控指标管理
- 配置监控时间范围（天数）
- 选择活跃机构
- 设置活跃次数阈值
- 可视化展示配置覆盖线索预估

### 3. 推送频率管理
- 监控指标选择
- 频率策略可视化（每周分布热力图）
- 精细设置规则（推送间隔、周末规则、防爆保护）
- 预览与测试

### 4. 高潜线索营销
- 统计信息展示（命中用户总数、今日新增、待处理、转化率）
- 高潜线索列表
- 批量外呼营销
- 批量发送短信
- 线索状态跟踪

### 5. 数据加工及清洗
- T+1监控指标更新
- 自动筛选高潜线索
- 身份证哈希去重

### 6. 冷启动
- 预设监控指标配置
- 预设推送频率配置
- 导入名单即可自动筛选

## 环境要求

- JDK 1.8+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+
- IntelliJ IDEA（推荐）

## 快速开始

### 1. 数据库初始化

```sql
-- 执行数据库脚本
source backend/src/main/resources/db/schema.sql
```

或直接在MySQL中执行 `backend/src/main/resources/db/schema.sql` 文件。

### 2. 后端启动

```bash
# 进入后端目录
cd backend

# 使用Maven编译
mvn clean install

# 启动应用（IDEA中直接运行MarketingApplication.main方法）
# 或使用命令：
mvn spring-boot:run
```

后端服务默认运行在：`http://localhost:8080`

### 3. 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务默认运行在：`http://localhost:3000`

### 4. 访问系统

打开浏览器访问：`http://localhost:3000`

## 配置说明

### 后端配置

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/marketing_platform?...
    username: root          # 修改为你的MySQL用户名
    password: root          # 修改为你的MySQL密码
```

### 前端配置

前端API代理配置在 `frontend/vite.config.js`，如需修改后端地址，请修改proxy配置。

## API接口说明

### 线索管理
- `GET /api/lead/list` - 分页查询线索列表
- `GET /api/lead/{id}` - 查询线索详情
- `POST /api/lead/add` - 新增线索
- `PUT /api/lead/update` - 更新线索
- `DELETE /api/lead/{id}` - 删除线索
- `POST /api/lead/batchDelete` - 批量删除
- `POST /api/lead/batchImport` - 批量导入

### 监控指标管理
- `GET /api/monitor/config` - 获取监控配置
- `POST /api/monitor/save` - 保存监控配置

### 推送频率管理
- `GET /api/push/config` - 获取推送配置
- `POST /api/push/save` - 保存推送配置
- `POST /api/push/reset` - 重置推送配置

### 高潜线索营销
- `GET /api/highPotential/list` - 分页查询高潜线索
- `GET /api/highPotential/statistics` - 获取统计信息
- `POST /api/highPotential/batchCall` - 批量外呼
- `POST /api/highPotential/batchSms` - 批量发送短信
- `PUT /api/highPotential/status` - 更新线索状态

## 开发说明

### 后端开发
- 使用MyBatis Plus进行数据库操作
- 统一使用Result封装返回结果
- 使用PageResult进行分页返回
- 实体类使用Lombok简化代码

### 前端开发
- 使用Vue 3 Composition API
- 使用Element Plus组件库
- 使用Axios进行HTTP请求
- 使用ECharts进行数据可视化

## 注意事项

1. **外呼与短信功能**：需要对接甲方内部系统，当前仅提供接口框架，实际对接工作需由甲方自行开发。

2. **数据加工**：数据加工及清洗功能需要在后台定时任务中实现，当前版本未包含定时任务实现。

3. **身份证哈希**：身份证号使用哈希值存储，确保数据安全。

4. **跨域配置**：后端已配置CORS，支持前端跨域访问。

## 后续开发建议

1. 实现定时任务，完成T+1数据更新
2. 对接第三方外呼系统
3. 对接第三方短信系统
4. 完善数据导入导出功能
5. 添加用户权限管理
6. 添加操作日志记录
7. 完善数据可视化图表

## 许可证

本项目为内部项目，仅供杭银使用。

## 联系方式

如有问题，请联系开发团队。

