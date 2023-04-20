package cn.piesat.nj.slardar.starter.token;

import cn.piesat.nj.slardar.starter.SlardarContext;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

/**
 * <p>
 * token 接口
 * 应用方可自行实现 token
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/27
 */
public interface SlardarToken {

    /**
     * token 类型
     * - jwt
     * - ...
     * @return
     */
    String type();


    /**
     * 初始化
     * @param context
     */
    void initialize(SlardarContext context);

    /**
     * 生成 token
     * @param userDetails
     * @return
     */
    Payload generate(UserDetails userDetails);

    /**
     * 生成 token
     * @param username
     * @return
     */
    Payload generate(String username);

    /**
     * 从 token 值中解析出 subject （往往是 username）
     * @param tokenValue
     * @return
     */
    String getSubject(String tokenValue);

    /**
     * 时间上是否已过期
     * @param tokenValue
     * @return
     */
    Boolean isExpired(String tokenValue);

    /**
     * 过期秒数
     * @return
     */
    long getExpiration();


    class Payload {

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

        public Payload setTokenValue(String tokenValue) {
            this.tokenValue = tokenValue;
            return this;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        public Payload setExpiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }
    }
}
