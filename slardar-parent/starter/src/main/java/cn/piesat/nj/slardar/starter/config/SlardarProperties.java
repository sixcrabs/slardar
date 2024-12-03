package cn.piesat.nj.slardar.starter.config;

import cn.piesat.nj.slardar.starter.handler.SlardarDefaultAuthenticateResultAdapter;
import cn.piesat.nj.slardar.starter.support.spi.EmailOtpDispatcher;
import cn.piesat.nj.slardar.starter.support.LoginConcurrentPolicy;
import cn.piesat.nj.slardar.starter.support.spi.token.SlardarTokenProviderJwtImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static cn.piesat.nj.slardar.core.Constants.AUTH_LOGIN_URL;
import static cn.piesat.nj.slardar.core.Constants.AUTH_TOKEN_KEY;
import static org.springframework.boot.web.servlet.server.Encoding.DEFAULT_CHARSET;


/**
 * <p>
 * 配置信息
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
@ConfigurationProperties(prefix = "slardar")
public class SlardarProperties implements Serializable {

    /**
     * basic auth 配置
     */
    private BasicAuthSetting basic = new BasicAuthSetting();

    /**
     * token 类型设置
     */
    private TokenSetting token = new TokenSetting();

    /**
     * 登录相关参数设置
     */
    private LoginSetting login = new LoginSetting();

    /**
     * 多因子认证
     */
    private MfaSetting mfa = new MfaSetting();

    /**
     * 验证码参数设置
     */
    private CaptchaSetting captcha = new CaptchaSetting();

    /**
     * urls to be ignored
     */
    private String[] ignores = new String[]{"/login", "/captcha"};


    public BasicAuthSetting getBasic() {
        return basic;
    }

    public SlardarProperties setBasic(BasicAuthSetting basic) {
        this.basic = basic;
        return this;
    }

    public MfaSetting getMfa() {
        return mfa;
    }

    public SlardarProperties setMfa(MfaSetting mfa) {
        this.mfa = mfa;
        return this;
    }

    public TokenSetting getToken() {
        return token;
    }

    public SlardarProperties setToken(TokenSetting token) {
        this.token = token;
        return this;
    }

    public LoginSetting getLogin() {
        return login;
    }

    public SlardarProperties setLogin(LoginSetting login) {
        this.login = login;
        return this;
    }

    public CaptchaSetting getCaptcha() {
        return captcha;
    }

    public SlardarProperties setCaptcha(CaptchaSetting captcha) {
        this.captcha = captcha;
        return this;
    }

    public String[] getIgnores() {
        return ignores;
    }

    public SlardarProperties setIgnores(String[] ignores) {
        this.ignores = ignores;
        return this;
    }

    /**
     * api 签名认证参数设置
     */
    public static  class ApiSignatureSetting {
        /**
         * 是否开启 api 签名认证 默认关闭
         */
        private boolean enable = false;

        /**
         * 请求头 key 的前缀 可以避免和其他重复 默认 X-
         */
        private String headerKeyPrefix = "X-";

        /**
         * 针对哪些 url pattern 采用 Api 签名验证
         */
        private String[] filterUrls = new String[]{};


        public String getHeaderKeyPrefix() {
            return headerKeyPrefix;
        }

        public ApiSignatureSetting setHeaderKeyPrefix(String headerKeyPrefix) {
            this.headerKeyPrefix = headerKeyPrefix;
            return this;
        }

        public boolean isEnable() {
            return enable;
        }

        public ApiSignatureSetting setEnable(boolean enable) {
            this.enable = enable;
            return this;
        }

        public String[] getFilterUrls() {
            return filterUrls;
        }

        public ApiSignatureSetting setFilterUrls(String[] filterUrls) {
            this.filterUrls = filterUrls;
            return this;
        }
    }

    /**
     * BasicAuth 认证参数
     */
    public static class BasicAuthSetting {

        /**
         * 是否开启 basic auth 认证能力 默认关闭
         */
        private boolean enable = false;

        /**
         * 针对哪些 url pattern 采用 basicAuth 过滤
         */
        private String[] filterUrls = new String[]{};

        public boolean isEnable() {
            return enable;
        }

        public BasicAuthSetting setEnable(boolean enable) {
            this.enable = enable;
            return this;
        }

        public String[] getFilterUrls() {
            return filterUrls;
        }

        public BasicAuthSetting setFilterUrls(String[] filterUrls) {
            this.filterUrls = filterUrls;
            return this;
        }
    }

    /**
     * 验证码设置参数
     */
    public static class CaptchaSetting {

        /**
         * 验证码过期时间 单位 秒,默认 5分钟过期
         */
        private long expiration = 300L;

        /**
         * 验证码长度
         */
        private int length = 4;

        /**
         * 随机字符串的基数 若都为数字 则生成的是数字验证码
         */
        private String randomBase = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";

        /**
         * 图片高度
         */
        private int width = 160;

        /**
         * 图片宽度
         */
        private int height = 64;

        public long getExpiration() {
            return expiration;
        }

        public CaptchaSetting setExpiration(long expiration) {
            this.expiration = expiration;
            return this;
        }

        public int getLength() {
            return length;
        }

        public CaptchaSetting setLength(int length) {
            this.length = length;
            return this;
        }

        public String getRandomBase() {
            return randomBase;
        }

        public CaptchaSetting setRandomBase(String randomBase) {
            this.randomBase = randomBase;
            return this;
        }

        public int getWidth() {
            return width;
        }

        public CaptchaSetting setWidth(int width) {
            this.width = width;
            return this;
        }

        public int getHeight() {
            return height;
        }

        public CaptchaSetting setHeight(int height) {
            this.height = height;
            return this;
        }
    }

    /**
     * 登录参数设置
     */
    public static class LoginSetting {

        /**
         * 认证结果处理类型 可自定义 SPI 实现返回结果的定制
         */
        private String resultHandlerType = SlardarDefaultAuthenticateResultAdapter.NAME;

        /**
         * 登录的 url
         */
        private String url = AUTH_LOGIN_URL;

        /**
         * 是否仅支持 post 方式访问login接口
         */
        private boolean postOnly = true;

        /**
         * 登录成功返回 code 值
         */
        private int loginSuccessCode = 0;

        /**
         * 是否必须验证码 默认需要
         */
        private Boolean captchaEnabled = true;

        /**
         * 密码加密设置
         */
        private EncryptSetting encrypt = new EncryptSetting();

        /**
         * 是否同端互斥，默认不互斥，即 两个pc登录返回的token都可用
         * 互斥：另一个PC登录时 前一个token 会失效（登出）
         */
        private LoginConcurrentPolicy concurrentPolicy = LoginConcurrentPolicy.separate;

        /**
         * 登录最大允许尝试次数 默认 5次
         */
        private Integer maxAttemptsBeforeLocked = 5;

        /**
         * 登录失败最大次数后锁定时间 默认 1分钟
         */
        private Duration failedLockDuration = Duration.ofMinutes(1L);

        public String getResultHandlerType() {
            return resultHandlerType;
        }

        public LoginSetting setResultHandlerType(String resultHandlerType) {
            this.resultHandlerType = resultHandlerType;
            return this;
        }

        public Integer getMaxAttemptsBeforeLocked() {
            return maxAttemptsBeforeLocked;
        }

        public LoginSetting setMaxAttemptsBeforeLocked(Integer maxAttemptsBeforeLocked) {
            this.maxAttemptsBeforeLocked = maxAttemptsBeforeLocked;
            return this;
        }

        public Duration getFailedLockDuration() {
            return failedLockDuration;
        }

        public LoginSetting setFailedLockDuration(Duration failedLockDuration) {
            this.failedLockDuration = failedLockDuration;
            return this;
        }

        public EncryptSetting getEncrypt() {
            return encrypt;
        }

        public boolean isPostOnly() {
            return postOnly;
        }

        public LoginSetting setPostOnly(boolean postOnly) {
            this.postOnly = postOnly;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public LoginSetting setUrl(String url) {
            this.url = url;
            return this;
        }

        public int getLoginSuccessCode() {
            return loginSuccessCode;
        }

        public LoginSetting setLoginSuccessCode(int loginSuccessCode) {
            this.loginSuccessCode = loginSuccessCode;
            return this;
        }

        public Boolean getCaptchaEnabled() {
            return captchaEnabled;
        }

        public LoginSetting setCaptchaEnabled(Boolean captchaEnabled) {
            this.captchaEnabled = captchaEnabled;
            return this;
        }

        public boolean isCryptoEnabled() {
            return encrypt.isEnabled();
        }

        public LoginSetting setEncrypt(EncryptSetting encrypt) {
            this.encrypt = encrypt;
            return this;
        }

        public LoginConcurrentPolicy getConcurrentPolicy() {
            return concurrentPolicy;
        }

        public LoginSetting setConcurrentPolicy(LoginConcurrentPolicy concurrentPolicy) {
            this.concurrentPolicy = concurrentPolicy;
            return this;
        }
    }


    /**
     * 多因素认证配置 (OTP)
     */
    public static class MfaSetting {

        /**
         * 默认关闭，一旦开启 会改变登录过程
         */
        private boolean enabled;

        /**
         * OTP 口令发送模式
         * - email
         * - sms
         * - 自定义实现
         */
        private String otpMode = EmailOtpDispatcher.MODE;

        /**
         * email 参数设置
         */
        private EmailSetting email = new EmailSetting();


        public boolean isEnabled() {
            return enabled;
        }

        public MfaSetting setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getOtpMode() {
            return otpMode;
        }

        public MfaSetting setOtpMode(String otpMode) {
            this.otpMode = otpMode;
            return this;
        }

        public EmailSetting getEmail() {
            return email;
        }

        public MfaSetting setEmail(EmailSetting email) {
            this.email = email;
            return this;
        }
    }


    /**
     * email 发送配置
     */
    public static class EmailSetting {

        /**
         * SMTP server host. For instance, `smtp.example.com`.
         */
        private String host;

        /**
         * SMTP server port.
         */
        private Integer port;

        /**
         * Login user of the SMTP server.
         */
        private String username;

        /**
         * Login password of the SMTP server.
         * 授权码
         */
        private String password;

        /**
         * Protocol used by the SMTP server.
         */
        private String protocol = "smtp";

        /**
         * Default MimeMessage encoding.
         */
        private Charset defaultEncoding = DEFAULT_CHARSET;

        /**
         * Additional JavaMail Session properties.
         */
        private Map<String, String> properties = new HashMap<>();


        public void applyProperties(JavaMailSenderImpl sender) {
            sender.setHost(this.getHost());
            if (this.getPort() != null) {
                sender.setPort(this.getPort());
            }
            sender.setUsername(this.getUsername());
            sender.setPassword(this.getPassword());
            sender.setProtocol(this.getProtocol());
            if (this.getDefaultEncoding() != null) {
                sender.setDefaultEncoding(this.getDefaultEncoding().name());
            }
            if (!this.getProperties().isEmpty()) {
                sender.setJavaMailProperties(asProperties(this.getProperties()));
            }
        }

        private Properties asProperties(Map<String, String> source) {
            Properties properties = new Properties();
            properties.putAll(source);
            return properties;
        }


        public String getHost() {
            return host;
        }

        public EmailSetting setHost(String host) {
            this.host = host;
            return this;
        }

        public Integer getPort() {
            return port;
        }

        public EmailSetting setPort(Integer port) {
            this.port = port;
            return this;
        }

        public String getUsername() {
            return username;
        }

        public EmailSetting setUsername(String username) {
            this.username = username;
            return this;
        }

        public String getPassword() {
            return password;
        }

        public EmailSetting setPassword(String password) {
            this.password = password;
            return this;
        }

        public String getProtocol() {
            return protocol;
        }

        public EmailSetting setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Charset getDefaultEncoding() {
            return defaultEncoding;
        }

        public EmailSetting setDefaultEncoding(Charset defaultEncoding) {
            this.defaultEncoding = defaultEncoding;
            return this;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public EmailSetting setProperties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }
    }


    /**
     * 密码加密参数
     */
    public static class EncryptSetting {

        /**
         * 默认关闭
         */
        private boolean enabled;

        /**
         * 加密模式 默认 aes
         * 可自行扩展
         */
        private String mode = "AES";

        /**
         * 密钥 RSA 时是公钥
         */
        private String secretKey;


        public boolean isEnabled() {
            return enabled;
        }

        public EncryptSetting setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getMode() {
            return mode;
        }

        public EncryptSetting setMode(String mode) {
            this.mode = mode;
            return this;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public EncryptSetting setSecretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }
    }

    /**
     * token 参数设置
     */
    public static class TokenSetting {

        private String type = SlardarTokenProviderJwtImpl.NAME;

        /**
         * 设置 token name，默认 Authorization
         */
        private String key = AUTH_TOKEN_KEY;

        /**
         * jwt 参数
         */
        private JwtSetting jwt = new JwtSetting();

        /**
         * token key 分隔符 默认 _
         */
        private String separator = "_";


        public String getSeparator() {
            return separator;
        }

        public TokenSetting setSeparator(String separator) {
            this.separator = separator;
            return this;
        }

        public String getType() {
            return type;
        }

        public TokenSetting setType(String type) {
            this.type = type;
            return this;
        }

        public String getKey() {
            return key;
        }

        public TokenSetting setKey(String key) {
            this.key = key;
            return this;
        }

        public JwtSetting getJwt() {
            return jwt;
        }

        public TokenSetting setJwt(JwtSetting jwt) {
            this.jwt = jwt;
            return this;
        }
    }

    /**
     * jwt 参数设置
     */
    public static class JwtSetting {

        /**
         * jwt 签名 key
         */
        private String signKey = "piesat_nj_nync";

        /**
         * in seconds
         * 默认有效期 一天
         */
        private Long expiration = 24 * 60 * 60L;

        /**
         * 允许的时间差 秒数 默认 60
         */
        private Long allowedClockSkewSeconds = 60L;

        public Long getAllowedClockSkewSeconds() {
            return allowedClockSkewSeconds;
        }

        public JwtSetting setAllowedClockSkewSeconds(Long allowedClockSkewSeconds) {
            this.allowedClockSkewSeconds = allowedClockSkewSeconds;
            return this;
        }

        public String getSignKey() {
            return signKey;
        }

        public JwtSetting setSignKey(String signKey) {
            this.signKey = signKey;
            return this;
        }

        public Long getExpiration() {
            return expiration;
        }

        public JwtSetting setExpiration(Long expiration) {
            this.expiration = expiration;
            return this;
        }
    }


}
