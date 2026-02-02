# Yoyo Data 系统 API 接口文档

## 1. 文档概述

本文档描述了 Yoyo Data 系统的 API 接口设计，基于系统的数据库结构和业务需求。系统采用 RESTful API 设计风格，提供了用户认证、社交互动、旅行计划等功能的接口。

### 1.1 基础信息

- **API 基础路径**: `/api`
- **请求方法**: GET, POST, PUT, DELETE, PATCH
- **响应格式**: JSON
- **认证方式**: JWT Token
- **错误处理**: 统一的错误响应格式

### 1.2 响应格式

#### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2026-02-02T12:00:00"
}
```

#### 错误响应

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "timestamp": "2026-02-02T12:00:00"
}
```

## 2. 认证模块 (Auth)

### 2.1 用户注册

**请求**
- **路径**: `/auth/register`
- **方法**: POST
- **参数**:

```json
{
  "username": "user123",
  "email": "user@example.com",
  "password": "Password123!",
  "phone": "13800138000"
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "phone": "13800138000",
    "role": "user",
    "is_active": true,
    "is_verified": false,
    "created_at": "2026-02-02T12:00:00"
  }
}
```

### 2.2 用户登录

**请求**
- **路径**: `/auth/login`
- **方法**: POST
- **参数**:

```json
{
  "username": "user123",
  "password": "Password123!"
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "user123",
      "email": "user@example.com",
      "role": "user",
      "is_active": true,
      "is_verified": true
    },
    "expires_at": "2026-02-02T15:00:00"
  }
}
```

### 2.3 刷新 Token

**请求**
- **路径**: `/auth/refresh`
- **方法**: POST
- **Headers**:
  - `Authorization: Bearer {token}`

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "Token 刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_at": "2026-02-02T15:00:00"
  }
}
```

### 2.4 登出

**请求**
- **路径**: `/auth/logout`
- **方法**: POST
- **Headers**:
  - `Authorization: Bearer {token}`

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

### 2.5 发送验证码

**请求**
- **路径**: `/auth/send-code`
- **方法**: POST
- **参数**:

```json
{
  "email": "user@example.com",
  "type": "verify_email"  // verify_email, reset_password
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "验证码发送成功",
  "data": {
    "email": "user@example.com",
    "expires_in": 300
  }
}
```

### 2.6 验证邮箱

**请求**
- **路径**: `/auth/verify-email`
- **方法**: POST
- **参数**:

```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "邮箱验证成功",
  "data": {
    "email": "user@example.com",
    "is_verified": true
  }
}
```

## 3. 用户模块 (User)

### 3.1 获取用户信息

**请求**
- **路径**: `/users/me`
- **方法**: GET
- **Headers**:
  - `Authorization: Bearer {token}`

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "phone": "13800138000",
    "avatar_url": "https://example.com/avatar.jpg",
    "bio": "旅行爱好者",
    "role": "user",
    "is_active": true,
    "is_verified": true,
    "created_at": "2026-02-02T12:00:00",
    "last_login_at": "2026-02-02T10:00:00"
  }
}
```

### 3.2 更新用户信息

**请求**
- **路径**: `/users/me`
- **方法**: PUT
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:

```json
{
  "phone": "13900139000",
  "avatar_url": "https://example.com/new-avatar.jpg",
  "bio": "热爱旅行和摄影"
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "phone": "13900139000",
    "avatar_url": "https://example.com/new-avatar.jpg",
    "bio": "热爱旅行和摄影"
  }
}
```

### 3.3 获取用户档案

**请求**
- **路径**: `/users/me/profile`
- **方法**: GET
- **Headers**:
  - `Authorization: Bearer {token}`

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "user_id": 1,
    "full_name": "张三",
    "gender": "male",
    "birth_date": "1990-01-01",
    "location": "北京市",
    "travel_preferences": ["自然风光", "历史文化", "美食"],
    "visited_cities": ["北京", "上海", "杭州"],
    "travel_stats": {
      "total_trips": 10,
      "total_distance": 5000,
      "favorite_country": "中国"
    }
  }
}
```

### 3.4 更新用户档案

**请求**
- **路径**: `/users/me/profile`
- **方法**: PUT
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:

```json
{
  "full_name": "张三",
  "gender": "male",
  "birth_date": "1990-01-01",
  "location": "北京市",
  "travel_preferences": ["自然风光", "历史文化", "美食"],
  "visited_cities": ["北京", "上海", "杭州"]
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "user_id": 1,
    "full_name": "张三",
    "gender": "male",
    "birth_date": "1990-01-01",
    "location": "北京市",
    "travel_preferences": ["自然风光", "历史文化", "美食"],
    "visited_cities": ["北京", "上海", "杭州"]
  }
}
```

### 3.5 获取其他用户信息

**请求**
- **路径**: `/users/{id}`
- **方法**: GET

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 2,
    "username": "otheruser",
    "avatar_url": "https://example.com/avatar2.jpg",
    "bio": "旅行博主",
    "location": "上海市",
    "travel_preferences": ["自然风光", "摄影"],
    "created_at": "2026-01-01T12:00:00"
  }
}
```

## 4. 社交模块 (Social)

### 4.1 帖子管理

#### 4.1.1 创建帖子

**请求**
- **路径**: `/posts`
- **方法**: POST
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:

```json
{
  "title": "北京三日游攻略",
  "content": "北京是中国的首都，有着丰富的历史文化...",
  "media_urls": ["https://example.com/photo1.jpg", "https://example.com/photo2.jpg"],
  "tags": ["北京", "旅行", "攻略"],
  "location": "北京市",
  "status": "published"
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "user_id": 1,
    "title": "北京三日游攻略",
    "content": "北京是中国的首都，有着丰富的历史文化...",
    "media_urls": ["https://example.com/photo1.jpg", "https://example.com/photo2.jpg"],
    "tags": ["北京", "旅行", "攻略"],
    "location": "北京市",
    "status": "published",
    "view_count": 0,
    "like_count": 0,
    "comment_count": 0,
    "created_at": "2026-02-02T12:00:00",
    "published_at": "2026-02-02T12:00:00"
  }
}
```

#### 4.1.2 获取帖子列表

**请求**
- **路径**: `/posts`
- **方法**: GET
- **参数**:
  - `page`: 页码，默认 1
  - `size`: 每页数量，默认 10
  - `status`: 状态，可选 published, draft
  - `tag`: 标签筛选
  - `location`: 位置筛选
  - `sort`: 排序方式，默认 created_at

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "items": [
      {
        "id": 1,
        "user_id": 1,
        "username": "user123",
        "title": "北京三日游攻略",
        "content": "北京是中国的首都，有着丰富的历史文化...",
        "media_urls": ["https://example.com/photo1.jpg"],
        "tags": ["北京", "旅行", "攻略"],
        "location": "北京市",
        "view_count": 100,
        "like_count": 20,
        "comment_count": 5,
        "created_at": "2026-02-02T12:00:00",
        "published_at": "2026-02-02T12:00:00"
      }
    ]
  }
}
```

#### 4.1.3 获取帖子详情

**请求**
- **路径**: `/posts/{id}`
- **方法**: GET

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "user_id": 1,
    "username": "user123",
    "avatar_url": "https://example.com/avatar.jpg",
    "title": "北京三日游攻略",
    "content": "北京是中国的首都，有着丰富的历史文化...",
    "media_urls": ["https://example.com/photo1.jpg", "https://example.com/photo2.jpg"],
    "tags": ["北京", "旅行", "攻略"],
    "location": "北京市",
    "view_count": 101,
    "like_count": 20,
    "comment_count": 5,
    "created_at": "2026-02-02T12:00:00",
    "updated_at": "2026-02-02T12:30:00",
    "published_at": "2026-02-02T12:00:00",
    "is_liked": false
  }
}
```

#### 4.1.4 更新帖子

**请求**
- **路径**: `/posts/{id}`
- **方法**: PUT
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:

```json
{
  "title": "北京三日游攻略（更新版）",
  "content": "更新后的内容...",
  "tags": ["北京", "旅行", "攻略", "更新"]
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "title": "北京三日游攻略（更新版）",
    "content": "更新后的内容...",
    "tags": ["北京", "旅行", "攻略", "更新"],
    "updated_at": "2026-02-02T13:00:00"
  }
}
```

#### 4.1.5 删除帖子

**请求**
- **路径**: `/posts/{id}`
- **方法**: DELETE
- **Headers**:
  - `Authorization: Bearer {token}`

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": {
    "id": 1,
    "status": "deleted"
  }
}
```

### 4.2 评论管理

#### 4.2.1 创建评论

**请求**
- **路径**: `/posts/{postId}/comments`
- **方法**: POST
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:

```json
{
  "content": "这篇攻略非常详细，谢谢分享！",
  "parent_id": null  // 回复评论时填写父评论ID
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "评论成功",
  "data": {
    "id": 1,
    "post_id": 1,
    "user_id": 1,
    "username": "user123",
    "content": "这篇攻略非常详细，谢谢分享！",
    "parent_id": null,
    "like_count": 0,
    "created_at": "2026-02-02T13:00:00"
  }
}
```

#### 4.2.2 获取评论列表

**请求**
- **路径**: `/posts/{postId}/comments`
- **方法**: GET
- **参数**:
  - `page`: 页码，默认 1
  - `size`: 每页数量，默认 20
  - `sort`: 排序方式，默认 created_at

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 10,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": 1,
        "user_id": 1,
        "username": "user123",
        "avatar_url": "https://example.com/avatar.jpg",
        "content": "这篇攻略非常详细，谢谢分享！",
        "parent_id": null,
        "like_count": 5,
        "created_at": "2026-02-02T13:00:00",
        "replies": [
          {
            "id": 2,
            "user_id": 2,
            "username": "otheruser",
            "content": "不客气，希望对你有帮助！",
            "parent_id": 1,
            "like_count": 2,
            "created_at": "2026-02-02T13:30:00"
          }
        ]
      }
    ]
  }
}
```

#### 4.2.3 删除评论

**请求**
- **路径**: `/comments/{id}`
- **方法**: DELETE
- **Headers**:
  - `Authorization: Bearer {token}`

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": {
    "id": 1,
    "is_deleted": true
  }
}
```

### 4.3 点赞管理

#### 4.3.1 点赞/取消点赞

**请求**
- **路径**: `/likes`
- **方法**: POST
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:

```json
{
  "target_type": "post",  // post, comment
  "target_id": 1
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "target_type": "post",
    "target_id": 1,
    "is_liked": true,
    "like_count": 21
  }
}
```

### 4.4 关注管理

#### 4.4.1 关注/取消关注用户

**请求**
- **路径**: `/follows/{userId}`
- **方法**: POST
- **Headers**:
  - `Authorization: Bearer {token}`

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "关注成功",
  "data": {
    "follower_id": 1,
    "following_id": 2,
    "is_following": true,
    "follower_count": 50,
    "following_count": 30
  }
}
```

#### 4.4.2 获取关注列表

**请求**
- **路径**: `/users/{userId}/followings`
- **方法**: GET
- **参数**:
  - `page`: 页码，默认 1
  - `size`: 每页数量，默认 20

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 30,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": 2,
        "username": "otheruser",
        "avatar_url": "https://example.com/avatar2.jpg",
        "bio": "旅行博主",
        "is_following": true,
        "followed_at": "2026-02-01T12:00:00"
      }
    ]
  }
}
```

#### 4.4.3 获取粉丝列表

**请求**
- **路径**: `/users/{userId}/followers`
- **方法**: GET
- **参数**:
  - `page`: 页码，默认 1
  - `size`: 每页数量，默认 20

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 50,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": 3,
        "username": "fanuser",
        "avatar_url": "https://example.com/avatar3.jpg",
        "bio": "旅行爱好者",
        "is_following": false,
        "followed_at": "2026-02-01T10:00:00"
      }
    ]
  }
}
```

## 5. 旅行计划模块 (TravelPlan)

### 5.1 创建旅行计划

**请求**
- **路径**: `/travel-plans`
- **方法**: POST
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:

```json
{
  "title": "北京三日游",
  "city": "北京市",
  "start_date": "2026-03-01",
  "end_date": "2026-03-03",
  "description": "北京经典三日游行程",
  "days": [
    {
      "day": 1,
      "activities": [
        {
          "time": "09:00",
          "location": "天安门广场",
          "description": "参观天安门广场"
        },
        {
          "time": "10:00",
          "location": "故宫",
          "description": "游览故宫博物院"
        }
      ]
    }
  ],
  "transportation": "地铁+公交",
  "budget": 2000,
  "tags": ["北京", "历史", "文化"]
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "plan_id": "plan_123456",
    "user_id": 1,
    "title": "北京三日游",
    "city": "北京市",
    "start_date": "2026-03-01",
    "end_date": "2026-03-03",
    "created_at": "2026-02-02T14:00:00"
  }
}
```

### 5.2 获取旅行计划列表

**请求**
- **路径**: `/travel-plans`
- **方法**: GET
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:
  - `page`: 页码，默认 1
  - `size`: 每页数量，默认 10
  - `city`: 城市筛选
  - `start_date`: 开始日期
  - `end_date`: 结束日期

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 5,
    "page": 1,
    "size": 10,
    "items": [
      {
        "plan_id": "plan_123456",
        "title": "北京三日游",
        "city": "北京市",
        "start_date": "2026-03-01",
        "end_date": "2026-03-03",
        "days_count": 3,
        "budget": 2000,
        "tags": ["北京", "历史", "文化"],
        "created_at": "2026-02-02T14:00:00"
      }
    ]
  }
}
```

### 5.3 获取旅行计划详情

**请求**
- **路径**: `/travel-plans/{planId}`
- **方法**: GET

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "plan_id": "plan_123456",
    "user_id": 1,
    "username": "user123",
    "title": "北京三日游",
    "city": "北京市",
    "start_date": "2026-03-01",
    "end_date": "2026-03-03",
    "description": "北京经典三日游行程",
    "days": [
      {
        "day": 1,
        "activities": [
          {
            "time": "09:00",
            "location": "天安门广场",
            "description": "参观天安门广场"
          },
          {
            "time": "10:00",
            "location": "故宫",
            "description": "游览故宫博物院"
          }
        ]
      }
    ],
    "transportation": "地铁+公交",
    "budget": 2000,
    "tags": ["北京", "历史", "文化"],
    "created_at": "2026-02-02T14:00:00"
  }
}
```

### 5.4 更新旅行计划

**请求**
- **路径**: `/travel-plans/{planId}`
- **方法**: PUT
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:

```json
{
  "title": "北京三日游（更新版）",
  "budget": 2500,
  "tags": ["北京", "历史", "文化", "美食"]
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "plan_id": "plan_123456",
    "title": "北京三日游（更新版）",
    "budget": 2500,
    "tags": ["北京", "历史", "文化", "美食"],
    "updated_at": "2026-02-02T15:00:00"
  }
}
```

### 5.5 删除旅行计划

**请求**
- **路径**: `/travel-plans/{planId}`
- **方法**: DELETE
- **Headers**:
  - `Authorization: Bearer {token}`

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": {
    "plan_id": "plan_123456",
    "status": "deleted"
  }
}
```

## 6. 对话模块 (Dialog)

### 6.1 创建对话会话

**请求**
- **路径**: `/dialogs`
- **方法**: POST
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:

```json
{
  "title": "旅行咨询",
  "initial_message": "我想去北京旅游，有什么推荐的景点吗？"
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "session_id": "session_123456",
    "user_id": 1,
    "title": "旅行咨询",
    "status": "active",
    "created_at": "2026-02-02T16:00:00",
    "last_message_at": "2026-02-02T16:00:00"
  }
}
```

### 6.2 发送消息

**请求**
- **路径**: `/dialogs/{sessionId}/messages`
- **方法**: POST
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:

```json
{
  "content": "我想了解北京的美食推荐",
  "message_type": "text"  // text, image, file
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "session_id": "session_123456",
    "message_id": "msg_123456",
    "user_id": 1,
    "content": "我想了解北京的美食推荐",
    "message_type": "text",
    "created_at": "2026-02-02T16:05:00",
    "response": {
      "content": "北京有很多著名的美食，比如烤鸭、炸酱面、豆汁儿等...",
      "message_type": "text",
      "created_at": "2026-02-02T16:06:00"
    }
  }
}
```

### 6.3 获取对话会话列表

**请求**
- **路径**: `/dialogs`
- **方法**: GET
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:
  - `page`: 页码，默认 1
  - `size`: 每页数量，默认 10
  - `status`: 状态，可选 active, closed

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 5,
    "page": 1,
    "size": 10,
    "items": [
      {
        "session_id": "session_123456",
        "title": "旅行咨询",
        "status": "active",
        "last_message": "我想了解北京的美食推荐",
        "last_message_at": "2026-02-02T16:05:00",
        "message_count": 2
      }
    ]
  }
}
```

### 6.4 获取对话历史

**请求**
- **路径**: `/dialogs/{sessionId}/messages`
- **方法**: GET
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:
  - `page`: 页码，默认 1
  - `size`: 每页数量，默认 50

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 2,
    "page": 1,
    "size": 50,
    "items": [
      {
        "message_id": "msg_123456",
        "user_id": 1,
        "role": "user",
        "content": "我想了解北京的美食推荐",
        "message_type": "text",
        "created_at": "2026-02-02T16:05:00"
      },
      {
        "message_id": "msg_789012",
        "user_id": null,
        "role": "assistant",
        "content": "北京有很多著名的美食，比如烤鸭、炸酱面、豆汁儿等...",
        "message_type": "text",
        "created_at": "2026-02-02T16:06:00"
      }
    ]
  }
}
```

### 6.5 关闭对话会话

**请求**
- **路径**: `/dialogs/{sessionId}/close`
- **方法**: POST
- **Headers**:
  - `Authorization: Bearer {token}`

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "关闭成功",
  "data": {
    "session_id": "session_123456",
    "status": "closed",
    "closed_at": "2026-02-02T16:10:00"
  }
}
```

## 7. 系统模块 (System)

### 7.1 验证码管理

#### 7.1.1 获取图片验证码

**请求**
- **路径**: `/system/captcha`
- **方法**: GET
- **参数**:
  - `session_id`: 会话ID

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "session_id": "session_123",
    "image_url": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "expires_in": 300
  }
}
```

#### 7.1.2 验证验证码

**请求**
- **路径**: `/system/captcha/verify`
- **方法**: POST
- **参数**:

```json
{
  "session_id": "session_123",
  "captcha_code": "123456"
}
```

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "验证成功",
  "data": {
    "session_id": "session_123",
    "is_verified": true
  }
}
```

### 7.2 系统状态

#### 7.2.1 获取系统健康状态

**请求**
- **路径**: `/system/health`
- **方法**: GET

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "status": "UP",
    "components": {
      "database": "UP",
      "redis": "UP",
      "mongodb": "UP",
      "kafka": "UP",
      "minio": "UP"
    },
    "uptime": "24h 30m 45s",
    "version": "1.0.0"
  }
}
```

### 7.3 审计日志

#### 7.3.1 获取审计日志（管理员）

**请求**
- **路径**: `/system/audit-logs`
- **方法**: GET
- **Headers**:
  - `Authorization: Bearer {token}`
- **参数**:
  - `page`: 页码，默认 1
  - `size`: 每页数量，默认 20
  - `user_id`: 用户ID筛选
  - `action`: 操作类型筛选
  - `start_date`: 开始日期
  - `end_date`: 结束日期

**响应**
- **成功**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 100,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": 1,
        "user_id": 1,
        "username": "user123",
        "action": "login",
        "resource": "auth",
        "resource_id": null,
        "details": {"ip": "192.168.1.1", "user_agent": "Mozilla/5.0"},
        "ip_address": "192.168.1.1",
        "created_at": "2026-02-02T12:00:00"
      }
    ]
  }
}
```

## 8. 错误码说明

| 错误码 | 描述 | 含义 |
|--------|------|------|
| 200 | OK | 操作成功 |
| 400 | Bad Request | 请求参数错误 |
| 401 | Unauthorized | 未授权，需要登录 |
| 403 | Forbidden | 禁止访问，权限不足 |
| 404 | Not Found | 资源不存在 |
| 405 | Method Not Allowed | 请求方法不允许 |
| 409 | Conflict | 资源冲突 |
| 429 | Too Many Requests | 请求过于频繁，被限流 |
| 500 | Internal Server Error | 服务器内部错误 |
| 502 | Bad Gateway | 网关错误 |
| 503 | Service Unavailable | 服务不可用 |
| 504 | Gateway Timeout | 网关超时 |

## 9. 认证与授权

### 9.1 JWT Token 使用

系统使用 JWT (JSON Web Token) 进行认证，客户端需要在请求头中携带 Token：

```
Authorization: Bearer {token}
```

Token 有效期默认为 2 小时，过期后需要重新登录或刷新 Token。

### 9.2 权限控制

系统基于角色的访问控制 (RBAC) 模型，主要角色包括：

- **user**: 普通用户，可使用基本功能
- **admin**: 管理员，可使用所有功能

## 10. 接口版本控制

系统采用 URL 路径版本控制，当前版本为 v1：

```
/api/v1/...
```

后续版本将通过修改 URL 路径中的版本号进行区分。

## 11. 速率限制

为防止滥用，系统对 API 请求实施速率限制：

- 普通用户：60 请求/分钟
- 认证用户：300 请求/分钟
- 管理员：1000 请求/分钟

## 12. 最佳实践

### 12.1 请求规范

- 使用 HTTPS 协议
- 正确设置 Content-Type 头
- 携带必要的认证信息
- 合理设置请求超时时间
- 对敏感操作使用 POST 请求

### 12.2 响应处理

- 检查响应状态码
- 正确处理错误响应
- 对分页接口进行合理的分页处理
- 缓存频繁访问的资源

### 12.3 安全性

- 不要在请求中包含敏感信息
- 使用 HTTPS 保护数据传输
- 定期更新 Token
- 避免使用固定的 Token
- 对 API 调用进行日志记录

## 13. 总结

本文档描述了 Yoyo Data 系统的 API 接口设计，涵盖了用户认证、社交互动、旅行计划等核心功能。系统采用 RESTful API 设计风格，提供了丰富的接口以满足各种业务需求。

随着系统的发展和业务需求的变化，API 接口可能会进行调整和扩展，建议开发者参考最新的 API 文档。