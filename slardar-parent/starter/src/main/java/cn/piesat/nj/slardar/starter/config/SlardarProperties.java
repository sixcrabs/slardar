package cn.piesat.nj.slardar.starter.config;

import cn.piesat.nj.slardar.starter.support.LoginConcurrentPolicy;
import cn.piesat.nj.slardar.starter.token.SlardarTokenJwtImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

import static cn.piesat.nj.slardar.core.Constants.AUTH_LOGIN_URL;
import static cn.piesat.nj.slardar.core.Constants.AUTH_TOKEN_KEY;


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
     * 验证码参数设置
     */
    private CaptchaSettings captcha = new CaptchaSettings();

    /**
     * urls to be ignored
     */
    private String[] ignores = new String[]{"/login", "/captcha"};


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
        private EncryptSetting encrypt;

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
