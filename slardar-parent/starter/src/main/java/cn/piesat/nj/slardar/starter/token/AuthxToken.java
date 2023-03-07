package cn.piesat.nj.slardar.starter.token;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/27
 */
public interface AuthxToken {


    /**
     * 生成 token
     * @param userDetails
     * @return
     */
    Payload generateToken(UserDetails userDetails);

    /**
     * 生成 token
     * @param username
     * @return
     */
    Payload generateToken(String username);

    /**
     * 从 token 值中解析出 subject （往往是 username）
     * @param tokenValue
     * @return
     */
    String getSubjectFromToken(String tokenValue);

    /**
     * 时间上是否已过期
     * @param tokenValue
     * @return
     */
    Boolean isExpired(String tokenValue);



    @Data
    @Accessors(chain = true)
    class Payload {

        /**
         * token 值
         */
       private String tokenValue;

        /**
         * 过期日期
         */
       private LocalDateTime expiresAt;

    }
}
