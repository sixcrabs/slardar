package org.winterfell.slardar.core.domain;

import org.winterfell.slardar.core.domain.core.BaseEntity;

import java.util.Map;

/**
 * <p>
 * 权限 实体
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/1/9
 */
public class Authority extends BaseEntity<String> {

    /**
     * 权限内容
     * eg: read_info
     * /home
     * read_btn
     */
    private String content;

    /**
     * 描述
     */
    private String description;

    /**
     * 备用字段
     */
    private String type;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    public String getContent() {
        return content;
    }

    public Authority setContent(String content) {
        this.content = content;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Authority setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public Authority setType(String type) {
        this.type = type;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Authority setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }
}
