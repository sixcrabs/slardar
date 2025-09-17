package cn.piesat.v.slardar.spi.token;

import cn.piesat.v.slardar.spi.SlardarSpi;

import java.time.LocalDateTime;

/**
 * <p>
 * token provider spi，应用方可以通过实现此接口， 重写认证token 逻辑
 * </p>
 *
 * @author alex
 * @version v1.0 2023/11/20
 */
public interface SlardarTokenProvider extends SlardarSpi {

    /**
     * 生成 token
     * @param userKey  用户key
     * @return
     */
    SlardarToken provide(String userKey);

    /**
     * 从 token 值中解析出 userKey
     * @param tokenValue token值
     * @return 用户key
     */
    String geUserKey(String tokenValue);

    /**
     * token 存活秒数
     * @return
     */
    long getTokenTTL();


    class SlardarToken {

        /**
         * token 值
         */
        private String tokenValue;

        /**
         * 过期日期
         */
        private LocalDateTime expiresAt;

        public String getTokenValue() {
            return tokenValue;
        }

        public SlardarToken setTokenValue(String tokenValue) {
            this.tokenValue = tokenValue;
            return this;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        public SlardarToken setExpiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }
    }
}
