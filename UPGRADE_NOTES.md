# Spring Boot 3.x 升级说明

## 升级内容

### 1. Spring Boot 版本
- **从**: 2.7.14
- **到**: 3.2.0（原生支持 JDK 21）

### 2. 依赖版本更新
- **MyBatis Plus**: 3.5.3.1 → 3.5.5
- **Druid**: 1.2.18 → 1.2.20
- **MySQL Driver**: mysql-connector-java 8.0.33 → mysql-connector-j 8.2.0
- **Lombok**: 1.18.30（支持 JDK 21）

### 3. Java 版本
- **JDK**: 21

## 主要变化

### 1. 包名变化（本项目未使用）
Spring Boot 3.x 将 `javax.*` 包迁移到 `jakarta.*`，但本项目代码中未使用 `javax` 包，因此无需修改代码。

### 2. MySQL 驱动变化
- **旧**: `mysql:mysql-connector-java`
- **新**: `com.mysql:mysql-connector-j`

### 3. 配置兼容性
- `application.yml` 配置保持兼容，无需修改
- CORS 配置保持兼容

## 升级后操作步骤

### 1. 重新加载 Maven 项目
在 IDEA 中：
- 右键点击 `pom.xml` → `Maven` → `Reload Project`
- 或点击右侧 Maven 面板的刷新按钮

### 2. 清理并重新编译
```bash
cd backend
mvn clean compile
```

### 3. 检查 IDEA 设置
- `File` → `Project Structure` → `Project`
  - `SDK`: JDK 21
  - `Language level`: 21
- `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
  - 确保 `Enable annotation processing` 已勾选

### 4. 更新 Lombok 插件
- `File` → `Settings` → `Plugins`
- 确保 Lombok 插件已安装并启用最新版本

### 5. 重启 IDEA
完成上述步骤后，重启 IDEA 以确保所有更改生效。

## 可能遇到的问题及解决方案

### 问题1: CORS 配置警告
如果运行时出现 CORS 相关警告，可能需要修改 `CorsConfig.java`：

```java
// 如果使用 setAllowCredentials(true)，不能使用 "*"
// 需要改为具体地址或使用模式
config.addAllowedOriginPattern("http://localhost:*");
// 或者
config.addAllowedOrigin("http://localhost:3000");
```

### 问题2: 依赖冲突
如果出现依赖冲突，执行：
```bash
mvn dependency:tree
```
查看依赖树，解决冲突。

### 问题3: 编译错误
如果仍有编译错误，尝试：
1. `File` → `Invalidate Caches / Restart`
2. 删除 `target` 目录
3. 重新编译

## 验证升级

运行以下命令验证项目是否能正常启动：
```bash
cd backend
mvn spring-boot:run
```

如果看到类似以下输出，说明升级成功：
```
Started MarketingApplication in X.XXX seconds
```

## 注意事项

1. **JDK 21 要求**: Spring Boot 3.x 最低要求 JDK 17，本项目使用 JDK 21
2. **向后兼容性**: Spring Boot 3.x 不完全向后兼容 2.x，但本项目代码无需修改
3. **生产环境**: 建议在测试环境充分测试后再部署到生产环境

