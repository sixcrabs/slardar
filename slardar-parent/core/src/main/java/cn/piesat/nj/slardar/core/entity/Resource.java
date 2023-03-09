package cn.piesat.nj.slardar.core.entity;

import cn.piesat.nj.slardar.core.entity.core.BaseTreeLikeEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 资源
 * - 前端页面菜单项
 * - 后端接口url
 * - 其他任意对象
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public class Resource extends BaseTreeLikeEntity<Resource> {

    /**
     * 资源类型
     */
    private String type;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 资源别名
     */
    private String alias;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    /**
     * 具有权限的 角色id
     */
    private List<String> roles;

    /**
     * 权重 用于排序
     */
    private Integer weight;

    public String getType() {
        return type;
    }

    public Resource setType(String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public Resource setName(String name) {
        this.name = name;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public Resource setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Resource setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Resource setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public Integer getWeight() {
        return weight;
    }

    public Resource setWeight(Integer weight) {
        this.weight = weight;
        return this;
    }
}
