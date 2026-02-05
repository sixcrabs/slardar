package io.github.sixcrabs.slardar.sso.server;

import org.winterfell.misc.hutool.mini.RandomUtil;
import io.github.sixcrabs.slardar.spi.SlardarSpiFactory;
import io.github.sixcrabs.slardar.sso.server.config.SsoServerProperties;
import io.github.sixcrabs.slardar.spi.SlardarKeyStore;
import io.github.sixcrabs.slardar.starter.SlardarProperties;

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

    private final SlardarKeyStore keyStore;

    private final SsoServerProperties.TicketSetting ticketSetting;

    public SsoTicketService(SlardarSpiFactory spiFactory, SlardarProperties slardarProperties, SsoServerProperties serverProperties) {
        this.ticketSetting = serverProperties.getTicket();
        this.keyStore = spiFactory.findKeyStore(slardarProperties.getKeyStore().getType());
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