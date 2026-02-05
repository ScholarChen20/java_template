#!/bin/bash

# 企业级项目结构重构脚本
# 用途: 将现有项目重构为标准的企业级DDD分层架构

echo "========================================="
echo "  企业级项目结构重构工具"
echo "========================================="
echo ""

PROJECT_ROOT="D:/JavaPro/yoyo_data"
SRC_DIR="$PROJECT_ROOT/src/main/java/com/example/yoyo_data"

# 创建新的目录结构
echo "📁 创建企业级目录结构..."

# API层
mkdir -p "$SRC_DIR/api/controller/auth"
mkdir -p "$SRC_DIR/api/controller/business"
mkdir -p "$SRC_DIR/api/controller/system"
mkdir -p "$SRC_DIR/api/handler"
mkdir -p "$SRC_DIR/api/interceptor"
mkdir -p "$SRC_DIR/api/aspect"

# 领域层
mkdir -p "$SRC_DIR/domain/entity/mysql"
mkdir -p "$SRC_DIR/domain/entity/mongodb"
mkdir -p "$SRC_DIR/domain/dto/request"
mkdir -p "$SRC_DIR/domain/dto/response"
mkdir -p "$SRC_DIR/domain/dto/common"
mkdir -p "$SRC_DIR/domain/vo"
mkdir -p "$SRC_DIR/domain/enums"

# 应用层
mkdir -p "$SRC_DIR/application/service/auth"
mkdir -p "$SRC_DIR/application/service/business"
mkdir -p "$SRC_DIR/application/service/system"
mkdir -p "$SRC_DIR/application/service/impl/auth"
mkdir -p "$SRC_DIR/application/service/impl/business"
mkdir -p "$SRC_DIR/application/service/impl/system"

# 基础设施层
mkdir -p "$SRC_DIR/infrastructure/repository/mysql"
mkdir -p "$SRC_DIR/infrastructure/repository/mongodb"
mkdir -p "$SRC_DIR/infrastructure/cache"
mkdir -p "$SRC_DIR/infrastructure/mq"
mkdir -p "$SRC_DIR/infrastructure/storage"
mkdir -p "$SRC_DIR/infrastructure/external"

# 配置层
mkdir -p "$SRC_DIR/config/properties"
mkdir -p "$SRC_DIR/config/database"
mkdir -p "$SRC_DIR/config/cache"
mkdir -p "$SRC_DIR/config/security"
mkdir -p "$SRC_DIR/config/middleware"
mkdir -p "$SRC_DIR/config/async"

# 通用层
mkdir -p "$SRC_DIR/common/base"
mkdir -p "$SRC_DIR/common/result"
mkdir -p "$SRC_DIR/common/exception"
mkdir -p "$SRC_DIR/common/constant"
mkdir -p "$SRC_DIR/common/annotation"
mkdir -p "$SRC_DIR/common/validator"

# 工具层
mkdir -p "$SRC_DIR/util/jwt"
mkdir -p "$SRC_DIR/util/encrypt"
mkdir -p "$SRC_DIR/util/date"
mkdir -p "$SRC_DIR/util/collection"
mkdir -p "$SRC_DIR/util/json"
mkdir -p "$SRC_DIR/util/http"
mkdir -p "$SRC_DIR/util/thread"

echo "✅ 目录结构创建完成"
echo ""

# 移动现有文件到新结构
echo "📦 重组现有文件..."

# Controller层移动
if [ -d "$SRC_DIR/controller" ]; then
    echo "  移动 Controller 文件..."
    mv "$SRC_DIR/controller/AuthController.java" "$SRC_DIR/api/controller/auth/" 2>/dev/null
    mv "$SRC_DIR/controller/UserController.java" "$SRC_DIR/api/controller/auth/" 2>/dev/null
    mv "$SRC_DIR/controller/DialogController.java" "$SRC_DIR/api/controller/business/" 2>/dev/null
    mv "$SRC_DIR/controller/TravelPlanController.java" "$SRC_DIR/api/controller/business/" 2>/dev/null
    mv "$SRC_DIR/controller/PostController.java" "$SRC_DIR/api/controller/business/" 2>/dev/null
    mv "$SRC_DIR/controller/CommentController.java" "$SRC_DIR/api/controller/business/" 2>/dev/null
    mv "$SRC_DIR/controller/LikeController.java" "$SRC_DIR/api/controller/business/" 2>/dev/null
    mv "$SRC_DIR/controller/SystemController.java" "$SRC_DIR/api/controller/system/" 2>/dev/null
    mv "$SRC_DIR/controller/FileUploadController.java" "$SRC_DIR/api/controller/system/" 2>/dev/null
fi

# Service层移动
if [ -d "$SRC_DIR/service" ]; then
    echo "  移动 Service 文件..."
    # 接口
    mv "$SRC_DIR/service/AuthService.java" "$SRC_DIR/application/service/auth/" 2>/dev/null
    mv "$SRC_DIR/service/UserService.java" "$SRC_DIR/application/service/auth/" 2>/dev/null
    mv "$SRC_DIR/service/DialogService.java" "$SRC_DIR/application/service/business/" 2>/dev/null
    mv "$SRC_DIR/service/TravelPlanService.java" "$SRC_DIR/application/service/business/" 2>/dev/null
    mv "$SRC_DIR/service/PostService.java" "$SRC_DIR/application/service/business/" 2>/dev/null
    mv "$SRC_DIR/service/CommentService.java" "$SRC_DIR/application/service/business/" 2>/dev/null
    mv "$SRC_DIR/service/LikeService.java" "$SRC_DIR/application/service/business/" 2>/dev/null
    mv "$SRC_DIR/service/FileUploadService.java" "$SRC_DIR/application/service/system/" 2>/dev/null
    mv "$SRC_DIR/service/SystemService.java" "$SRC_DIR/application/service/system/" 2>/dev/null

    # 实现
    if [ -d "$SRC_DIR/service/impl" ]; then
        mv "$SRC_DIR/service/impl/AuthServiceImpl.java" "$SRC_DIR/application/service/impl/auth/" 2>/dev/null
        mv "$SRC_DIR/service/impl/UserServiceImpl.java" "$SRC_DIR/application/service/impl/auth/" 2>/dev/null
        mv "$SRC_DIR/service/impl/DialogServiceImpl.java" "$SRC_DIR/application/service/impl/business/" 2>/dev/null
        mv "$SRC_DIR/service/impl/TravelPlanServiceImpl.java" "$SRC_DIR/application/service/impl/business/" 2>/dev/null
        mv "$SRC_DIR/service/impl/PostServiceImpl.java" "$SRC_DIR/application/service/impl/business/" 2>/dev/null
        mv "$SRC_DIR/service/impl/CommentServiceImpl.java" "$SRC_DIR/application/service/impl/business/" 2>/dev/null
        mv "$SRC_DIR/service/impl/LikeServiceImpl.java" "$SRC_DIR/application/service/impl/business/" 2>/dev/null
        mv "$SRC_DIR/service/impl/FileUploadServiceImpl.java" "$SRC_DIR/application/service/impl/system/" 2>/dev/null
        mv "$SRC_DIR/service/impl/SystemServiceImpl.java" "$SRC_DIR/application/service/impl/system/" 2>/dev/null
    fi
fi

# Mapper/Repository层移动
if [ -d "$SRC_DIR/mapper" ]; then
    echo "  移动 Mapper 文件..."
    mv "$SRC_DIR/mapper"/* "$SRC_DIR/infrastructure/repository/mysql/" 2>/dev/null
fi

if [ -d "$SRC_DIR/repository" ]; then
    echo "  移动 Repository 文件..."
    mv "$SRC_DIR/repository"/* "$SRC_DIR/infrastructure/repository/mongodb/" 2>/dev/null
fi

# Entity层移动
if [ -d "$SRC_DIR/common/pojo" ]; then
    echo "  移动 Entity 文件..."
    mv "$SRC_DIR/common/pojo/Users.java" "$SRC_DIR/domain/entity/mysql/" 2>/dev/null
    mv "$SRC_DIR/common/pojo/UserProfile.java" "$SRC_DIR/domain/entity/mysql/" 2>/dev/null
    mv "$SRC_DIR/common/pojo/Post.java" "$SRC_DIR/domain/entity/mysql/" 2>/dev/null
    mv "$SRC_DIR/common/pojo/Comment.java" "$SRC_DIR/domain/entity/mysql/" 2>/dev/null
    mv "$SRC_DIR/common/pojo/Like.java" "$SRC_DIR/domain/entity/mysql/" 2>/dev/null
    mv "$SRC_DIR/common/pojo/Follow.java" "$SRC_DIR/domain/entity/mysql/" 2>/dev/null
    mv "$SRC_DIR/common/pojo/Tag.java" "$SRC_DIR/domain/entity/mysql/" 2>/dev/null
    mv "$SRC_DIR/common/pojo/Role.java" "$SRC_DIR/domain/entity/mysql/" 2>/dev/null
    mv "$SRC_DIR/common/pojo/Permission.java" "$SRC_DIR/domain/entity/mysql/" 2>/dev/null
    mv "$SRC_DIR/common/pojo/CaptchaRecord.java" "$SRC_DIR/domain/entity/mysql/" 2>/dev/null
    mv "$SRC_DIR/common/pojo/AuditLog.java" "$SRC_DIR/domain/entity/mysql/" 2>/dev/null
fi

if [ -d "$SRC_DIR/document" ]; then
    echo "  移动 MongoDB Document 文件..."
    mv "$SRC_DIR/document"/* "$SRC_DIR/domain/entity/mongodb/" 2>/dev/null
fi

if [ -d "$SRC_DIR/common/document" ]; then
    echo "  移动 MongoDB Document 文件..."
    mv "$SRC_DIR/common/document"/* "$SRC_DIR/domain/entity/mongodb/" 2>/dev/null
fi

# DTO层移动
if [ -d "$SRC_DIR/dto" ]; then
    echo "  移动 DTO 文件..."
    mv "$SRC_DIR/dto"/* "$SRC_DIR/domain/dto/common/" 2>/dev/null
fi

if [ -d "$SRC_DIR/common/dto" ]; then
    echo "  移动 DTO 文件..."
    mv "$SRC_DIR/common/dto/request"/* "$SRC_DIR/domain/dto/request/" 2>/dev/null
    mv "$SRC_DIR/common/dto/response"/* "$SRC_DIR/domain/dto/response/" 2>/dev/null
    mv "$SRC_DIR/common/dto"/*.java "$SRC_DIR/domain/dto/common/" 2>/dev/null
fi

# VO层移动
if [ -d "$SRC_DIR/common/vo" ]; then
    echo "  移动 VO 文件..."
    mv "$SRC_DIR/common/vo"/* "$SRC_DIR/domain/vo/" 2>/dev/null
fi

# Config层移动
if [ -d "$SRC_DIR/config" ]; then
    echo "  移动 Config 文件..."
    mv "$SRC_DIR/config/RedisConfig.java" "$SRC_DIR/config/cache/" 2>/dev/null
    mv "$SRC_DIR/config/RedissonConfig.java" "$SRC_DIR/config/cache/" 2>/dev/null
    mv "$SRC_DIR/config/MongoConfig.java" "$SRC_DIR/config/database/" 2>/dev/null
    mv "$SRC_DIR/config/WebMvcConfig.java" "$SRC_DIR/config/security/" 2>/dev/null
    mv "$SRC_DIR/config/MinIOConfig.java" "$SRC_DIR/config/middleware/" 2>/dev/null
fi

# Utils层移动
if [ -d "$SRC_DIR/utils" ]; then
    echo "  移动 Utils 文件..."
    mv "$SRC_DIR/utils/JwtUtils.java" "$SRC_DIR/util/jwt/" 2>/dev/null
    mv "$SRC_DIR/utils/HashUtils.java" "$SRC_DIR/util/encrypt/" 2>/dev/null
fi

# Handler/Interceptor/Aspect层移动
if [ -d "$SRC_DIR/api/handler" ]; then
    mv "$SRC_DIR/api/handler"/* "$SRC_DIR/api/handler/" 2>/dev/null
fi

if [ -d "$SRC_DIR/interceptor" ]; then
    mv "$SRC_DIR/interceptor"/* "$SRC_DIR/api/interceptor/" 2>/dev/null
fi

if [ -d "$SRC_DIR/infrastructure/aspect" ]; then
    mv "$SRC_DIR/infrastructure/aspect"/* "$SRC_DIR/api/aspect/" 2>/dev/null
fi

# Cache层移动
if [ -d "$SRC_DIR/cache" ]; then
    mv "$SRC_DIR/cache"/* "$SRC_DIR/infrastructure/cache/" 2>/dev/null
fi

echo "✅ 文件重组完成"
echo ""

# 生成README
echo "📝 生成项目文档..."

cat > "$PROJECT_ROOT/README_NEW.md" << 'EOL'
# Yoyo Data - 企业级Spring Boot项目模板

## 📋 项目简介

这是一个基于Spring Boot 2.3.12的企业级项目模板，采用DDD分层架构，集成了常用的中间件和工具。

## 🏗️ 技术栈

- **框架**: Spring Boot 2.3.12
- **数据库**: MySQL 8.0 + MongoDB 4.4
- **缓存**: Redis 6.0 + Redisson
- **消息队列**: Kafka
- **对象存储**: MinIO
- **ORM**: MyBatis-Plus 3.5.1
- **认证**: JWT
- **文档**: Swagger 2.9.2

## 📁 项目结构

```
src/main/java/com/example/yoyo_data/
├── api/                    # API层
│   ├── controller/        # 控制器
│   ├── handler/          # 异常处理器
│   ├── interceptor/      # 拦截器
│   └── aspect/           # 切面
├── domain/                # 领域层
│   ├── entity/           # 实体类
│   ├── dto/              # 数据传输对象
│   ├── vo/               # 视图对象
│   └── enums/            # 枚举类
├── application/           # 应用层
│   └── service/          # 服务层
├── infrastructure/        # 基础设施层
│   ├── repository/       # 仓储层
│   ├── cache/            # 缓存层
│   ├── mq/               # 消息队列
│   └── storage/          # 存储服务
├── config/                # 配置层
├── common/                # 通用层
└── util/                  # 工具层
```

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd yoyo_data
```

### 2. 配置环境

修改 `application.yml` 配置文件：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: root
    password: your_password

  redis:
    host: localhost
    port: 6379
    password: your_password

  data:
    mongodb:
      uri: mongodb://localhost:27017/your_database

jwt:
  secret: your-secret-key-at-least-64-bytes
```

### 3. 启动中间件

```bash
# 使用Docker Compose启动所有中间件
docker-compose up -d
```

### 4. 运行项目

```bash
mvn spring-boot:run
```

## 📚 文档

- [企业级架构规范](./ENTERPRISE_ARCHITECTURE.md)
- [中间件使用模板](./MIDDLEWARE_TEMPLATES.md)
- [MongoDB + Redis集成说明](./MONGODB_REDIS_INTEGRATION.md)
- [代码优化总结](./CODE_OPTIMIZATION_SUMMARY.md)
- [JWT修复指南](./JWT_FIX_GUIDE.md)

## 🔧 开发规范

### 代码规范

- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 统一异常处理
- 统一响应格式
- 完整的参数校验

### 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具链相关
```

## 🌟 核心功能

- [x] JWT认证授权
- [x] Redis缓存
- [x] MongoDB文档存储
- [x] MySQL关系数据库
- [x] Kafka消息队列
- [x] MinIO对象存储
- [x] 接口限流
- [x] 全局异常处理
- [x] 统一日志记录
- [x] Swagger API文档

## 📊 性能优化

- Redis多级缓存
- 数据库连接池优化
- 异步任务处理
- 分页查询优化
- 慢查询监控

## 🔐 安全特性

- JWT Token认证
- 密码BCrypt加密
- SQL注入防护
- XSS防护
- 接口权限控制
- 敏感数据脱敏

## 📝 许可证

MIT License

## 👥 贡献

欢迎提交Issue和Pull Request

## 📧 联系方式

如有问题请联系: your-email@example.com
EOL

echo "✅ 文档生成完成"
echo ""

echo "========================================="
echo "  重构完成！"
echo "========================================="
echo ""
echo "📁 新的项目结构已创建"
echo "📦 现有文件已重组"
echo "📝 项目文档已生成 (README_NEW.md)"
echo ""
echo "⚠️  请注意:"
echo "   1. 检查移动后的文件包名是否正确"
echo "   2. 更新import语句"
echo "   3. 运行测试确保功能正常"
echo "   4. 提交前备份原项目"
echo ""
echo "📚 参考文档:"
echo "   - ENTERPRISE_ARCHITECTURE.md"
echo "   - MIDDLEWARE_TEMPLATES.md"
echo ""
