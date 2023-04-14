## Slardar

### 接入

#### 引入 `slardar-starter`

```xml
 <dependency>
     <groupId>cn.piesat.nj</groupId>
     <artifactId>slardar-starter</artifactId>
     <version>1.3.0-SNAPSHOT</version>
</dependency>
```

#### 实现相关接口

- 实现 `AccountGateway` (必须)

```java
@Component
public class AccountGatewayImpl implements AccountGateway {

    /**
     * find by openid
     *
     * @param openId
     * @return
     */
    @Override
    public Account findByOpenId(String openId) {
        return null;
    }

    /**
     * find by name
     *
     * @param name
     * @param realm
     * @return
     */
    @Override
    public Account findByName(String name, String realm) {
        // TODO: 数据库查询
        return accounts.stream().filter(account ->
                account.getName().equals(name) && account.getRealm().equals(realm)
        ).findFirst().orElse(null);
    }

    /**
     * 创建实体
     *
     * @param entity
     * @return
     */
    @Override
    public String create(Account entity) {
        return null;
    }

    /**
     * 更新实体
     *
     * @param entity
     * @return
     */
    @Override
    public boolean update(Account entity) {
        return false;
    }

    
}
```

- 实现 `AuditLogGateway`

#### 增加配置信息

必须配置：
```yaml
skv:
  type: redis
  uri: redis://localhost/0
```

#### 实现接口权限控制

- 使用注解方式

```java

@GetMapping("/hi")
@SlardarAuthority("hasRole('ADMIN')")
public Resp sayHi() {
        return Resp.of("Hello, ".concat(RandomUtil.randomString(6)));
}

```

or

```java
    @GetMapping("/name")
    @SlardarIgnore
    public Resp getName() {
        return Resp.of(RandomUtil.randomString(18));
    }

```

- spring boot bean方式

实现 `SlardarUrlRegistryCustomizer` 接口对接口进行特定权限控制

```java
@Component
public class MyRegistryCustomizerImpl implements SlardarUrlRegistryCustomizer {
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

or

```java
@Component
public class MyIgnoreRegistryImpl implements SlardarIgnoringCustomizer {
    @Override
    public void customize(WebSecurity.IgnoredRequestConfigurer configurer) {
        configurer.antMatchers("/demo/**");
    }
}
```

#### 扩展

- 自定义 token
支持自定义 token 的创建、过期判断等逻辑方法


- 监听登录/登出事件

- 认证前置方法



