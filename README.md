# 🎨 Yoyo Data 系统

<div align="center">

![Java](https://img.shields.io/badge/Java-1.8%2B-blue?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.3.12-9cf?style=for-the-badge&logo=spring-boot)
![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-orange?style=for-the-badge&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-6.0%2B-red?style=for-the-badge&logo=redis)
![MongoDB](https://img.shields.io/badge/MongoDB-4.4%2B-green?style=for-the-badge&logo=mongodb)
![Kafka](https://img.shields.io/badge/Kafka-2.8%2B-purple?style=for-the-badge&logo=apache-kafka)
![MinIO](https://img.shields.io/badge/MinIO-2023%2B-yellow?style=for-the-badge&logo=minio)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

</div>

## 📖 项目简介

<div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px; border-radius: 10px; color: white; margin: 20px 0;">
  <h3 style="margin-top: 0;">🚀 Yoyo Data</h3>
  <p>Yoyo Data 是一个基于 Spring Boot 的高并发单体项目框架，集成了多种中间件，包括 MySQL、MongoDB、Redis、Kafka 和 MinIO 等，适用于处理高并发场景下的数据业务。</p>
  <div style="display: flex; flex-wrap: wrap; gap: 10px; margin-top: 15px;">
    <span style="background: rgba(255, 255, 255, 0.2); padding: 5px 10px; border-radius: 20px; font-size: 14px;">高性能</span>
    <span style="background: rgba(255, 255, 255, 0.2); padding: 5px 10px; border-radius: 20px; font-size: 14px;">可扩展</span>
    <span style="background: rgba(255, 255, 255, 0.2); padding: 5px 10px; border-radius: 20px; font-size: 14px;">安全可靠</span>
    <span style="background: rgba(255, 255, 255, 0.2); padding: 5px 10px; border-radius: 20px; font-size: 14px;">功能丰富</span>
    <span style="background: rgba(255, 255, 255, 0.2); padding: 5px 10px; border-radius: 20px; font-size: 14px;">易于部署</span>
  </div>
</div>

### ✨ 核心特性

- **高性能**：采用多级缓存、异步处理等优化策略
- **可扩展**：模块化设计，易于扩展和维护
- **安全可靠**：完善的认证授权和安全防护机制
- **功能丰富**：集成多种中间件，满足各种业务需求
- **易于部署**：支持容器化部署和集群扩展

## 🏗️ 系统架构设计

<div style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); padding: 15px; border-radius: 10px; color: white; margin: 20px 0;">
  <h3 style="margin-top: 0;">📐 分层架构设计</h3>
  <p>系统采用清晰的分层架构，分离各层职责，提高系统的可维护性和扩展性。</p>
</div>

### 整体架构

```
┌───────────────────────────────────────────────────────────────────────────┐
│                                接入层 🎯                                  │
├──────────────┬──────────────┬──────────────┬──────────────────────────────┤
│   API接口    │  认证授权    │  流量控制    │           负载均衡           │
├──────────────┼──────────────┼──────────────┼──────────────────────────────┤
│  Controller  │  Security   │  RateLimit   │       外部负载均衡器         │
└──────────────┴──────────────┴──────────────┴──────────────────────────────┘
                              │
┌─────────────────────────────▼─────────────────────────────────────────────┐
│                              业务层 🏭                                   │
├──────────────┬──────────────┬──────────────┬──────────────┬──────────────┤
│  业务服务    │  事务管理    │  业务规则    │  事件处理    │  异步任务    │
├──────────────┼──────────────┼──────────────┼──────────────┼──────────────┤
│   Service    │  Transaction │  BusinessRule│  EventHandler│  AsyncTask   │
└──────────────┴──────────────┴──────────────┴──────────────┴──────────────┘
                              │
┌─────────────────────────────▼─────────────────────────────────────────────┐
│                              数据层 💾                                   │
├──────────────┬──────────────┬──────────────┬──────────────┬──────────────┤
│  数据访问    │  缓存管理    │  消息队列    │  文件存储    │  数据同步    │
├──────────────┼──────────────┼──────────────┼──────────────┼──────────────┤
│   Repository │   Cache      │   Kafka      │   MinIO      │   SyncTask   │
└──────────────┴──────────────┴──────────────┴──────────────┴──────────────┘
                              │
┌─────────────────────────────▼─────────────────────────────────────────────┐
│                              基础设施层 🔧                               │
├──────────────┬──────────────┬──────────────┬──────────────┬──────────────┤
│  配置管理    │  监控告警    │  日志管理    │  工具类      │  连接池管理  │
├──────────────┼──────────────┼──────────────┼──────────────┼──────────────┤
│  Config      │  Monitoring  │  Logging     │  Utils       │  ConnectionPool│
└──────────────┴──────────────┴──────────────┴──────────────┴──────────────┘
                              │
┌─────────────────────────────▼─────────────────────────────────────────────┐
│                              中间件层 🛠️                                   │
├──────────────┬──────────────┬──────────────┬──────────────┬──────────────┤
│   MySQL      │   MongoDB    │   Redis      │   Kafka      │   MinIO      │
└──────────────┴──────────────┴──────────────┴──────────────┴──────────────┘
```

### 设计原则

<div style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); padding: 15px; border-radius: 10px; color: white; margin: 20px 0;">
  <h3 style="margin-top: 0;">🎯 核心设计理念</h3>
  <p>系统设计遵循以下原则，确保系统的高性能、可靠性和可扩展性。</p>
</div>

<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px; margin: 20px 0;">
  <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0;">🏗️ 分层架构</h4>
    <p>清晰的职责分离，便于维护和扩展</p>
  </div>
  <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0;">🔗 高内聚低耦合</h4>
    <p>模块间依赖最小化，提高系统稳定性</p>
  </div>
  <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0;">⚡ 高并发优化</h4>
    <p>通过缓存、异步处理、连接池等手段提升并发能力</p>
  </div>
  <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0;">🔍 可观测性</h4>
    <p>完善的监控、日志和告警机制</p>
  </div>
  <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0;">🔒 安全性</h4>
    <p>包含认证、授权、加密等安全措施</p>
  </div>
  <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0;">📈 可扩展性</h4>
    <p>模块化设计，支持水平扩展</p>
  </div>
  <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0;">🛡️ 可靠性</h4>
    <p>完善的错误处理和容错机制</p>
  </div>
</div>

## 📁 目录结构

```
yoyo_data/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── yoyo_data/
│   │   │               ├── YoyoDataApplication.java        # 应用启动类
│   │   │               ├── api/                         # 接入层
│   │   │               │   ├── controller/              # 控制器
│   │   │               │   ├── handler/                 # 异常处理器
│   │   │               │   ├── interceptor/             # 拦截器
│   │   │               │   └── validator/               # 数据校验
│   │   │               ├── business/                    # 业务层
│   │   │               │   ├── service/                 # 业务服务
│   │   │               │   ├── event/                   # 事件处理
│   │   │               │   ├── async/                   # 异步任务
│   │   │               │   └── rule/                    # 业务规则
│   │   │               ├── data/                        # 数据层
│   │   │               │   ├── repository/              # 数据访问
│   │   │               │   ├── cache/                   # 缓存管理
│   │   │               │   ├── message/                 # 消息队列
│   │   │               │   └── storage/                 # 文件存储
│   │   │               ├── domain/                      # 领域模型
│   │   │               │   ├── entity/                  # 实体类
│   │   │               │   ├── vo/                      # 视图对象
│   │   │               │   └── enums/                   # 枚举类
│   │   │               ├── infrastructure/              # 基础设施层
│   │   │               │   ├── config/                  # 配置管理
│   │   │               │   ├── monitor/                 # 监控告警
│   │   │               │   ├── logging/                 # 日志管理
│   │   │               │   ├── security/                # 安全管理
│   │   │               │   └── utils/                   # 工具类
│   │   │               ├── support/                     # 支持层
│   │   │               │   ├── constant/                # 常量定义
│   │   │               │   ├── dto/                     # 数据传输对象
│   │   │               │   ├── exception/               # 异常定义
│   │   │               │   ├── response/                # 响应封装
│   │   │               │   └── vo/                      # 视图对象
│   │   │               └── jwt/                         # JWT相关
│   │   └── resources/
│   │       ├── static/                                  # 静态资源
│   │       ├── mapper/                                  # MyBatis 映射文件
│   │       ├── application.yml                         # 主配置文件
│   │       ├── application-dev.yml                     # 开发环境配置
│   │       └── application-prod.yml                    # 生产环境配置
│   └── test/                                            # 测试代码
├── pom.xml                                              # Maven 依赖
├── README.md                                            # 项目说明
├── ARCHITECTURE_DESIGN.md                               # 架构设计文档
└── .gitignore                                           # Git 忽略文件
```

## 🛠️ 技术栈

<div style="background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); padding: 15px; border-radius: 10px; color: #333; margin: 20px 0;">
  <h3 style="margin-top: 0;">🔧 技术选型</h3>
  <p>系统采用主流的技术栈，确保高性能、可靠性和可维护性。</p>
</div>

<div style="overflow-x: auto; margin: 20px 0;">
  <table style="width: 100%; border-collapse: collapse; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <thead style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;">
      <tr>
        <th style="padding: 12px; text-align: left;">类别</th>
        <th style="padding: 12px; text-align: left;">技术</th>
        <th style="padding: 12px; text-align: left;">版本</th>
        <th style="padding: 12px; text-align: left;">用途</th>
      </tr>
    </thead>
    <tbody>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">基础框架</td>
        <td style="padding: 12px;">Spring Boot</td>
        <td style="padding: 12px;">2.3.12</td>
        <td style="padding: 12px;">应用基础框架</td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">数据库</td>
        <td style="padding: 12px;">MySQL</td>
        <td style="padding: 12px;">8.0+</td>
        <td style="padding: 12px;">关系型数据存储</td>
      </tr>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">数据库</td>
        <td style="padding: 12px;">MongoDB</td>
        <td style="padding: 12px;">4.4+</td>
        <td style="padding: 12px;">非关系型数据存储</td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">缓存</td>
        <td style="padding: 12px;">Redis</td>
        <td style="padding: 12px;">6.0+</td>
        <td style="padding: 12px;">分布式缓存</td>
      </tr>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">消息队列</td>
        <td style="padding: 12px;">Kafka</td>
        <td style="padding: 12px;">2.8+</td>
        <td style="padding: 12px;">异步消息处理</td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">对象存储</td>
        <td style="padding: 12px;">MinIO</td>
        <td style="padding: 12px;">2023+</td>
        <td style="padding: 12px;">文件存储服务</td>
      </tr>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">ORM 框架</td>
        <td style="padding: 12px;">MyBatis-Plus</td>
        <td style="padding: 12px;">3.5.1</td>
        <td style="padding: 12px;">数据库访问</td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">安全</td>
        <td style="padding: 12px;">JWT</td>
        <td style="padding: 12px;">0.9.1</td>
        <td style="padding: 12px;">认证授权</td>
      </tr>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">工具类</td>
        <td style="padding: 12px;">Hutool</td>
        <td style="padding: 12px;">5.8.38</td>
        <td style="padding: 12px;">通用工具类</td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">工具类</td>
        <td style="padding: 12px;">Lombok</td>
        <td style="padding: 12px;">1.18+</td>
        <td style="padding: 12px;">代码简化</td>
      </tr>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">JSON 处理</td>
        <td style="padding: 12px;">FastJSON</td>
        <td style="padding: 12px;">1.2.83</td>
        <td style="padding: 12px;">JSON 序列化/反序列化</td>
      </tr>
      <tr style="background: white;">
        <td style="padding: 12px; font-weight: bold;">连接池</td>
        <td style="padding: 12px;">Druid</td>
        <td style="padding: 12px;">1.2.18</td>
        <td style="padding: 12px;">数据库连接池</td>
      </tr>
    </tbody>
  </table>
</div>

## 🚀 快速开始

<div style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); padding: 15px; border-radius: 10px; color: white; margin: 20px 0;">
  <h3 style="margin-top: 0;">🎯 开始使用</h3>
  <p>按照以下步骤快速启动和运行系统。</p>
</div>

### 环境要求

<div style="overflow-x: auto; margin: 20px 0;">
  <table style="width: 100%; border-collapse: collapse; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <thead style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white;">
      <tr>
        <th style="padding: 12px; text-align: left;">依赖</th>
        <th style="padding: 12px; text-align: left;">版本</th>
        <th style="padding: 12px; text-align: left;">安装说明</th>
      </tr>
    </thead>
    <tbody>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">JDK</td>
        <td style="padding: 12px;">1.8+</td>
        <td style="padding: 12px;"><a href="https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html" style="color: #667eea; text-decoration: none;">Oracle JDK</a> 或 <a href="https://openjdk.java.net/" style="color: #667eea; text-decoration: none;">OpenJDK</a></td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">Maven</td>
        <td style="padding: 12px;">3.6+</td>
        <td style="padding: 12px;"><a href="https://maven.apache.org/download.cgi" style="color: #667eea; text-decoration: none;">Maven 下载</a></td>
      </tr>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">MySQL</td>
        <td style="padding: 12px;">8.0+</td>
        <td style="padding: 12px;"><a href="https://dev.mysql.com/doc/refman/8.0/en/installing.html" style="color: #667eea; text-decoration: none;">MySQL 安装</a></td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">MongoDB</td>
        <td style="padding: 12px;">4.4+</td>
        <td style="padding: 12px;"><a href="https://docs.mongodb.com/manual/installation/" style="color: #667eea; text-decoration: none;">MongoDB 安装</a></td>
      </tr>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">Redis</td>
        <td style="padding: 12px;">6.0+</td>
        <td style="padding: 12px;"><a href="https://redis.io/download" style="color: #667eea; text-decoration: none;">Redis 安装</a></td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">Kafka</td>
        <td style="padding: 12px;">2.8+</td>
        <td style="padding: 12px;"><a href="https://kafka.apache.org/quickstart" style="color: #667eea; text-decoration: none;">Kafka 安装</a></td>
      </tr>
      <tr style="background: #f8f9fa;">
        <td style="padding: 12px; font-weight: bold;">MinIO</td>
        <td style="padding: 12px;">2023+</td>
        <td style="padding: 12px;"><a href="https://min.io/docs/minio/linux/index.html" style="color: #667eea; text-decoration: none;">MinIO 安装</a></td>
      </tr>
    </tbody>
  </table>
</div>

### 安装与运行

<div style="display: flex; flex-wrap: wrap; gap: 20px; margin: 20px 0;">
  <div style="flex: 1; min-width: 300px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px; border-radius: 10px; color: white;">
    <h4 style="margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">📁</span> 1. 克隆项目</h4>
    <div style="background: rgba(255, 255, 255, 0.1); padding: 15px; border-radius: 8px; font-family: monospace;">
      <code>git clone &lt;项目地址&gt;</code><br>
      <code>cd yoyo_data</code>
    </div>
  </div>
  
  <div style="flex: 1; min-width: 300px; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); padding: 20px; border-radius: 10px; color: white;">
    <h4 style="margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">⚙️</span> 2. 配置环境</h4>
    <p>修改 <code>src/main/resources/application.yml</code> 文件，配置数据库连接、Redis、Kafka 等中间件的连接信息。</p>
  </div>
  
  <div style="flex: 1; min-width: 300px; background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); padding: 20px; border-radius: 10px; color: white;">
    <h4 style="margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">🔨</span> 3. 编译项目</h4>
    <div style="background: rgba(255, 255, 255, 0.1); padding: 15px; border-radius: 8px; font-family: monospace;">
      <code>mvn clean install</code>
    </div>
  </div>
  
  <div style="flex: 1; min-width: 300px; background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); padding: 20px; border-radius: 10px; color: white;">
    <h4 style="margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">🚀</span> 4. 运行项目</h4>
    <div style="background: rgba(255, 255, 255, 0.1); padding: 15px; border-radius: 8px; font-family: monospace;">
      <code>mvn spring-boot:run</code><br><br>
      或使用编译后的 jar 包运行：<br>
      <code>java -jar target/yoyo_data-0.0.1-SNAPSHOT.jar</code>
    </div>
  </div>
  
  <div style="flex: 1; min-width: 300px; background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); padding: 20px; border-radius: 10px; color: #333;">
    <h4 style="margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">🌐</span> 5. 访问系统</h4>
    <p>项目启动后，可通过以下地址访问：</p>
    <ul style="list-style: none; padding: 0;">
      <li style="margin: 5px 0; display: flex; align-items: center;"><span style="margin-right: 10px;">🔗</span> API 接口：<a href="http://localhost:8080/api" style="color: #667eea; text-decoration: none;">http://localhost:8080/api</a></li>
      <li style="margin: 5px 0; display: flex; align-items: center;"><span style="margin-right: 10px;">🔗</span> 健康检查：<a href="http://localhost:8080/actuator/health" style="color: #667eea; text-decoration: none;">http://localhost:8080/actuator/health</a></li>
    </ul>
  </div>
</div>

## ⚙️ 配置说明

<div style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); padding: 15px; border-radius: 10px; color: white; margin: 20px 0;">
  <h3 style="margin-top: 0;">🔧 配置管理</h3>
  <p>系统提供了丰富的配置选项，可根据实际环境进行调整。</p>
</div>

### 核心配置项

<div style="overflow-x: auto; margin: 20px 0;">
  <table style="width: 100%; border-collapse: collapse; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <thead style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white;">
      <tr>
        <th style="padding: 12px; text-align: left;">配置项</th>
        <th style="padding: 12px; text-align: left;">说明</th>
        <th style="padding: 12px; text-align: left;">默认值</th>
      </tr>
    </thead>
    <tbody>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">spring.datasource</td>
        <td style="padding: 12px;">数据库连接配置</td>
        <td style="padding: 12px;">-</td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">spring.redis</td>
        <td style="padding: 12px;">Redis 连接配置</td>
        <td style="padding: 12px;">-</td>
      </tr>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">spring.data.mongodb</td>
        <td style="padding: 12px;">MongoDB 连接配置</td>
        <td style="padding: 12px;">-</td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">spring.kafka</td>
        <td style="padding: 12px;">Kafka 连接配置</td>
        <td style="padding: 12px;">-</td>
      </tr>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">minio</td>
        <td style="padding: 12px;">MinIO 连接配置</td>
        <td style="padding: 12px;">-</td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">jwt.secret</td>
        <td style="padding: 12px;">JWT 密钥</td>
        <td style="padding: 12px;">mySecretKeyForJwtTokenGenerationAndValidation1234567890</td>
      </tr>
      <tr style="background: #f8f9fa; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">jwt.expiration</td>
        <td style="padding: 12px;">JWT 过期时间(毫秒)</td>
        <td style="padding: 12px;">7200000</td>
      </tr>
      <tr style="background: white; border-bottom: 1px solid #e9ecef;">
        <td style="padding: 12px; font-weight: bold;">server.port</td>
        <td style="padding: 12px;">服务器端口</td>
        <td style="padding: 12px;">8080</td>
      </tr>
      <tr style="background: #f8f9fa;">
        <td style="padding: 12px; font-weight: bold;">server.servlet.context-path</td>
        <td style="padding: 12px;">上下文路径</td>
        <td style="padding: 12px;">/api</td>
      </tr>
    </tbody>
  </table>
</div>

### 环境配置

<div style="background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); padding: 15px; border-radius: 10px; color: #333; margin: 20px 0;">
  <h3 style="margin-top: 0;">🌍 多环境支持</h3>
  <p>项目支持多环境配置，通过 <code>spring.profiles.active</code> 切换不同环境的配置。</p>
</div>

<div style="display: flex; flex-wrap: wrap; gap: 20px; margin: 20px 0;">
  <div style="flex: 1; min-width: 300px; background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">🧪</span> 开发环境</h4>
    <p>配置文件：<code>application-dev.yml</code></p>
    <p>适用于本地开发和测试，包含详细的日志和调试信息。</p>
  </div>
  <div style="flex: 1; min-width: 300px; background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #f5576c; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">🚀</span> 生产环境</h4>
    <p>配置文件：<code>application-prod.yml</code></p>
    <p>适用于生产部署，包含优化的性能配置和安全设置。</p>
  </div>
</div>

## 📋 系统功能

<div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 15px; border-radius: 10px; color: white; margin: 20px 0;">
  <h3 style="margin-top: 0;">🌟 核心功能</h3>
  <p>系统提供了丰富的功能模块，满足各种业务需求。</p>
</div>

<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin: 20px 0;">
  <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #667eea; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">🔐</span> 用户认证与授权</h4>
    <p>JWT 令牌认证，基于角色的访问控制</p>
    <p style="font-size: 14px; color: #666; margin-top: 10px;">技术实现：Spring Security + JWT</p>
  </div>
  <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #667eea; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">💾</span> 数据管理</h4>
    <p>支持 MySQL 和 MongoDB 的数据操作</p>
    <p style="font-size: 14px; color: #666; margin-top: 10px;">技术实现：MyBatis-Plus + Spring Data MongoDB</p>
  </div>
  <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #667eea; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">⚡</span> 缓存管理</h4>
    <p>多级缓存策略，提升系统性能</p>
    <p style="font-size: 14px; color: #666; margin-top: 10px;">技术实现：Redis + 本地缓存</p>
  </div>
  <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #667eea; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">📨</span> 消息处理</h4>
    <p>基于 Kafka 的异步消息处理</p>
    <p style="font-size: 14px; color: #666; margin-top: 10px;">技术实现：Spring Kafka</p>
  </div>
  <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #667eea; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">📁</span> 文件存储</h4>
    <p>集成 MinIO 实现对象存储</p>
    <p style="font-size: 14px; color: #666; margin-top: 10px;">技术实现：MinIO Java SDK</p>
  </div>
  <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #667eea; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">🚀</span> 高并发处理</h4>
    <p>通过各种优化策略支持高并发场景</p>
    <p style="font-size: 14px; color: #666; margin-top: 10px;">技术实现：缓存 + 异步 + 连接池优化</p>
  </div>
  <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #667eea; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">🔍</span> 监控告警</h4>
    <p>系统健康检查和指标监控</p>
    <p style="font-size: 14px; color: #666; margin-top: 10px;">技术实现：Spring Boot Actuator</p>
  </div>
  <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #667eea; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">📝</span> 日志管理</h4>
    <p>标准化的日志记录和管理</p>
    <p style="font-size: 14px; color: #666; margin-top: 10px;">技术实现：SLF4J + Logback</p>
  </div>
</div>

### API 接口

<div style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); padding: 15px; border-radius: 10px; color: white; margin: 20px 0;">
  <h3 style="margin-top: 0;">🌐 RESTful API</h3>
  <p>系统提供标准化的 RESTful API 接口，支持各种客户端调用。</p>
</div>

<div style="display: flex; flex-wrap: wrap; gap: 20px; margin: 20px 0;">
  <div style="flex: 1; min-width: 300px; background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">👤</span> 用户管理</h4>
    <ul style="list-style: none; padding: 0;">
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #667eea; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">POST</span>
        <code>/api/auth/login</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 用户登录</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #667eea; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">POST</span>
        <code>/api/auth/register</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 用户注册</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #4facfe; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">GET</span>
        <code>/api/auth/info</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 获取用户信息</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #f5576c; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">PUT</span>
        <code>/api/auth/update</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 更新用户信息</span>
      </li>
    </ul>
  </div>
  
  <div style="flex: 1; min-width: 300px; background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">📊</span> 数据管理</h4>
    <ul style="list-style: none; padding: 0;">
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #4facfe; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">GET</span>
        <code>/api/data/list</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 获取数据列表</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #4facfe; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">GET</span>
        <code>/api/data/detail/{id}</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 获取数据详情</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #667eea; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">POST</span>
        <code>/api/data/create</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 创建数据</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #f5576c; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">PUT</span>
        <code>/api/data/update/{id}</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 更新数据</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #fa709a; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">DELETE</span>
        <code>/api/data/delete/{id}</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 删除数据</span>
      </li>
    </ul>
  </div>
  
  <div style="flex: 1; min-width: 300px; background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">📁</span> 文件管理</h4>
    <ul style="list-style: none; padding: 0;">
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #667eea; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">POST</span>
        <code>/api/file/upload</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 上传文件</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #4facfe; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">GET</span>
        <code>/api/file/download/{id}</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 下载文件</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #fa709a; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">DELETE</span>
        <code>/api/file/delete/{id}</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 删除文件</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #4facfe; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">GET</span>
        <code>/api/file/list</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 获取文件列表</span>
      </li>
    </ul>
  </div>
  
  <div style="flex: 1; min-width: 300px; background: #f8f9fa; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
    <h4 style="color: #4facfe; margin-top: 0; display: flex; align-items: center;"><span style="margin-right: 10px;">⚙️</span> 系统管理</h4>
    <ul style="list-style: none; padding: 0;">
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #4facfe; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">GET</span>
        <code>/api/system/health</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 系统健康检查</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #4facfe; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">GET</span>
        <code>/api/system/metrics</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 系统指标</span>
      </li>
      <li style="margin: 10px 0; display: flex; align-items: center;">
        <span style="background: #4facfe; color: white; padding: 2px 8px; border-radius: 12px; font-size: 12px; margin-right: 10px;">GET</span>
        <code>/api/system/config</code>
        <span style="margin-left: 10px; color: #666; font-size: 14px;">- 系统配置</span>
      </li>
    </ul>
  </div>
</div>

## ⚡ 高并发优化策略

### 1. 缓存策略

- **多级缓存**：本地缓存 + Redis 分布式缓存
- **缓存预热**：系统启动时加载热点数据
- **缓存更新**：异步更新或延迟双删策略
- **缓存降级**：当缓存不可用时，直接访问数据库
- **缓存穿透**：布隆过滤器防止缓存穿透
- **缓存雪崩**：随机过期时间防止缓存雪崩

### 2. 数据库优化

- **连接池优化**：使用 Druid 连接池，合理配置连接数
- **索引优化**：为常用查询字段建立索引
- **SQL 优化**：避免复杂查询，使用分页查询
- **读写分离**：主库写，从库读，提高并发能力
- **分库分表**：对大表进行分库分表，提高查询效率
- **批量操作**：使用批量插入和更新，减少网络开销

### 3. 异步处理

- **消息队列**：使用 Kafka 处理异步任务和事件
- **线程池**：合理配置线程池，处理并发请求
- **CompletableFuture**：使用异步非阻塞编程模式
- **定时任务**：使用 Spring Task 或 Quartz 处理定时任务

### 4. 限流与降级

- **分布式限流**：使用 Redis + Lua 脚本实现分布式限流
- **服务降级**：当系统负载过高时，降级非核心功能
- **熔断机制**：使用 Sentinel 或 Hystrix 实现服务熔断
- **超时控制**：为所有外部调用设置合理的超时时间

### 5. 网络优化

- **HTTP 连接池**：使用 HttpClient 或 OkHttp 连接池
- **CDN**：使用 CDN 加速静态资源访问
- **GZIP 压缩**：启用 HTTP 压缩，减少网络传输量
- **WebSocket**：对于实时通信，使用 WebSocket 替代 HTTP 轮询
- **长连接**：使用 TCP 长连接，减少连接建立开销

## 📊 监控与日志

### 监控

- **Spring Boot Actuator**：提供系统健康检查、指标监控等
- **自定义监控**：针对业务指标的监控
- **告警机制**：当系统指标异常时，触发告警

### 日志

- **SLF4J + Logback**：标准日志框架
- **日志级别**：可通过配置文件调整
- **日志输出**：控制台和文件输出
- **日志归档**：定期归档和清理日志
- **分布式日志**：可集成 ELK 或 Loki 实现分布式日志管理

## 🔒 安全措施

### 1. 认证授权

- **JWT 令牌**：无状态认证，便于水平扩展
- **密码加密**：使用 bcrypt 进行密码加密
- **基于角色的访问控制**：细粒度的权限管理
- **令牌刷新**：支持令牌自动刷新
- **令牌黑名单**：已注销的令牌加入黑名单

### 2. 输入验证

- **参数验证**：使用 Spring Validation 进行参数验证
- **防 SQL 注入**：使用 MyBatis-Plus 防止 SQL 注入
- **防 XSS 攻击**：对输入输出进行过滤
- **防 CSRF 攻击**：实现 CSRF 令牌验证

### 3. 网络安全

- **HTTPS**：生产环境使用 HTTPS
- **CORS 配置**：合理配置跨域资源共享
- **请求限流**：防止恶意请求和 DoS 攻击
- **敏感信息保护**：敏感信息加密存储和传输

## 📦 部署建议

### 单机部署

适用于开发和测试环境：

1. 安装并配置所有中间件
2. 编译打包项目
3. 运行 jar 包

### 生产部署

建议使用以下部署方式：

1. **容器化部署**：
   - 使用 Docker 容器化应用
   - 使用 Docker Compose 管理多容器

2. **集群部署**：
   - 多实例部署，配合负载均衡
   - 使用 Kubernetes 进行容器编排

3. **配置管理**：
   - 使用 Nacos 或 Consul 管理配置
   - 实现配置的动态更新

4. **服务发现**：
   - 实现服务自动发现
   - 支持服务健康检查

5. **监控告警**：
   - 配置完善的监控和告警机制
   - 集成 Prometheus + Grafana

## 🛠️ 开发规范

### 代码规范

- **命名规范**：使用驼峰命名法
- **代码风格**：遵循 Java 代码规范
- **注释规范**：关键代码必须添加注释
- **日志规范**：合理使用日志级别，避免过度日志
- **异常处理**：统一的异常处理机制

### 开发流程

1. **需求分析**：明确功能需求和技术要求
2. **设计**：进行系统设计和数据库设计
3. **编码**：按照规范编写代码
4. **测试**：单元测试、集成测试、性能测试
5. **代码审查**：提交代码前进行代码审查
6. **部署**：部署到测试环境进行验证
7. **上线**：灰度发布到生产环境

## ❓ 常见问题

### 启动失败

- **问题**：项目启动失败
- **原因**：中间件未启动或配置错误
- **解决方案**：
  1. 检查中间件是否正常运行
  2. 检查配置文件是否正确
  3. 检查端口是否被占用
  4. 查看启动日志，定位具体错误

### 性能问题

- **问题**：系统响应慢
- **原因**：数据库查询慢、缓存未生效、并发处理能力不足
- **解决方案**：
  1. 检查数据库索引是否合理
  2. 检查缓存是否有效使用
  3. 检查是否存在慢查询
  4. 检查线程池配置是否合理
  5. 考虑使用异步处理

### 安全问题

- **问题**：系统存在安全漏洞
- **原因**：输入验证不足、密码加密不当、权限控制不严格
- **解决方案**：
  1. 定期更新依赖包，防止安全漏洞
  2. 加强密码策略，使用强加密算法
  3. 完善权限控制，实现最小权限原则
  4. 对输入输出进行严格验证和过滤
  5. 定期进行安全扫描和渗透测试

## 🤝 贡献指南

欢迎贡献代码和提出建议！

1. **Fork 项目**
2. **创建分支**：`git checkout -b feature/your-feature`
3. **提交修改**：`git commit -m 'Add some feature'`
4. **推送到分支**：`git push origin feature/your-feature`
5. **创建 Pull Request**

## 📞 联系方式

- **开发团队**：Yoyo Data Team
- **邮箱**：contact@yoyodata.com
- **GitHub**：https://github.com/yoyodata/yoyo_data
- **文档**：https://docs.yoyodata.com

## 📄 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

---

<div align="center">

**Yoyo Data - 高性能、可扩展的数据业务系统**

</div>