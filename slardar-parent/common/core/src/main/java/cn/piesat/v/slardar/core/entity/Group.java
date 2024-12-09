package cn.piesat.v.slardar.core.entity;

import cn.piesat.v.slardar.core.entity.core.BaseTreeLikeEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 组
 * - 用户组
 * - 角色组
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public class Group extends BaseTreeLikeEntity<Group> {

    /**
     * 组名称
     */
    private String name;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    /**
     * 关联的 用户 id
     */
    private List<String> userProfiles;

    /**
     *  关联的角色
     */
    private List<String> roles;

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Group setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public List<String> getUserProfiles() {
        return userProfiles;
    }

    public Group setUserProfiles(List<String> userProfiles) {
        this.userProfiles = userProfiles;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Group setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }
}
