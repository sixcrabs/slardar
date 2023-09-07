package cn.piesat.nj.slardar.starter.token;

import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.v.shared.timer.cron.DateTimeUtil;
import com.google.auto.service.AutoService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;

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
public class SlardarTokenProviderJwtImpl implements SlardarTokenProvider {

    private static final String CLAIM_KEY_USERNAME = "sub";

    private static final String CLAIM_KEY_CREATED = "created";

    public static final String NAME = "jwt";

    private String secret;

    private Long expiration;

    private static final Logger log = LoggerFactory.getLogger(SlardarTokenProviderJwtImpl.class);


    /**
     * token 类型
     * - jwt
     * - ...
     *
     * @return
     */
    @Override
    public String type() {
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
        expiration = context.getBean(SlardarProperties.class).getToken().getJwt().getExpiration();
    }


    @Override
    public Payload generate(UserDetails userDetails) {
        return generate(userDetails.getUsername());

    }

    @Override
    public Payload generate(String username) {
        Map<String, Object> claims = new HashMap<>(16);
        claims.put(CLAIM_KEY_USERNAME, username);
        claims.put(CLAIM_KEY_CREATED, new Date());
        Date expirationDate = generateExpirationDate();
        String token = generate(claims, expirationDate);
        return new Payload()
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
    public String getSubject(String tokenValue) {
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
     * validate token if expired or not
     *
     * @param tokenValue 令牌
     * @return 是否有效
     */
    @Override
    public Boolean isExpired(String tokenValue) {
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
     * 过期秒数
     *
     * @return
     */
    @Override
    public long getExpiration() {
        return expiration;
    }


    /**
     * 生成token
     */
    private String generate(Map<String, Object> claims, Date expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .compressWith(CompressionCodecs.DEFLATE)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 生成token的过期时间
     */
    private Date generateExpirationDate() {
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
     * 从token中获取jwt的负载
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.info("JWT格式验证失败");
        }
        return claims;
    }
}
