package io.github.sixcrabs.slardar.spi.crypto;

import io.github.sixcrabs.slardar.core.SlardarException;
import io.github.sixcrabs.slardar.spi.SlardarSpi;

/**
 * <p>
 * 加密 SPI
 * </p>
 *
 * @author alex
 * @version v1.0 2023/11/20
 */
public interface SlardarCrypto extends SlardarSpi {

    /**
     * 加密
     * @param plaintext
     * @return
     */
    String encrypt(String plaintext) throws SlardarException;


    /**
     * 解密
     *
     * @param ciphertext
     * @return
     */
    String decrypt(String ciphertext) throws SlardarException;
}