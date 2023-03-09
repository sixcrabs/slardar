package cn.piesat.nj.slardar.core.entity;

import cn.piesat.nj.slardar.core.entity.core.BaseEntity;

import java.util.List;
import java.util.Map;

import static cn.piesat.nj.slardar.core.Constants.REALM_MASTER;

/**
 * <p>
 * realm 模型 (租户) 对数据进行隔离
 * realm 下包含 user、client、role、department、group、 region
 * TODO:
 * - 过期时间
 * - 账号额度
 * - 绑定域名
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public class Realm extends BaseEntity<String> {

    /**
     * 租户名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    /**
     * 领域下的用户
     */
    private List<UserProfile> userProfiles;

    /**
     * 是否是 master 租户
     *
     * @return
     */
    public boolean isMaster() {
        return REALM_MASTER.equalsIgnoreCase(name);
    }


    public String getName() {
        return name;
    }

    public Realm setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Realm setDescription(String description) {
        this.description = description;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Realm setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public List<UserProfile> getUserProfiles() {
        return userProfiles;
    }

    public Realm setUserProfiles(List<UserProfile> userProfiles) {
        this.userProfiles = userProfiles;
        return this;
    }
}
