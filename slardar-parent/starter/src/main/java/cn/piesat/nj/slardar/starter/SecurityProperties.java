package cn.piesat.nj.slardar.starter;

import cn.piesat.v.authx.security.infrastructure.spring.support.LoginConcurrentPolicy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

import static cn.piesat.v.authx.security.infrastructure.spring.support.SecUtil.AUTH_LOGIN_URL;

/**
 * <p>
 * 配置信息
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/26
 */
@Data
@ConfigurationProperties(prefix = "vintage.security")
public class SecurityProperties implements Serializable {

    /**
     * jwt 参数
     */
    private JwtSettings jwt = new JwtSettings();

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
    private String[] ignores = new String[]{"/login"};


    @Data
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

    }


    /**
     * 登录参数设置
     */
    @Data
    public static class LoginSettings {

        /**
         * 登录的 url
         */
        private String url = AUTH_LOGIN_URL;

        /**
         * 是否必须验证码 默认需要
         */
        private Boolean captchaEnabled = true;

        /**
         * 是否开启加密设置
         */
        private boolean cryptoEnabled = false;

        /**
         * AES 加密 私钥 必须 16位
         */
        private String cryptoSecretKey = "abcdefghijklmnop";

        /**
         * 是否同端互斥，默认不互斥，即 两个pc登录返回的token都可用
         * 互斥：另一个PC登录时 前一个token 会失效（登出）
         */
        private LoginConcurrentPolicy concurrentPolicy = LoginConcurrentPolicy.separate;


    }


    public static class JwtSettings {

        /**
         * jwt 签名 key
         */
        private String signKey = "piesat_nj";

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
