package org.winterfell.slardar.starter.support.spi.token;

import org.apache.commons.codec.digest.DigestUtils;
import org.winterfell.misc.timer.cron.DateTimeUtil;
import org.winterfell.slardar.core.SlardarContext;
import org.winterfell.slardar.spi.token.SlardarTokenProvider;
import org.winterfell.slardar.starter.SlardarProperties;
import com.google.auto.service.AutoService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * jwt 实现 token
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/27
 */
@AutoService(SlardarTokenProvider.class)
public class SlardarTokenJwtProvider implements SlardarTokenProvider {

    private static final String CLAIM_KEY_USERNAME = "sub";

    private static final String CLAIM_KEY_CREATED = "created";

    public static final String NAME = "jwt";

    private String secret;

    /**
     * 允许的时间偏移 秒,
     */
    private long allowedClockSkewSeconds = 30L;

    private Long expiration;

    private static final Logger log = LoggerFactory.getLogger(SlardarTokenJwtProvider.class);


    /**
     * token 类型
     * - jwt
     * - ...
     *
     * @return
     */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * 初始化
     *
     * @param context
     */
    @Override
    public void initialize(SlardarContext context) {
        secret = context.getBean(SlardarProperties.class).getToken().getJwt().getSignKey();
        expiration = context.getBean(SlardarProperties.class).getToken().getTtl();
        allowedClockSkewSeconds = context.getBean(SlardarProperties.class).getToken().getJwt().getAllowedClockSkewSeconds();
    }


    @Override
    public SlardarToken provide(String userKey) {
        Map<String, Object> claims = new HashMap<>(16);
        claims.put(CLAIM_KEY_USERNAME, userKey);
        claims.put(CLAIM_KEY_CREATED, new Date());
        Date expirationDate = getExpirationDate();
        String token = provide(claims, expirationDate);
        return new SlardarToken()
                .setTokenValue(token)
                .setExpiresAt(DateTimeUtil.fromDate(expirationDate));
    }

    /**
     * get username from token value
     *
     * @param tokenValue
     * @return
     */
    @Override
    public String geUserKey(String tokenValue) {
        String subject;
        try {
            Claims claims = getClaimsFromToken(tokenValue);
            subject = claims.getSubject();
        } catch (Exception e) {
            log.error("get subject from [{}] error: {}", tokenValue, e.getLocalizedMessage());
            subject = null;
        }
        return subject;
    }

    /**
     * 过期秒数
     *
     * @return
     */
    @Override
    public long getTokenTTL() {
        return expiration;
    }

    /**
     * 生成token
     */
    private String provide(Map<String, Object> claims, Date expiration) {
        // 确保密钥长度满足HS512要求（至少64字节）
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // 如果密钥太短，进行扩展
        if (keyBytes.length < 64) {
            // 使用更长的哈希算法或重复填充
            keyBytes = Arrays.copyOf(DigestUtils.sha512(secret), 64);
        }
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .compressWith(CompressionCodecs.DEFLATE)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成token的过期时间
     */
    private Date getExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    /**
     * 从token中获取过期时间
     */
    private Date getExpiredDataFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * validate token if expired or not
     *
     * @param tokenValue 令牌
     * @return 是否有效
     */
    private Boolean isExpired(String tokenValue) {
        try {
            Claims claims = getClaimsFromToken(tokenValue);
            Date expiration = claims.getExpiration();
            return new Date().before(expiration);
        } catch (Exception e) {
            log.error("validate {} error: {}", tokenValue, e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * 从token中获取jwt的负载
     */
    private Claims getClaimsFromToken(String token) {
        try {
            // 确保使用与签名相同的密钥格式
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 64) {
                keyBytes = Arrays.copyOf(DigestUtils.sha512(secret), 64);
            }
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(allowedClockSkewSeconds)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("JWT格式验证失败: {}", e.getLocalizedMessage());
            return null;
        }
    }
}