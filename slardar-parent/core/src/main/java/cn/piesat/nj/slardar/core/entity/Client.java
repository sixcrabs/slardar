package cn.piesat.nj.slardar.core.entity;

import cn.piesat.nj.slardar.core.entity.core.BaseRealmEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 客户端 可以是一个web页面或 一个接口服务 或 一个第三方应用
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public class Client extends BaseRealmEntity<String> {

    /**
     * client id
     */
    private String clientId;

    /**
     * client secret
     */
    private String clientSecret;

    /**
     * 客户端允许访问的范围
     */
    private List<String> clientScopes;

    /**
     * admin url
     */
    private String adminUrl;

    /**
     * base url
     */
    private String baseUrl;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否禁用
     */
    private int disabled;

    /**
     * 允许全部scope
     */
    private int fullScopeAllowed;


    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;





}
