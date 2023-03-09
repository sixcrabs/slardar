package cn.piesat.nj.slardar.core.entity;

import cn.piesat.nj.slardar.core.entity.core.BaseRealmEntity;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色 实体
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public class Role extends BaseRealmEntity<String> {

    /**
     * 英文名称
     */
    private String name;

    /**
     * 中文别名
     */
    private String alias;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    /**
     * 关联的用户信息
     */
    private List<String> userIds;

    public String getName() {
        return name;
    }

    public Role setName(String name) {
        this.name = name;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public Role setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Role setDescription(String description) {
        this.description = description;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Role setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public Role setUserIds(List<String> userIds) {
        this.userIds = userIds;
        return this;
    }
}
