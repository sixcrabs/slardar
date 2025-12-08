# Oauth

## structure

- oauth-server: oauth2 授权服务封装
- oauth-client: oauth 客户端封装，用于对接各类 oauth2 认证（包含第三方的微博、gitee、qq等平台）

slardar 框架支持 oauth2.1 标准的认证授权.

授权方式支持：

- 授权码模式
- client credentials 模式

## quickstart

需要先搭建一个 oauth2 授权server， 新建一个 springboot 工程，引入以下依赖：

```xml

<dependency>
    <groupId>org.winterfell</groupId>
    <artifactId>slardar-oauth-server</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>org.winterfell</groupId>
    <artifactId>slardar-starter</artifactId>
    <version>1.7.0-SNAPSHOT</version>
</dependency>
```

实现对应接口： 
- `ClientProvider`: 用于获取到认证的客户端信息
- `AccountProvider`： 用于获取oauth server 的用户信息（即 user 信息）
- `AuditLogIngest`: 审计日志的保存

> 注： 以上实现类需要注入到 `Ioc` 容器中.

WIP...

## 对接其他第三方认证平台
 `oauth-client` 对第三方认证平台进行了一系列封装，可方便的在应用服务里对第三方认证平台进行对接，考虑更多的扩展性和通用性，
 只封装调用部分，拿到token 和用户信息后，需要在应用服务中自行存储，这部分功能不在 `slardar` 里提供实现
 
### 场景1: 仅希望支持第三方认证平台登录
> WIP

### 场景2: 服务采用`slardar`认证服务，同时需要支持第三方认证平台登录
> WIP