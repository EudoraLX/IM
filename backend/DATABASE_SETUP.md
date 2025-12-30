# 数据库初始化指南

## 方法一：使用 MySQL 命令行（推荐）

### 1. 打开 MySQL 命令行
```bash
mysql -u root -p
# 输入密码：Qwer.com123
```

### 2. 执行 SQL 脚本
```sql
source G:/a_eudora/IM/backend/src/main/resources/db/init.sql
```

或者直接复制粘贴整个 `init.sql` 文件的内容到 MySQL 命令行执行。

## 方法二：使用 MySQL Workbench 或其他工具

1. 打开 MySQL Workbench 或 Navicat 等工具
2. 连接到 MySQL 服务器
3. 打开 `backend/src/main/resources/db/init.sql` 文件
4. 执行整个脚本

## 方法三：使用 IDEA 数据库工具

1. 在 IDEA 中打开 Database 工具窗口
2. 连接到 MySQL 数据库
3. 右键点击连接 → `New` → `Query Console`
4. 打开 `backend/src/main/resources/db/init.sql` 文件
5. 复制所有内容到 Query Console
6. 点击执行按钮

## 验证数据库是否创建成功

执行以下 SQL 查询验证：

```sql
USE imdb;

-- 查看所有表
SHOW TABLES;

-- 应该看到以下表：
-- lead
-- monitor_indicator
-- push_frequency
-- high_potential_lead

-- 验证表结构
DESC monitor_indicator;

-- 验证默认数据
SELECT * FROM monitor_indicator;
SELECT * FROM push_frequency;
```

## 如果表已存在但报错

如果表已经存在但应用仍然报错，可能是：

1. **表在不同的数据库中**
   ```sql
   -- 检查当前使用的数据库
   SELECT DATABASE();
   
   -- 切换到正确的数据库
   USE imdb;
   ```

2. **表名大小写问题**
   - MySQL 在 Windows 上默认不区分大小写
   - 确保表名与代码中的一致

3. **权限问题**
   ```sql
   -- 检查当前用户权限
   SHOW GRANTS;
   ```

## 快速修复脚本

如果只想创建缺失的表，可以单独执行：

```sql
USE imdb;

-- 创建监控指标配置表
CREATE TABLE IF NOT EXISTS `monitor_indicator` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `time_range_days` INT(11) DEFAULT 7 COMMENT '监控时间范围（天数）',
    `active_organizations` TEXT COMMENT '活跃机构（JSON格式存储多个机构）',
    `activity_threshold` INT(11) DEFAULT 3 COMMENT '活跃次数阈值',
    `status` TINYINT(1) DEFAULT 1 COMMENT '配置状态：0-未启用，1-已启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控指标配置表';
```

## 常见问题

### Q: 执行 SQL 时报语法错误
A: 确保：
- 使用 UTF-8 编码
- 每个语句以分号结尾
- 没有复制多余的字符

### Q: 表创建成功但应用仍报错
A: 检查：
- `application.yml` 中的数据库名称是否为 `imdb`
- 数据库连接是否正常
- 重启应用

### Q: 如何重新初始化数据库
A: 执行 `init.sql` 脚本，它会先删除旧表再创建新表（使用 DROP TABLE IF EXISTS）

