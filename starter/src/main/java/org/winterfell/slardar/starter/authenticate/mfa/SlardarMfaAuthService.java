package org.winterfell.slardar.starter.authenticate.mfa;

import cn.piesat.v.misc.hutool.mini.RandomUtil;
import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.spi.SlardarKeyStore;
import org.winterfell.slardar.spi.SlardarSpiFactory;
import org.winterfell.slardar.spi.mfa.SlardarOtpDispatcher;
import org.winterfell.slardar.starter.SlardarUserDetails;
import org.winterfell.slardar.starter.config.SlardarProperties;
//import cn.piesat.v.skv.core.KvStore;
import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.SecretGenerator;
import com.bastiaanjansen.otp.TOTPGenerator;

import java.time.Duration;
import java.util.Objects;

import static org.winterfell.slardar.starter.support.SecUtil.GSON;

/**
 * <p>
 * 处理 多因素认证
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/29
 */
public class SlardarMfaAuthService {

    private final SlardarSpiFactory spiFactory;

    private final SlardarProperties slardarProperties;

    private final SlardarKeyStore keyStore;

    /**
     * 有效期 秒 默认 5分钟
     */
    private static final Duration TTL = Duration.ofSeconds(60 * 5);

    private SlardarOtpDispatcher dispatcher;

    public SlardarMfaAuthService(SlardarSpiFactory spiFactory, SlardarProperties slardarProperties) {
        this.spiFactory = spiFactory;
        this.slardarProperties = slardarProperties;
        this.keyStore = spiFactory.findKeyStore(slardarProperties.getKeyStore().getType());
        init();
    }

    private void init() {
        dispatcher = spiFactory.findOtpDispatcher(slardarProperties.getMfa().getOtpMode());
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
        String uuid = RandomUtil.randomString(16);
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
