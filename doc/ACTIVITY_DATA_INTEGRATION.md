# 活跃度数据接入指南

## 问题说明

**如何获取线索的活跃次数？**

活跃次数应该来自第三方数据源，包括：
- 人脸验证事件日志
- 身份证OCR验证事件日志
- 数据调用事件日志
- 其他业务事件日志

## 解决方案

### 方案1：对接客户端APP的API（推荐）

如果客户端APP已经提供了活跃度数据API，可以按以下步骤接入：

#### 步骤1：配置API地址

在 `application.yml` 中配置：

```yaml
activity:
  data:
    enabled: true  # 启用第三方数据源
    api:
      url: http://your-app-server:port/api/activity  # 客户端APP的API地址
```

#### 步骤2：修改 ActivityDataServiceImpl

打开文件：`backend/src/main/java/com/hangyin/marketing/service/impl/ActivityDataServiceImpl.java`

找到 `getActivityCount` 方法，修改为实际API调用：

```java
@Override
public int getActivityCount(Long leadId, LocalDateTime startTime, LocalDateTime endTime, List<String> organizations) {
    try {
        // 构建API请求URL
        String url = activityDataApiUrl + "/count";
        
        // 构建请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("leadId", leadId);
        params.put("startTime", startTime.toString());
        params.put("endTime", endTime.toString());
        params.put("organizations", organizations);
        
        // 调用客户端APP的API
        // 方式1：GET请求
        Integer count = restTemplate.getForObject(
            url + "?leadId={leadId}&startTime={startTime}&endTime={endTime}", 
            Integer.class, 
            params
        );
        
        // 方式2：POST请求（如果API是POST）
        // Map<String, Object> requestBody = new HashMap<>();
        // requestBody.put("leadId", leadId);
        // requestBody.put("startTime", startTime);
        // requestBody.put("endTime", endTime);
        // requestBody.put("organizations", organizations);
        // 
        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        // HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        // 
        // ResponseEntity<Integer> response = restTemplate.postForEntity(url, entity, Integer.class);
        // Integer count = response.getBody();
        
        return count != null ? count : 0;
        
    } catch (Exception e) {
        log.error("调用活跃度数据API失败，leadId: {}", leadId, e);
        // 失败时返回0或使用模拟数据
        return 0;
    }
}
```

#### 步骤3：实现批量获取方法

同样修改 `batchGetActivityCounts` 方法：

```java
@Override
public Map<Long, Integer> batchGetActivityCounts(List<Long> leadIds, LocalDateTime startTime, 
                                                 LocalDateTime endTime, List<String> organizations) {
    try {
        String url = activityDataApiUrl + "/batchCount";
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("leadIds", leadIds);
        requestBody.put("startTime", startTime);
        requestBody.put("endTime", endTime);
        requestBody.put("organizations", organizations);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        // 调用API
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        Map<String, Integer> resultMap = response.getBody();
        
        // 转换结果
        Map<Long, Integer> result = new HashMap<>();
        if (resultMap != null) {
            for (Map.Entry<String, Integer> entry : resultMap.entrySet()) {
                result.put(Long.parseLong(entry.getKey()), entry.getValue());
            }
        }
        
        return result;
        
    } catch (Exception e) {
        log.error("批量获取活跃次数失败", e);
        return new HashMap<>();
    }
}
```

### 方案2：对接数据库（如果事件日志存储在数据库）

如果客户端APP将事件日志存储在数据库中，可以直接查询数据库：

#### 步骤1：创建事件日志表（如果不存在）

```sql
CREATE TABLE IF NOT EXISTS `activity_event_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `lead_id` BIGINT(20) COMMENT '线索ID',
    `event_type` VARCHAR(50) COMMENT '事件类型：FACE_VERIFICATION-人脸验证，ID_CARD_OCR-身份证OCR，DATA_CALL-数据调用',
    `organization` VARCHAR(100) COMMENT '机构名称',
    `event_time` DATETIME COMMENT '事件时间',
    `event_detail` TEXT COMMENT '事件详情',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_lead_id` (`lead_id`),
    KEY `idx_event_time` (`event_time`),
    KEY `idx_organization` (`organization`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活跃事件日志表';
```

#### 步骤2：创建Mapper和Service

创建 `ActivityEventLogMapper` 和相应的查询方法，直接查询数据库统计活跃次数。

### 方案3：使用消息队列（实时处理）

如果客户端APP通过消息队列发送事件，可以：

1. 监听消息队列（如RabbitMQ、Kafka）
2. 实时接收事件并存储到数据库
3. 查询数据库统计活跃次数

## 客户端APP需要提供的API接口

### 接口1：获取单个线索的活跃次数

**请求**：
```
GET /api/activity/count?leadId={leadId}&startTime={startTime}&endTime={endTime}&organizations={org1,org2}
```

**响应**：
```json
{
  "leadId": 1,
  "activityCount": 5,
  "startTime": "2025-12-23T00:00:00",
  "endTime": "2025-12-30T23:59:59"
}
```

### 接口2：批量获取活跃次数

**请求**：
```
POST /api/activity/batchCount
Content-Type: application/json

{
  "leadIds": [1, 2, 3, 4, 5],
  "startTime": "2025-12-23T00:00:00",
  "endTime": "2025-12-30T23:59:59",
  "organizations": ["华东中心机构", "上海研发分部"]
}
```

**响应**：
```json
{
  "1": 5,
  "2": 3,
  "3": 8,
  "4": 2,
  "5": 6
}
```

### 接口3：获取活跃事件详情（可选）

**请求**：
```
GET /api/activity/events?leadId={leadId}&startTime={startTime}&endTime={endTime}
```

**响应**：
```json
[
  {
    "leadId": 1,
    "eventType": "FACE_VERIFICATION",
    "organization": "华东中心机构",
    "eventTime": "2025-12-25T10:30:00",
    "eventDetail": "人脸验证成功"
  },
  {
    "leadId": 1,
    "eventType": "ID_CARD_OCR",
    "organization": "华东中心机构",
    "eventTime": "2025-12-26T14:20:00",
    "eventDetail": "身份证OCR验证"
  }
]
```

## 修改代码的位置

### 核心文件

1. **`ActivityDataServiceImpl.java`** 
   - 位置：`backend/src/main/java/com/hangyin/marketing/service/impl/ActivityDataServiceImpl.java`
   - 作用：实现活跃度数据获取逻辑
   - 需要修改的方法：
     - `getActivityCount()` - 获取单个线索活跃次数
     - `batchGetActivityCounts()` - 批量获取活跃次数
     - `getActivityEvents()` - 获取活跃事件详情

2. **`application.yml`**
   - 位置：`backend/src/main/resources/application.yml`
   - 作用：配置API地址和开关
   - 需要配置：
     ```yaml
     activity:
       data:
         enabled: true  # 启用第三方数据源
         api:
           url: http://your-api-server:port/api/activity
     ```

### 数据加工服务

`DataProcessingServiceImpl.java` 已经集成了 `ActivityDataService`，会自动调用获取活跃次数，无需修改。

## 开发测试

### 测试步骤

1. **使用模拟数据测试**（默认）
   ```yaml
   activity:
     data:
       enabled: false  # 使用模拟数据
   ```
   - 系统会使用模拟的活跃次数进行测试

2. **对接真实API测试**
   ```yaml
   activity:
     data:
       enabled: true
       api:
         url: http://localhost:8081/api/activity
   ```
   - 修改 `ActivityDataServiceImpl` 中的API调用代码
   - 启动客户端APP或数据服务
   - 测试数据加工功能

### 调试建议

1. **查看日志**：检查 `ActivityDataServiceImpl` 的日志输出
2. **测试API**：使用Postman等工具先测试客户端APP的API
3. **逐步集成**：先实现单个线索查询，再实现批量查询

## 示例：对接HTTP API

完整示例代码：

```java
@Override
public int getActivityCount(Long leadId, LocalDateTime startTime, LocalDateTime endTime, List<String> organizations) {
    try {
        // 构建请求URL
        String url = activityDataApiUrl + "/count";
        
        // 构建请求参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("leadId", leadId)
            .queryParam("startTime", startTime.toString())
            .queryParam("endTime", endTime.toString());
        
        if (organizations != null && !organizations.isEmpty()) {
            builder.queryParam("organizations", String.join(",", organizations));
        }
        
        // 发送GET请求
        ResponseEntity<Map> response = restTemplate.getForEntity(
            builder.toUriString(), 
            Map.class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            Object count = body.get("activityCount");
            if (count != null) {
                return Integer.parseInt(count.toString());
            }
        }
        
        return 0;
        
    } catch (Exception e) {
        log.error("获取活跃次数失败，leadId: {}", leadId, e);
        return 0;
    }
}
```

## 注意事项

1. **API认证**：如果客户端APP的API需要认证，需要在请求头中添加Token
2. **错误处理**：API调用失败时，要有降级策略（返回0或使用缓存数据）
3. **性能优化**：批量查询时使用批量API，避免多次单独调用
4. **数据同步**：确保客户端APP的事件日志及时更新

## 总结

**修改代码的位置**：
- 主要修改：`ActivityDataServiceImpl.java` 中的API调用方法
- 配置文件：`application.yml` 中的API地址配置

**接入流程**：
1. 确认客户端APP的API接口格式
2. 修改 `ActivityDataServiceImpl.java` 中的API调用代码
3. 在 `application.yml` 中配置API地址
4. 设置 `activity.data.enabled=true` 启用第三方数据源
5. 测试验证

