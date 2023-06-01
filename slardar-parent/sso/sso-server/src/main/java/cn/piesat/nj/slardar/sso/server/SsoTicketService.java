package cn.piesat.nj.slardar.sso.server;

import cn.hutool.core.util.RandomUtil;
import cn.piesat.nj.skv.core.KvStore;
import cn.piesat.nj.slardar.sso.server.config.SsoServerProperties;
import cn.piesat.nj.slardar.sso.server.support.SsoException;

import javax.annotation.Resource;

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

    @Resource
    private KvStore kvStore;

    private final SsoServerProperties serverProperties;

    private final SsoServerProperties.TicketSetting ticketSetting;


    public SsoTicketService(SsoServerProperties serverProperties) {
        this.serverProperties = serverProperties;
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
        // set ttl
        kvStore.setex(ticket, token, ticketSetting.getTtl().getSeconds());
        return ticket;
    }

    /**
     *
     * @param ticketValue
     * @return
     */
    public String checkTicket(String ticketValue) {
        return kvStore.get(ticketValue);
    }
}
