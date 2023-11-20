package cn.piesat.nj.slardar.starter.authenticate.mfa;

import cn.hutool.core.util.RandomUtil;
import cn.piesat.nj.skv.core.KvStore;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.core.entity.Account;
import cn.piesat.nj.slardar.spi.SlardarSpiFactory;
import cn.piesat.nj.slardar.spi.mfa.SlardarOtpDispatcher;
import cn.piesat.nj.slardar.starter.SlardarUserDetails;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.SecretGenerator;
import com.bastiaanjansen.otp.TOTPGenerator;
import com.google.gson.Gson;

import java.time.Duration;
import java.util.Objects;

import static cn.piesat.nj.slardar.starter.support.SecUtil.GSON;

/**
 * <p>
 * 处理 多因素认证
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/29
 */
public class SlardarMfaAuthService {

    private final SlardarSpiFactory dispatcherFactory;

    private final SlardarProperties slardarProperties;

    private final KvStore keyStore;

    /**
     * 有效期 秒 默认 5分钟
     */
    private static final Duration TTL = Duration.ofSeconds(60 * 5);

    private SlardarOtpDispatcher dispatcher;

    public SlardarMfaAuthService(SlardarSpiFactory dispatcherFactory, SlardarProperties slardarProperties, KvStore keyStore) {
        this.dispatcherFactory = dispatcherFactory;
        this.slardarProperties = slardarProperties;
        this.keyStore = keyStore;
        init();
    }

    private void init() {
        String otpMode = slardarProperties.getMfa().getOtpMode();
        try {
            dispatcher = dispatcherFactory.findOtpDispatcher(otpMode);
        } catch (SlardarException e) {
            e.printStackTrace();
        }
    }


    /**
     * 生成并发送 OTP
     *
     * @param userDetails
     * @return key 用于标记此次OTP记录
     * @throws SlardarException
     */
    public String generateAndDispatch(SlardarUserDetails userDetails) throws SlardarException {
        // 生成密钥
        byte[] secret = SecretGenerator.generate();
        String uuid = RandomUtil.simpleUUID();
        boolean b = keyStore.setex(uuid, new String(secret), TTL.toMillis() / 1000L);
        // 同时保存 user details
        keyStore.setex(uuid.concat("_account"), GSON.toJson(userDetails), (TTL.toMillis() / 1000L) + 5L);
        if (b) {
            // 创建 otp code
            TOTPGenerator generator = getGenerator(secret);
            String code = generator.now();
            dispatcher.dispatch(code, userDetails.getAccount());
            // TODO
            return uuid;
        }
        return null;
    }

    /**
     * 验证 otp code
     *
     * @param key
     * @param otpCode
     * @return
     * @throws SlardarException
     */
    public boolean verify(String key, String otpCode) throws SlardarException {
        String secret = keyStore.get(key);
        if (Objects.isNull(secret)) {
            throw new SlardarException("OTP 密码已过期!");
        }
        TOTPGenerator generator = getGenerator(secret.getBytes());
        // store里失效
        keyStore.setex(key, "", 1L);
        return generator.verify(otpCode);
    }


    public SlardarUserDetails getUserDetails(String key) {
        String accountKey = key.concat("_account");
        String value = keyStore.get(accountKey);
        keyStore.setex(accountKey, "", 1L);
        return GSON.fromJson(value, SlardarUserDetails.class);
    }


    private TOTPGenerator getGenerator(byte[] secret) {
        return new TOTPGenerator.Builder(secret)
                .withHOTPGenerator(builder -> {
                    // 一次性密码的长度
                    builder.withPasswordLength(7);
                    // 散列算法
                    builder.withAlgorithm(HMACAlgorithm.SHA512);
                })
                // 时间周期
                .withPeriod(TTL)
                .build();
    }


}
