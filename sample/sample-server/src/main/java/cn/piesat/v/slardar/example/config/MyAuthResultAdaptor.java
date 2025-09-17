package cn.piesat.v.slardar.example.config;

import cn.piesat.v.misc.hutool.mini.MapUtil;
import cn.piesat.v.slardar.core.AccountInfoDTO;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.spi.authenticate.SlardarAuthenticateResultAdapter;
import com.google.auto.service.AutoService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/12/10
 */
@AutoService(SlardarAuthenticateResultAdapter.class)
public class MyAuthResultAdaptor implements SlardarAuthenticateResultAdapter {
    /**
     * 认证成功结果
     *
     * @param accountInfoDTO 账户信息
     * @return
     */
    @Override
    public Map<String, Object> authSucceedResult(AccountInfoDTO accountInfoDTO) {
        HashMap<String, Object> map = MapUtil.newHashMap(3);
        map.put("token", accountInfoDTO.getToken());
        map.put("accountName", accountInfoDTO.getAccountName());
        map.put("username", accountInfoDTO.getUserProfile().getName());
        return map;
    }

    /**
     * 认证失败结果
     *
     * @param exception 认证异常
     * @return
     */
    @Override
    public Map<String, Object> authFailedResult(RuntimeException exception) {
        HashMap<String, Object> map = MapUtil.newHashMap(3);
        map.put("error", exception.getLocalizedMessage());
        map.put("code", 500);
        map.put("hint", "登录失败了");
        return map;
    }

    /**
     * 无权限访问结果
     *
     * @param exception 认证异常
     * @return
     */
    @Override
    public Map<String, Object> authDeniedResult(RuntimeException exception) {
        HashMap<String, Object> map = MapUtil.newHashMap(3);
        map.put("error", exception.getLocalizedMessage());
        map.put("code", 403);
        map.put("hint", "没有权限访问哦~");
        return map;
    }

    /**
     * 实现名称, 区分不同的 SPI 实现，必须
     *
     * @return
     */
    @Override
    public String name() {
        return "custom";
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
