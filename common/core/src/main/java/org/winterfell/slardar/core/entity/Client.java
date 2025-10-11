package org.winterfell.slardar.core.entity;

import org.winterfell.slardar.core.entity.core.BaseRealmEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 客户端 可以是一个web页面或 一个接口服务 或 一个第三方应用
 * oauth 规范里的 client 概念
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
     * 所有允许授权的 url
     * `*` 表示允许所有
     */
    private String redirectUrisAllowed;

    /**
     * 所有允许的授权模式
     * authorization_code\password\client_credentials\refresh_token
     */
    private String grantTypesAllowed;

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


    public String getRedirectUrisAllowed() {
        return redirectUrisAllowed;
    }

    public Client setRedirectUrisAllowed(String redirectUrisAllowed) {
        this.redirectUrisAllowed = redirectUrisAllowed;
        return this;
    }

    public String getGrantTypesAllowed() {
        return grantTypesAllowed;
    }

    public Client setGrantTypesAllowed(String grantTypesAllowed) {
        this.grantTypesAllowed = grantTypesAllowed;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public Client setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Client setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public List<String> getClientScopes() {
        return clientScopes;
    }

    public Client setClientScopes(List<String> clientScopes) {
        this.clientScopes = clientScopes;
        return this;
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public Client setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
        return this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Client setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Client setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getDisabled() {
        return disabled;
    }

    public Client setDisabled(int disabled) {
        this.disabled = disabled;
        return this;
    }

    public int getFullScopeAllowed() {
        return fullScopeAllowed;
    }

    public Client setFullScopeAllowed(int fullScopeAllowed) {
        this.fullScopeAllowed = fullScopeAllowed;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Client setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }
}
