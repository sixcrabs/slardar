package cn.piesat.nj.slardar.core.entity;

import cn.piesat.nj.slardar.core.entity.core.BaseTreeLikeEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 部门 模型
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public class Department extends BaseTreeLikeEntity<Department> {


    /**
     * 部门名称
     */
    private String name;

    /**
     * 别名
     */
    private String alias;

    /**
     * 描述
     */
    private String description;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    /**
     * 部门拥有的用户
     */
    private List<UserProfile> userProfiles;

    public List<UserProfile> getUserProfiles() {
        return userProfiles;
    }

    public Department setUserProfiles(List<UserProfile> userProfiles) {
        this.userProfiles = userProfiles;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public Department setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getName() {
        return name;
    }

    public Department setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Department setDescription(String description) {
        this.description = description;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Department setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Department that = (Department) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }
}
