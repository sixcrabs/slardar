
# Slardar

基于 Spring Security 框架封装定义了 4A (Authentication、Authorization、Account、Audit) 相关的接口和逻辑流程，应用服务可以快速具有`4A`等能力。

## 分支说明

- master： 主分支，支持 springboot 2.x 
- springboot3: 支持 springboot3 + jdk 17

## 特性

- 基于 spring security 实现认证和权限控制
- 支持 SSO 单点登录
- 支持 MFA 多因素认证
- 支持登录加密、验证码等
- 支持 LDAP 等用户联合认证体系
- 支持集成方实现SPI进行自定义扩展
- ...

## 版本更新

最新版本: `1.7.0-SNAPSHOT`

### 1.7.0-SNAPSHOT
- 更新升级内部依赖包的版本号
- 增加插件`slardar-ext-firewall` 用于控制接口访问
- 增加了`license`授权模块，可用于对应用进行授权
- 其他一些bug修复

### 1.6.0-SNAPSHOT
- 移除了 `hutool` 的相关依赖，改为内部实现，可以避免和应用包内的 hutool 依赖冲突
- 重写了 `keystore` 模块，支持多种存储方式(memory/mapdb/mvstore/redis), 在轻量的单体服务中，可以不必依赖redis，采用内部存储即可
- 修复一些 bug

## 快速使用
### 引入依赖

```xml
<dependency>
    <groupId>org.winterfell</groupId>
    <artifactId>slardar-starter</artifactId>
    <version>${latest.version}</version>
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

> 这里示例代码在本地创建了示例账户，实际应用开发时 这部分应当从数据库中获取到用户信息

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

```java
    @GetMapping("/name")
    @SlardarIgnore
    public Resp getName() {
        return Resp.of(RandomUtil.randomString(18));
    }
```

### @SlardarAuthority
指定被注解的接口的访问权限

```java
    @GetMapping("/admin/demo")
    @SlardarAuthority("hasRole('ADMIN')")
    public Resp onlyAdmin() {
        return Resp.of("admin_".concat(RandomUtil.randomString(12)));
    }
```
> 定义可访问权限，支持:
    - hasAnyRole('ADMIN')
    - hasRole('xx')
    - hasAuthority('xxx')
    - hasAnyAuthority('xx','yy')
    - permitAll()
    - denyAll()
    - ...

### 配置方式
支持在配置文件中全局配置哪些url会被忽略:

```yaml
slardar:
  ignores: /login,/open/**
```

### 自定义bean
自定义bean 可以定义需要被忽略的url以及对url进行细粒度的权限控制

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

## 登录定制

内置了 `/login` 的登录接口以及 `/captcha` 的验证码接口，且提供了配置方式来满足不同场景的需求。

- 修改默认的登录url

```yaml
slardar:
  login:
    url: /auth/login
```

- 启用/关闭 登录验证码

```yaml
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

## 自定义扩展

为了应对复杂的业务场景以及提供尽可能大的修改范围，`slardar` 中设计了几个扩展点，按类型主要分为 `SPI` 扩展以及 `Spring Component` 扩展

### SPI

#### SlardarTokenProvider

用于自定义认证 token 的生成等处理逻辑，默认是采用的 jwt 处理token，你也可以实现这个接口来创造自己的 token 生成等逻辑

示例:

```java
@AutoService(SlardarTokenProvider.class)
public class MyTokenProviderImpl implements SlardarTokenProvider {
    /**
     * token 类型
     * - jwt
     * - ...
     *
     * @return
     */
    @Override
    public String name() {
        return "my";
    }

    /**
     * 初始化
     *
     * @param context
     */
    @Override
    public void initialize(SlardarSpiContext context) {
    }

    /**
     * 生成 token
     *
     * @param userDetails
     * @return
     */
    @Override
    public Payload generate(Object userDetails) {
        UserDetails data = (UserDetails) userDetails;
        return new Payload().setTokenValue(RandomUtil.randomString(5)).setExpiresAt(LocalDateTime.now().plusHours(1L));
    }



    /**
     * 生成 token
     *
     * @param username
     * @return
     */
    @Override
    public Payload generate(String username) {
        return new Payload().setTokenValue(RandomUtil.randomString(5)).setExpiresAt(LocalDateTime.now().plusHours(1L));
    }

    /**
     * 从 token 值中解析出 subject （往往是 username）
     *
     * @param tokenValue
     * @return
     */
    @Override
    public String getSubject(String tokenValue) {
        return "";
    }

    /**
     * 时间上是否已过期
     *
     * @param tokenValue
     * @return
     */
    @Override
    public Boolean isExpired(String tokenValue) {
        return true;
    }

    /**
     * 过期秒数
     *
     * @return
     */
    @Override
    public long getExpiration() {
        return 120000;
    }

}
```

随后，在配置中启用该token provider:

```yaml
slardar:
  token:
    type: my   #来自于 name() 方法返回值
```


#### SlardarAuthenticateResultAdapter

用于定制认证返回结果，有些场景里，希望能对返回的成功或失败的结果进行自定义设置，就可以通过实现该SPI接口来满足要求，示例:

```java
@AutoService(SlardarAuthenticateResultAdapter.class)
public class MyAuthResultAdaptor implements SlardarAuthenticateResultAdapter {
    /**
     * 认证成功结果
     *
     * @param accountInfoDTO 账户信息
     * @return
     */
    @Override
    public Map<String, Object> authSucceedResult(AccountInfoDTO accountInfoDTO) {
        HashMap<String, Object> map = MapUtil.newHashMap(3);
        map.put("token", accountInfoDTO.getToken());
        map.put("accountName", accountInfoDTO.getAccountName());
        map.put("username", accountInfoDTO.getUserProfile().getName());
        return map;
    }

    /**
     * 认证失败结果
     *
     * @param exception 认证异常
     * @return
     */
    @Override
    public Map<String, Object> authFailedResult(RuntimeException exception) {
        HashMap<String, Object> map = MapUtil.newHashMap(3);
        map.put("error", exception.getLocalizedMessage());
        map.put("code", 500);
        map.put("hint", "登录失败了");
        return map;
    }

    /**
     * 无权限访问结果
     *
     * @param exception 认证异常
     * @return
     */
    @Override
    public Map<String, Object> authDeniedResult(RuntimeException exception) {
        HashMap<String, Object> map = MapUtil.newHashMap(3);
        map.put("error", exception.getLocalizedMessage());
        map.put("code", 403);
        map.put("hint", "没有权限访问哦~");
        return map;
    }

    /**
     * 实现名称, 区分不同的 SPI 实现，必须
     *
     * @return
     */
    @Override
    public String name() {
        return "custom";
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void initialize(SlardarSpiContext context) {
        // 这里可以获取到容器里注入的 bean、配置等上下文环境
    }
}
```

同样的，需要在配置里指定采用自定义的处理类型:
```yaml
slardar:
    login:
        result-handler-type: custom 
```

#### SlardarCrypto

用于自定义加解密实现

> 目前加解密主要用于登录时密码的传输

示例，自定义一个加解密实现：

```java
@AutoService(SlardarCrypto.class)
public class MyCrypto implements SlardarCrypto {
    /**
     * 加密
     *
     * @param plaintext
     * @return
     */
    @Override
    public String encrypt(String plaintext) throws SlardarException {
        return Base64Encoder.encode(plaintext);
    }

    /**
     * 解密
     *
     * @param ciphertext
     * @return
     */
    @Override
    public String decrypt(String ciphertext) throws SlardarException {
        return Base64Decoder.decodeStr(ciphertext);
    }

    /**
     * 实现名称, 区分不同的 SPI 实现，必须
     *
     * @return
     */
    @Override
    public String name() {
        return "mystical";
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void initialize(SlardarSpiContext context) {
        // 这里可以获取到容器里注入的 bean、配置等上下文环境
    }
}
```

同样的，需要在配置开启登录密码加密并指定加密实现：
```yaml
slardar:
    login:
        encrypt:
            enabled: true  #开启加解密
            mode: mystical #使用自定义的加解密实现
```

#### SlardarOtpDispatcher

用于定制MFA 多因素认证时 otp 验证码发送的实现，可参考默认的 `EmailOtpDispatcher` 来实现自定义的OTP发送，比如通过短信发送验证码

### Spring Component 扩展

#### SlardarAuthenticateHandler

适用于自定义或微调认证逻辑的场景，比如考虑这样一个需求场景：某个应用需要支持用户名密码登录以及微信扫码登录两种方式，slardar 默认内置的是用户名密码方式，此时就可以通过实现此接口增加一种认证方式：

> slardar 提供了一个插件 `slardar-ext-ldap` 就是通过上述实现的支持`LDAP`认证

```java
@AutoService(SlardarAuthenticateHandler.class)
public class LdapSlardarAuthenticateHandlerImpl extends AbstractSlardarAuthenticateHandler {

    public static final Logger logger = LoggerFactory.getLogger(LdapSlardarAuthenticateHandlerImpl.class);

    private static final String NAME = "LDAP";

    /**
     * 认证处理类型 用于区分
     *
     * @return
     */
    @Override
    public String type() {
        return NAME;
    }

    /**
     * 处理认证请求
     *
     * @param requestWrapper
     * @return
     * @throws AuthenticationServiceException
     */
    @Override
    public SlardarAuthentication handleRequest(RequestWrapper requestWrapper) throws AuthenticationException {
        String username = requestWrapper.getRequestParams().get("username");
        String password = requestWrapper.getRequestParams().get("password");
        // 租户信息 默认为 master
        String realm = getRealm(requestWrapper);
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new AuthenticationServiceException("`username` and `password` should not be null");
        }
        return new SlardarAuthentication(username, NAME, null)
                .setRealm(realm)
                .setLoginDeviceType(requestWrapper.getLoginDeviceType())
                .setSessionId(requestWrapper.getSessionId())
                .setPassword(password);
    }


    /**
     * 子类实现
     * TESTME: 访问ldap 进行认证 和 用户身份同步
     *
     * @param authentication
     * @return
     */
    @Override
    protected SlardarAuthentication doAuthenticate0(SlardarAuthentication authentication) {
        LdapProperties properties = context.getBeanIfAvailable(LdapProperties.class);
        LdapUtils ldapUtils = new LdapUtils(properties.determineUrls(),
                StrUtil.format("cn={},{}", authentication.getAccountName(), properties.getBase()),
                authentication.getPassword(), properties.getBase());
        try {
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(ldapUtils.getSearchScope());
            String filter = StrUtil.format("cn={}", authentication.getAccountName().trim(),
                    authentication.getCredentials().toString());
            NamingEnumeration<SearchResult> results = ldapUtils.getConnection().search(ldapUtils.getBaseDN(), filter, searchCtls);
            long recordCount = 0;
            while (null != results && results.hasMoreElements()) {
                SearchResult sr = results.nextElement();
                if (sr != null) {
                    // 同步ldap用户的信息
                    Account account = new Account();
                    logger.debug("LDAP User {} , name [{}] , NameInNamespace [{}]",
                            (++recordCount), sr.getName(), sr.getNameInNamespace());

                    Attributes attributes = sr.getAttributes();
                    account.setName(authentication.getAccountName())
                            .setRealm(authentication.getRealm());
                    account.setDeleted(0);
                    UserProfile userProfile = new UserProfile().setName(LdapUtils.getAttrStringValue(attributes.get("sn")))
                            .setTelephone(LdapUtils.getAttrStringValue(attributes.get("telephoneNumber")));
                    account.setUserProfile(userProfile);
                    authentication.setUserDetails(new SlardarUserDetails(account));
                }
            }
            if (recordCount == 0) {
                // not found
                throw new UsernameNotFoundException("用户不存在");

            }
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getLocalizedMessage());
        } finally {
            ldapUtils.close();
        }
        return authentication;
    }
}
```


#### SlardarAuthenticatePreHandler

用于自定义认证前置处理，考虑这样一个业务需求：只允许来自某个ip段的用户可以登录（ip白名单） 或 限制某个ip段的用户登录(ip黑名单)，或者是限制某个（类）账号在PC端登录，这些需求可以用前置处理器来实现：

```java [IpWhiteListSlardarAuthenticatePreHandler.java]
@Component
public class IpWhiteListSlardarAuthenticatePreHandler implements SlardarAuthenticatePreHandler {

    private static final  ArrayList<String> ipList = Lists.newArrayList("127.0.0.1");

    /**
     * 在进入认证前 由应用前置处理，
     * 如
     * - 判断登录端类型
     * - 判断客户端ip等
     *
     * @param authentication 认证数据对象
     * @throws SlardarException 抛出异常 则终止认证
     */
    @Override
    public void preHandle(SlardarAuthentication authentication) throws SlardarException {
        String clientIp = authentication.getReqClientIp();
        // 这里进行ip 白名单控制
        System.out.println(clientIp);
    }
}
```

```java [MySlardarAuthenticatePreHandler.java]
@Component
public class MySlardarAuthenticatePreHandler implements SlardarAuthenticatePreHandler {

    /**
     * 在进入认证前 由应用前置处理，
     * 如
     * - 判断登录端类型
     * - 判断客户端ip等
     *
     * @param authentication
     * @throws SlardarException 抛出异常 则终止认证
     */
    @Override
    public void preHandle(SlardarAuthentication authentication) throws SlardarException {
        // 这里可以访问数据库进行一系列操作
        LoginDeviceType loginDeviceType = authentication.getLoginDeviceType();
        String accountName = authentication.getAccountName();
        if ("zhangsan".equals(accountName)) {
            if (loginDeviceType.equals(LoginDeviceType.PC)) {
                throw new SlardarException("账号【%s】不允许在 %s 登录", accountName, "PC");
            }
        }

    }
}
```

### 其他
#### 登录方式扩展

适用场景：某个系统里，需要支持`pc`端的账号登录方式，同时也需要支持小程序端的微信账号登录方式，此时可以通过扩展 `AbstractSlardarAuthenticateHandler` 来实现自定义

> 实现原理是： 通过认证请求头参数 `X-Auth-Type` 的不同值，寻找对应的实现类，从而实现不同方式的认证

slardar 里默认提供了 两个实现：
- `OpenIdSlardarAuthenticateHandlerImpl`： open-id 实现，处理请求头 `X-Auth-Type : open-id` 的认证请求
- `DefaultSlardarAuthenticateHandlerImpl`: 默认实现，处理所有默认的认证请求

假如你要自定义一个实现，可以这么做：

```java
@AutoService(SlardarAuthenticateHandler.class)
public class MyOpenIdSlardarAuthenticateHandlerImpl extends AbstractSlardarAuthenticateHandler {
    /**
     * 子类实现
     *
     * @param authentication
     * @return
     */
    @Override
    protected SlardarAuthentication doAuthenticate0(SlardarAuthentication authentication) {
        String openId = authentication.getAccountName();
        SlardarUserDetailsServiceImpl detailsService = (SlardarUserDetailsServiceImpl) context.getBeanIfAvailable(UserDetailsService.class);
        SlardarUserDetails userDetails = (SlardarUserDetails) detailsService.loadUserByOpenId(openId);
        // 判断是否正确
        authentication.setUserDetails(userDetails).setAuthenticated(true);
        return authentication;
    }

    private static final String NAME = "wx-openid";

    /**
     * 认证处理类型 用于区分
     *
     * @return
     */
    @Override
    public String type() {
        return NAME;
    }

    /**
     * 处理认证请求
     *
     * @param requestWrapper
     * @return
     * @throws AuthenticationServiceException
     */
    @Override
    public SlardarAuthentication handleRequest(RequestWrapper requestWrapper) throws AuthenticationException {
        String openid = requestWrapper.getRequestParams().get("openid");
        if (StringUtil.isBlank(openid)) {
            throw new AuthenticationServiceException("需要提供openid！");
        }
        return new SlardarAuthentication(openid, Constants.AUTH_TYPE_WX_APP, null);
    }
}
```

此时前端仅需要在微信登录调用接口时，指定请求头 `X-Auth-Type:wx-openid`,该认证请求会通过自定义的认证逻辑来验证身份

> 以上代码仅作为示例，实际需求可能复杂许多