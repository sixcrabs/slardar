package org.winterfell.slardar.core.domain;

import org.winterfell.slardar.core.domain.core.BaseRealmEntity;

import java.util.Map;

/**
 * <p>
 * 资源范围、client 访问范围等
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/13
 */
public class Scope extends BaseRealmEntity<String> {

    /**
     * 类型
     */
    private String type;

    /**
     * 名称 如: read/write/delete/...
     */
    private String name;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 描述
     */
    private String description;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    public String getType() {
        return type;
    }

    public Scope setType(String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public Scope setName(String name) {
        this.name = name;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public Scope setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Scope setDescription(String description) {
        this.description = description;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Scope setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }
}
