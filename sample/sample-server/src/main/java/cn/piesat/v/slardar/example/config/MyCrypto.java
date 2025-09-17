package cn.piesat.v.slardar.example.config;

import cn.piesat.v.misc.hutool.mini.codec.Base64Decoder;
import cn.piesat.v.misc.hutool.mini.codec.Base64Encoder;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.spi.crypto.SlardarCrypto;
import com.google.auto.service.AutoService;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/12/10
 */
@AutoService(SlardarCrypto.class)
public class MyCrypto implements SlardarCrypto {
    /**
     * 加密
     *
     * @param plaintext
     * @return
     */
    @Override
    public String encrypt(String plaintext) throws SlardarException {
        return Base64Encoder.encode(plaintext);
    }

    /**
     * 解密
     *
     * @param ciphertext
     * @return
     */
    @Override
    public String decrypt(String ciphertext) throws SlardarException {
        return Base64Decoder.decodeStr(ciphertext);
    }

    /**
     * 实现名称, 区分不同的 SPI 实现，必须
     *
     * @return
     */
    @Override
    public String name() {
        return "mystical";
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void initialize(SlardarSpiContext context) {
        // 这里可以获取到容器里注入的 bean、配置等上下文环境
    }
}
