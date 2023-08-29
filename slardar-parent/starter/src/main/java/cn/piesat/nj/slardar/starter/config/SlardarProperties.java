package cn.piesat.nj.slardar.starter.config;

import cn.piesat.nj.slardar.starter.authenticate.mfa.impl.EmailOtpDispatcher;
import cn.piesat.nj.slardar.starter.support.LoginConcurrentPolicy;
import cn.piesat.nj.slardar.starter.token.SlardarTokenJwtImpl;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.Serializable;
import java.nio.charset.Charset;
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
     * token 类型设置
     */
    private TokenSettings token = new TokenSettings();

    /**
     * 登录相关参数设置
     */
    private LoginSettings login = new LoginSettings();

    /**
     * 多因子认证
     */
    private MfaSettings mfa = new MfaSettings();

    /**
     * 验证码参数设置
     */
    private CaptchaSettings captcha = new CaptchaSettings();

    /**
     * urls to be ignored
     */
    private String[] ignores = new String[]{"/login", "/captcha"};

    public MfaSettings getMfa() {
        return mfa;
    }

    public SlardarProperties setMfa(MfaSettings mfa) {
        this.mfa = mfa;
        return this;
    }

    public TokenSettings getToken() {
        return token;
    }

    public SlardarProperties setToken(TokenSettings token) {
        this.token = token;
        return this;
    }

    public LoginSettings getLogin() {
        return login;
    }

    public SlardarProperties setLogin(LoginSettings login) {
        this.login = login;
        return this;
    }

    public CaptchaSettings getCaptcha() {
        return captcha;
    }

    public SlardarProperties setCaptcha(CaptchaSettings captcha) {
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

    public static class CaptchaSettings {

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

        public CaptchaSettings setExpiration(long expiration) {
            this.expiration = expiration;
            return this;
        }

        public int getLength() {
            return length;
        }

        public CaptchaSettings setLength(int length) {
            this.length = length;
            return this;
        }

        public String getRandomBase() {
            return randomBase;
        }

        public CaptchaSettings setRandomBase(String randomBase) {
            this.randomBase = randomBase;
            return this;
        }

        public int getWidth() {
            return width;
        }

        public CaptchaSettings setWidth(int width) {
            this.width = width;
            return this;
        }

        public int getHeight() {
            return height;
        }

        public CaptchaSettings setHeight(int height) {
            this.height = height;
            return this;
        }
    }


    /**
     * 登录参数设置
     */
    public static class LoginSettings {

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


        public EncryptSetting getEncrypt() {
            return encrypt;
        }

        public boolean isPostOnly() {
            return postOnly;
        }

        public LoginSettings setPostOnly(boolean postOnly) {
            this.postOnly = postOnly;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public LoginSettings setUrl(String url) {
            this.url = url;
            return this;
        }

        public int getLoginSuccessCode() {
            return loginSuccessCode;
        }

        public LoginSettings setLoginSuccessCode(int loginSuccessCode) {
            this.loginSuccessCode = loginSuccessCode;
            return this;
        }

        public Boolean getCaptchaEnabled() {
            return captchaEnabled;
        }

        public LoginSettings setCaptchaEnabled(Boolean captchaEnabled) {
            this.captchaEnabled = captchaEnabled;
            return this;
        }

        public boolean isCryptoEnabled() {
            return encrypt.isEnabled();
        }

        public LoginSettings setEncrypt(EncryptSetting encrypt) {
            this.encrypt = encrypt;
            return this;
        }

        public LoginConcurrentPolicy getConcurrentPolicy() {
            return concurrentPolicy;
        }

        public LoginSettings setConcurrentPolicy(LoginConcurrentPolicy concurrentPolicy) {
            this.concurrentPolicy = concurrentPolicy;
            return this;
        }
    }


    /**
     * 多因素认证配置 (OTP)
     */
    public static class MfaSettings {

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

        public MfaSettings setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getOtpMode() {
            return otpMode;
        }

        public MfaSettings setOtpMode(String otpMode) {
            this.otpMode = otpMode;
            return this;
        }

        public EmailSetting getEmail() {
            return email;
        }

        public MfaSettings setEmail(EmailSetting email) {
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
         * 密钥
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
    public static class TokenSettings {

        private String type = SlardarTokenJwtImpl.NAME;

        /**
         * 设置 token name，默认 Authorization
         */
        private String key = AUTH_TOKEN_KEY;

        /**
         * jwt 参数
         */
        private JwtSettings jwt = new JwtSettings();

        public String getType() {
            return type;
        }

        public TokenSettings setType(String type) {
            this.type = type;
            return this;
        }

        public String getKey() {
            return key;
        }

        public TokenSettings setKey(String key) {
            this.key = key;
            return this;
        }

        public JwtSettings getJwt() {
            return jwt;
        }

        public TokenSettings setJwt(JwtSettings jwt) {
            this.jwt = jwt;
            return this;
        }
    }

    public static class JwtSettings {

        /**
         * jwt 签名 key
         */
        private String signKey = "piesat_nj_nync";

        /**
         * in seconds
         * 默认有效期 一天
         */
        private Long expiration = 24 * 60 * 60L;

        public String getSignKey() {
            return signKey;
        }

        public JwtSettings setSignKey(String signKey) {
            this.signKey = signKey;
            return this;
        }

        public Long getExpiration() {
            return expiration;
        }

        public JwtSettings setExpiration(Long expiration) {
            this.expiration = expiration;
            return this;
        }
    }


}
