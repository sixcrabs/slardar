package cn.piesat.nj.slardar.core.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * 权限 实体
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/1/9
 */
public class Authority implements Serializable {

    private String id;

    private String scopeId;

    private String resourceId;

    private String roleId;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    private Scope scope;

    private Resource resource;

    private Role role;


}
