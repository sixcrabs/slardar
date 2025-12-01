package org.winterfell.slardar.sso.server;

import org.winterfell.misc.hutool.mini.RandomUtil;
import org.winterfell.misc.keystore.SimpleKeyStore;
import org.winterfell.slardar.sso.server.config.SsoServerProperties;

/**
 * <p>
 * ticket 生成 验证...
 * TODO:
 * token 注销时 需要清空 ticket
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/30
 */
public class SsoTicketService {

    private final SimpleKeyStore keyStore;

    private final SsoServerProperties.TicketSetting ticketSetting;

    public SsoTicketService(SimpleKeyStore keyStore, SsoServerProperties serverProperties) {
        this.keyStore = keyStore;
        this.ticketSetting = serverProperties.getTicket();
    }

    /**
     * 创建  ticket
     * @param token
     * @return
     */
    public String createTicket(String token) {
        String ticket = RandomUtil.randomString("ABCDEFGHIJKMNRST123456789",
                ticketSetting.getLength() > 0 ? ticketSetting.getLength() : 12);
        keyStore.setex(ticket, token, ticketSetting.getTtl().getSeconds());
        return ticket;
    }

    /**
     *
     * @param ticketValue
     * @return
     */
    public String checkTicket(String ticketValue) {
        return keyStore.get(ticketValue);
    }
}