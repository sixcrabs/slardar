package org.winterfell.slardar.spi.crypto;

import org.winterfell.slardar.core.SlardarException;
import org.winterfell.slardar.spi.SlardarSpi;

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
