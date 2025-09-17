
# Slardar

基于 Spring Securtiy 框架封装定义了 4A (Authentication、Authorization、Account、Audit) 相关的接口和逻辑流程，应用服务可以快速具有4A等能力。

## 特性

- 基于 spring security 实现认证和权限控制
- 支持 SSO 单点登录
- 支持 MFA 多因素认证
- 支持登录加密、验证码等
- 支持 LDAP 等用户联合认证体系
- 支持集成方实现SPI进行自定义扩展
- ...

## 版本更新

最新版本: `1.6.0-SNAPSHOT`

### 1.6.0-SNAPSHOT
- 移除了 `hutool` 的相关依赖，改为内部实现，可以避免和应用包内的 hutool 依赖冲突
- 重写了 `keystore` 模块，支持多种存储方式(memory/mapdb/mvstore/redis), 在轻量的单体服务中，可以不必依赖redis，采用内部存储即可
- 修复一些 bug

## 快速使用
### 引入依赖

```xml
<dependency>
    <groupId>cn.piesat.v</groupId>
    <artifactId>slardar-starter</artifactId>
    <version>1.6.0-SNAPSHOT</version>
</dependency>
```

### 实现接口
`Slardar` 定义好了认证和权限控制的流程和步骤，需要集成方实现提供账户的 bean，slardar 会在恰当的时机调用获取到账户信息

```java
@Component
public class AccountProviderImpl implements AccountProvider {

    /**
     * 模拟用户数据
     */
    private static final List<UserProfile> USER_PROFILES = Lists.newArrayList();

    private static final List<Account> ACCOUNTS = Lists.newArrayList();

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    static {

        UserProfile profile = new UserProfile()
                .setAddress(RandomUtil.randomString(5))
                .setName("张三")
                .setTelephone("13456789765");
        profile.setRealm("master");
        profile.setDeleted(0);
        profile.setId(RandomUtil.randomString(8));
        profile.setEmail("1075xxxxx@qq.com");
        profile.setRoles(Lists.newArrayList(new Role().setName("NORMAL_USER")));
        profile.setAuthorities(Lists.newArrayList(new Authority().setContent("READ_URL")));

        UserProfile profile2 = new UserProfile()
                .setAddress(RandomUtil.randomString(5))
                .setName("李四")
                .setTelephone("13756789765");
        profile2.setRealm("master");
        profile2.setDeleted(0);
        profile2.setId(RandomUtil.randomString(8));
        profile2.setRoles(Lists.newArrayList(new Role().setName("NORMAL_USER"),
                new Role().setName("ADMIN")));

        USER_PROFILES.add(profile);
        USER_PROFILES.add(profile2);

        Account zhangsan = new Account().setName("zhangsan")
                .setPassword(ENCODER.encode("zhangsan123"));
        zhangsan.setRealm("master");
        zhangsan.setId(RandomUtil.randomString(8));
        zhangsan.setStatus(AccountStatus.accessible)
                .setUserProfile(profile);
        // 口令过期剩余天数
        zhangsan.setPwdValidRemainDays(5);

        Account lisi = new Account().setName("lisi")
                .setPassword(ENCODER.encode("lisi123"));
        lisi.setRealm("master");
        lisi.setId(RandomUtil.randomString(8));
        lisi.setStatus(AccountStatus.accessible)
                .setUserProfile(profile2);

        ACCOUNTS.add(zhangsan);
        ACCOUNTS.add(lisi);
    }

    /**
     * find by name
     *
     * @param name
     * @param realm
     * @return
     */
    @Override
    public Account findByName(String name, String realm) throws SlardarException {
        // 查询账户
        return ACCOUNTS.stream().filter(account ->
                account.getName().equals(name)
        ).findFirst().orElse(null);
    }

    /**
     * find by openid
     *
     * @param openId
     * @return
     */
    @Override
    public Account findByOpenId(String openId) throws SlardarException {
        throw new SlardarException("Unsupported");
    }
}
```

:::tip
这里示例代码在本地创建了示例账户，实际应用开发时 这部分应当从数据库中获取到用户信息
:::

实现 AuditLogIngest，用于保存审计日志

```java
@Component
public class AuditLogIngestImpl implements AuditLogIngest {

    /**
     * ingest log
     *
     * @param auditLog
     */
    @Override
    public void ingest(AuditLog auditLog) {
        // 这里入日志库或入到消息队列
        System.out.println(auditLog);
        System.out.println(auditLog.getDetail());
    }
}
```

### 增加配置

slardar 依赖 redis 实现认证信息存储、过期等状态，所以需要配置redis

```yaml
skv:
  type: redis
  uri: redis://localhost/0
```
### 启动服务

启动后，进行登录： 
> `http://localhost:9600/login?username=zhangsan&password=zhangsan123`

可以看到返回成功的登录结果：

```json
{
  "data": {
    "accountExpired": false,
    "accountLocked": false,
    "accountName": "zhangsan",
    "userProfile": {
      "id": "ji7b7ugl",
      "deleted": false,
      "realm": "master",
      "name": "张三",
      "email": "1075xxxxx@qq.com",
      "telephone": "13456789765",
      "address": "av7lm",
      "roles": [
        {
          "deleted": false,
          "name": "NORMAL_USER"
        }
      ],
      "authorities": [
        {
          "deleted": false,
          "content": "READ_URL"
        }
      ]
    },
    "token": "eyJhbGciOiJIUzUxMiIsInppcCI6IkRFRiJ9.eNqqViouTVKyUqrKSMxLL07Mi_eNjE9Ks0hNMU8zTjNKSTExMzVOSkqzTLI0NTA1MEk0Nko0UtJRSi5KTSxJTVGyMjQ3NgYiYxMzEwsDHaXUigKImIWhpbmJmY5SZmIJsqJaAAAAAP__.O9RaROT9lpaaDeGvXEBFtmdQu76ktW2LkIh-sq_gEz7tDNQD7zOA6c5WhsM5dNCanUd9OfWLR-DmejcWd6vG1A",
    "tokenExpiresAt": "2024-12-10 16:35:46",
    "authorities": [
      "ROLE_NORMAL_USER",
      "READ_URL"
    ],
    "accountPwdValidRemainDays": 5
  },
  "message": "success",
  "code": 0
}
```

至此，一个简单的具备用户登录、认证、权限控制的security 服务就完成了

## 权限控制
上述验证了用户登录(`Authentication`)相关的功能，下面看下如何利用 Slardar 实现接口资源的权限控制，支持以下几种权限控制方式：

- 使用注解方式
- 使用配置文件（仅支持配置`ignore`地址，即忽略哪些url）
- 配置 spring bean 代码方式

### 注解方式

### @SlardarIgnore
配置该接口被忽略，即无需进行认证

```java{2}
    @GetMapping("/name")
    @SlardarIgnore
    public Resp getName() {
        return Resp.of(RandomUtil.randomString(18));
    }
```

### @SlardarAuthority
指定被注解的接口的访问权限

```java{2}
    @GetMapping("/admin/demo")
    @SlardarAuthority("hasRole('ADMIN')")
    public Resp onlyAdmin() {
        return Resp.of("admin_".concat(RandomUtil.randomString(12)));
    }
```
:::tip
定义可访问权限，支持:
- hasAnyRole('ADMIN')
- hasRole('xx')
- hasAuthority('xxx')
- hasAnyAuthority('xx','yy')
- permitAll()
- denyAll()
- ...
:::

### 配置方式
支持在配置文件中全局配置哪些url会被忽略:

```yaml{2}
slardar:
  ignores: /login,/open/**
```

### 自定义bean
自定义bean 可以定义需要被忽略的url以及对url进行细粒度的权限控制

:::code-group
```java [MyIgnoreRegistryImpl.java]
@Component
public class MyIgnoreRegistryImpl implements SlardarIgnoringCustomizer {
    @Override
    public void customize(WebSecurity.IgnoredRequestConfigurer configurer) {
        configurer.antMatchers("/api/greeting/**");
    }
}
```

```java [MyUrlRegistryCustomizerImpl.java]
@Component
public class MyUrlRegistryCustomizerImpl implements SlardarUrlRegistryCustomizer {

    /**
     * 配置自定义受限 url 信息
     *
     * @param registry
     */
    @Override
    public void customize(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) {
        // /admin 的url只能被 admin 角色访问
        registry.antMatchers("/api/admin/**").hasAnyRole("ADMIN", "SYS_ADMIN");
    }
}

```
:::

## 登录定制

内置了 `/login` 的登录接口以及 `/captcha` 的验证码接口，且提供了配置方式来满足不同场景的需求。

- 修改默认的登录url

```yaml
slardar:
  login:
    url: /auth/login
```

- 启用/关闭 登录验证码

```yaml {3}
slardar:
  login:
    captcha-enabled: false
```

- 设置登录最大尝试次数和失败锁定时间

```yaml
slardar:
  login:
    max-attempts-before-locked: 3 # 最大尝试次数
    failed-lock-duration: 2m   # 失败锁定时间
```

- 登录密码加密

```yaml
slardar:
  login:
      encrypt:
        enabled: true
        mode: sm4 # 采用国密4加密
```







