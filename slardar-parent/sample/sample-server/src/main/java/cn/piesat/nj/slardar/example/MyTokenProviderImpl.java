package cn.piesat.nj.slardar.example;

import cn.hutool.core.util.RandomUtil;
import cn.piesat.nj.slardar.starter.SlardarContext;
import cn.piesat.nj.slardar.starter.token.SlardarTokenProvider;
import com.google.auto.service.AutoService;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

/**
 * <p>
 * .演示如何在 应用里 自定义 token 实现类
 * 启用需要在 配置文件里指定 `slardar.token.type: my`
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/17
 */
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
    public String type() {
        return "my";
    }

    /**
     * 初始化
     *
     * @param context
     */
    @Override
    public void initialize(SlardarContext context) {



    }

    /**
     * 生成 token
     *
     * @param userDetails
     * @return
     */
    @Override
    public Payload generate(UserDetails userDetails) {
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
        return "zhangsan";
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
