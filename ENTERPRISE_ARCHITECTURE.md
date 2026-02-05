# 企业级Spring Boot项目架构规范

## 📁 项目目录结构

```
yoyo_data/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/yoyo_data/
│   │   │       ├── YoyoDataApplication.java          # 应用启动类
│   │   │       │
│   │   │       ├── api/                              # API层（Controller）
│   │   │       │   ├── controller/                   # 控制器
│   │   │       │   │   ├── auth/                    # 认证相关
│   │   │       │   │   │   ├── AuthController.java
│   │   │       │   │   │   └── UserController.java
│   │   │       │   │   ├── business/               # 业务相关
│   │   │       │   │   │   ├── DialogController.java
│   │   │       │   │   │   ├── TravelPlanController.java
│   │   │       │   │   │   ├── PostController.java
│   │   │       │   │   │   ├── CommentController.java
│   │   │       │   │   │   └── LikeController.java
│   │   │       │   │   ├── system/                 # 系统管理
│   │   │       │   │   │   ├── SystemController.java
│   │   │       │   │   │   └── FileUploadController.java
│   │   │       │   │   └── BaseController.java     # 控制器基类
│   │   │       │   │
│   │   │       │   ├── handler/                     # 异常处理器
│   │   │       │   │   ├── GlobalExceptionHandler.java
│   │   │       │   │   └── ValidationExceptionHandler.java
│   │   │       │   │
│   │   │       │   ├── interceptor/                 # 拦截器
│   │   │       │   │   ├── JwtInterceptor.java
│   │   │       │   │   ├── LogInterceptor.java
│   │   │       │   │   └── RateLimitInterceptor.java
│   │   │       │   │
│   │   │       │   └── aspect/                      # 切面
│   │   │       │       ├── LoggingAspect.java
│   │   │       │       ├── PerformanceAspect.java
│   │   │       │       └── RateLimitAspect.java
│   │   │       │
│   │   │       ├── domain/                           # 领域层
│   │   │       │   ├── entity/                      # 实体类（数据库表对应）
│   │   │       │   │   ├── mysql/                  # MySQL实体
│   │   │       │   │   │   ├── Users.java
│   │   │       │   │   │   ├── UserProfile.java
│   │   │       │   │   │   ├── Post.java
│   │   │       │   │   │   ├── Comment.java
│   │   │       │   │   │   ├── Like.java
│   │   │       │   │   │   ├── Follow.java
│   │   │       │   │   │   ├── Tag.java
│   │   │       │   │   │   ├── Role.java
│   │   │       │   │   │   ├── Permission.java
│   │   │       │   │   │   ├── CaptchaRecord.java
│   │   │       │   │   │   └── AuditLog.java
│   │   │       │   │   │
│   │   │       │   │   └── mongodb/               # MongoDB文档
│   │   │       │   │       ├── DialogSession.java
│   │   │       │   │       └── TravelPlan.java
│   │   │       │   │
│   │   │       │   ├── dto/                        # 数据传输对象
│   │   │       │   │   ├── request/               # 请求DTO
│   │   │       │   │   │   ├── LoginRequest.java
│   │   │       │   │   │   ├── RegisterRequest.java
│   │   │       │   │   │   ├── CreatePostRequest.java
│   │   │       │   │   │   └── UpdatePostRequest.java
│   │   │       │   │   │
│   │   │       │   │   ├── response/              # 响应DTO
│   │   │       │   │   │   ├── LoginResponse.java
│   │   │       │   │   │   ├── TokenResponse.java
│   │   │       │   │   │   ├── DialogSessionDTO.java
│   │   │       │   │   │   ├── TravelPlanDTO.java
│   │   │       │   │   │   └── PageResponseDTO.java
│   │   │       │   │   │
│   │   │       │   │   └── common/                # 通用DTO
│   │   │       │   │       ├── JwtUserDTO.java
│   │   │       │   │       └── FileUploadDTO.java
│   │   │       │   │
│   │   │       │   ├── vo/                         # 视图对象
│   │   │       │   │   ├── UserVO.java
│   │   │       │   │   ├── PostVO.java
│   │   │       │   │   └── CommentVO.java
│   │   │       │   │
│   │   │       │   └── enums/                      # 枚举类
│   │   │       │       ├── UserStatus.java
│   │   │       │       ├── PostStatus.java
│   │   │       │       └── ResultCode.java
│   │   │       │
│   │   │       ├── application/                     # 应用层
│   │   │       │   ├── service/                    # 服务接口
│   │   │       │   │   ├── auth/
│   │   │       │   │   │   ├── AuthService.java
│   │   │       │   │   │   └── UserService.java
│   │   │       │   │   │
│   │   │       │   │   ├── business/
│   │   │       │   │   │   ├── DialogService.java
│   │   │       │   │   │   ├── TravelPlanService.java
│   │   │       │   │   │   ├── PostService.java
│   │   │       │   │   │   ├── CommentService.java
│   │   │       │   │   │   └── LikeService.java
│   │   │       │   │   │
│   │   │       │   │   └── system/
│   │   │       │   │       ├── FileUploadService.java
│   │   │       │   │       └── SystemService.java
│   │   │       │   │
│   │   │       │   └── service/impl/               # 服务实现
│   │   │       │       ├── auth/
│   │   │       │       │   ├── AuthServiceImpl.java
│   │   │       │       │   └── UserServiceImpl.java
│   │   │       │       │
│   │   │       │       ├── business/
│   │   │       │       │   ├── DialogServiceImpl.java
│   │   │       │       │   ├── TravelPlanServiceImpl.java
│   │   │       │       │   ├── PostServiceImpl.java
│   │   │       │       │   ├── CommentServiceImpl.java
│   │   │       │       │   └── LikeServiceImpl.java
│   │   │       │       │
│   │   │       │       └── system/
│   │   │       │           ├── FileUploadServiceImpl.java
│   │   │       │           └── SystemServiceImpl.java
│   │   │       │
│   │   │       ├── infrastructure/                  # 基础设施层
│   │   │       │   ├── repository/                 # 仓储层
│   │   │       │   │   ├── mysql/                 # MySQL Mapper
│   │   │       │   │   │   ├── UserMapper.java
│   │   │       │   │   │   ├── PostMapper.java
│   │   │       │   │   │   ├── CommentMapper.java
│   │   │       │   │   │   └── ...
│   │   │       │   │   │
│   │   │       │   │   └── mongodb/              # MongoDB Repository
│   │   │       │   │       ├── DialogSessionRepository.java
│   │   │       │   │       └── TravelPlanRepository.java
│   │   │       │   │
│   │   │       │   ├── cache/                     # 缓存层
│   │   │       │   │   ├── RedisService.java
│   │   │       │   │   ├── RedisServiceImpl.java
│   │   │       │   │   └── CacheKeyConstants.java
│   │   │       │   │
│   │   │       │   ├── mq/                        # 消息队列
│   │   │       │   │   ├── KafkaProducerService.java
│   │   │       │   │   ├── KafkaConsumerService.java
│   │   │       │   │   └── MessageEvent.java
│   │   │       │   │
│   │   │       │   ├── storage/                   # 存储服务
│   │   │       │   │   ├── MinIOService.java
│   │   │       │   │   ├── MinIOServiceImpl.java
│   │   │       │   │   ├── OSSService.java
│   │   │       │   │   └── OSSServiceImpl.java
│   │   │       │   │
│   │   │       │   └── external/                  # 外部服务
│   │   │       │       ├── EmailService.java
│   │   │       │       ├── SmsService.java
│   │   │       │       └── WeChatService.java
│   │   │       │
│   │   │       ├── config/                          # 配置层
│   │   │       │   ├── properties/                # 配置属性
│   │   │       │   │   ├── JwtProperties.java
│   │   │       │   │   ├── RedisProperties.java
│   │   │       │   │   ├── MinIOProperties.java
│   │   │       │   │   ├── KafkaProperties.java
│   │   │       │   │   └── DataSourceProperties.java
│   │   │       │   │
│   │   │       │   ├── database/                  # 数据库配置
│   │   │       │   │   ├��─ MybatisPlusConfig.java
│   │   │       │   │   ├── MongoConfig.java
│   │   │       │   │   └── DataSourceConfig.java
│   │   │       │   │
│   │   │       │   ├── cache/                     # 缓存配置
│   │   │       │   │   ├── RedisConfig.java
│   │   │       │   │   └── RedissonConfig.java
│   │   │       │   │
│   │   │       │   ├── security/                  # 安全配置
│   │   │       │   │   ├── JwtConfig.java
│   │   │       │   │   ├── WebMvcConfig.java
│   │   │       │   │   └── PasswordEncoderConfig.java
│   │   │       │   │
│   │   │       │   ├── middleware/                # 中间件配置
│   │   │       │   │   ├── KafkaConfig.java
│   │   │       │   │   ├── MinIOConfig.java
│   │   │       │   │   └── SwaggerConfig.java
│   │   │       │   │
│   │   │       │   └── async/                     # 异步配置
│   │   │       │       └── AsyncTaskConfig.java
│   │   │       │
│   │   │       ├── common/                          # 通用模块
│   │   │       │   ├── base/                      # 基础类
│   │   │       │   │   ├── BaseEntity.java
│   │   │       │   │   ├── BaseService.java
│   │   │       │   │   ├── BaseServiceImpl.java
│   │   │       │   │   ├── BaseController.java
│   │   │       │   │   └── BasePage.java
│   │   │       │   │
│   │   │       │   ├── result/                    # 统一响应
│   │   │       │   │   ├── Result.java
│   │   │       │   │   ├── ResultCode.java
│   │   │       │   │   └── ResultBuilder.java
│   │   │       │   │
│   │   │       │   ├── exception/                 # 异常定义
│   │   │       │   │   ├── BusinessException.java
│   │   │       │   │   ├── SystemException.java
│   │   │       │   │   └── ValidationException.java
│   │   │       │   │
│   │   │       │   ├── constant/                  # 常量定义
│   │   │       │   │   ├── CacheKeyConstants.java
│   │   │       │   │   ├── CommonConstants.java
│   │   │       │   │   └── ErrorCodeConstants.java
│   │   │       │   │
│   │   │       │   ├── annotation/                # 自定义注解
│   │   │       │   │   ├── RateLimit.java
│   │   │       │   │   ├── RequirePermission.java
│   │   │       │   │   └── Log.java
│   │   │       │   │
│   │   │       │   └── validator/                 # 校验器
│   │   │       │       ├── PhoneValidator.java
│   │   │       │       ├── EmailValidator.java
│   │   │       │       └── IdCardValidator.java
│   │   │       │
│   │   │       └── utils/                            # 工具类
│   │   │           ├── jwt/                       # JWT工具
│   │   │           │   └── JwtUtils.java
│   │   │           │
│   │   │           ├── encrypt/                   # 加密工具
│   │   │           │   ├── HashUtils.java
│   │   │           │   └── AESUtils.java
│   │   │           │
│   │   │           ├── date/                      # 日期工具
│   │   │           │   └── DateTimeUtils.java
│   │   │           │
│   │   │           ├── collection/                # 集合工具
│   │   │           │   └── CollectionUtils.java
│   │   │           │
│   │   │           ├── json/                      # JSON工具
│   │   │           │   └── JsonUtils.java
│   │   │           │
│   │   │           ├── http/                      # HTTP工具
│   │   │           │   └── HttpUtils.java
│   │   │           │
│   │   │           └── thread/                    # 线程工具
│   │   │               └── ThreadLocalUtils.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml                    # 主配置文件
│   │       ├── application-dev.yml               # 开发环境配置
│   │       ├── application-test.yml              # 测试环境配置
│   │       ├── application-prod.yml              # 生产环境配置
│   │       │
│   │       ├── mapper/                           # MyBatis XML
│   │       │   ├── UserMapper.xml
│   │       │   ├── PostMapper.xml
│   │       │   └── ...
│   │       │
│   │       ├── static/                           # 静态资源
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── images/
│   │       │
│   │       ├── templates/                        # 模板文件
│   │       │   └── email/
│   │       │       ├── welcome.html
│   │       │       └── reset-password.html
│   │       │
│   │       └── logback-spring.xml               # 日志配置
│   │
│   └── test/
│       └── java/
│           └── com/example/yoyo_data/
│               ├── unit/                         # 单元测试
│               │   ├── service/
│               │   └── util/
│               │
│               └── integration/                  # 集成测试
│                   ├── controller/
│                   └── repository/
│
├── docs/                                         # 项目文档
│   ├── API.md                                   # API文档
│   ├── DEPLOYMENT.md                            # 部署文档
│   ├── DATABASE.md                              # 数据库设计文档
│   └── ARCHITECTURE.md                          # 架构设计文档
│
├── scripts/                                      # 脚本文件
│   ├── docker/
│   │   ├── Dockerfile
│   │   └── docker-compose.yml
│   │
│   ├── sql/
│   │   ├── schema.sql                          # 数据库结构
│   │   └── data.sql                            # 初始化数据
│   │
│   └── shell/
│       ├── start.sh                            # 启动脚本
│       └── deploy.sh                           # 部署脚本
│
├── .gitignore
├── pom.xml
├── README.md
└── CHANGELOG.md
```

## 📐 分层架构说明

### 1. API层（Presentation Layer）
- **职责**: 接收HTTP请求，参数校验，调用应用层服务，返回响应
- **包含**: Controller、Interceptor、Handler、Aspect

### 2. 应用层（Application Layer）
- **职责**: 业务逻辑编排，事务控制，调用领域层和基础设施层
- **包含**: Service接口和实现

### 3. 领域层（Domain Layer）
- **职责**: 核心业务模型定义
- **包含**: Entity、DTO、VO、Enum

### 4. 基础设施层（Infrastructure Layer）
- **职责**: 技术实现，数据持久化，第三方服务集成
- **包含**: Repository、Cache、MQ、Storage、External

### 5. 通用层（Common Layer）
- **职责**: 通用功能和工具
- **包含**: Base类、Result、Exception、Constant、Annotation、Validator

### 6. 工具层（Utils Layer）
- **职责**: 无状态的工具方法
- **包含**: 各种Utils

## 🎯 命名规范

### 类命名
- Controller: `XxxController`
- Service接口: `XxxService`
- Service实现: `XxxServiceImpl`
- Repository/Mapper: `XxxRepository` / `XxxMapper`
- Entity: 实体名称（如 `User`, `Post`）
- DTO: `XxxDTO` / `XxxRequest` / `XxxResponse`
- VO: `XxxVO`
- Enum: `XxxEnum` 或直接使用枚举名
- Exception: `XxxException`
- Util: `XxxUtils`

### 方法命名
- 查询单个: `getXxx()` / `findXxx()`
- 查询列表: `listXxx()` / `findXxxList()`
- 分页查询: `pageXxx()`
- 新增: `createXxx()` / `addXxx()` / `insertXxx()`
- 修改: `updateXxx()` / `modifyXxx()`
- 删除: `deleteXxx()` / `removeXxx()`
- 统计: `countXxx()`
- 存在性判断: `existsXxx()` / `hasXxx()`
- 校验: `validateXxx()` / `checkXxx()`

## 📦 依赖关系

```
API层
  ↓ 调用
应用层
  ↓ 调用
领域层 + 基础设施层
  ↓ 使用
通用层 + 工具层
```

## 🔐 安全规范

1. 密码加密存储（BCrypt）
2. JWT Token认证
3. 接口权限控制
4. SQL注入防护
5. XSS防护
6. CSRF防护
7. 敏感数据脱敏

## 🚀 性能优化

1. Redis缓存
2. 数据库连接池
3. 异步任务处理
4. 分页查询
5. 慢查询监控
6. 接口限流

## 📊 监控运维

1. 应用日志（Logback）
2. 访问日志
3. 异常日志
4. 性能监控
5. 健康检查接口
6. Actuator监控

## 📝 开发规范

1. 代码注释完整
2. 统一异常处理
3. 统一响应格式
4. 参数校验
5. 日志记录
6. 单元测试覆盖率 > 70%
