package org.winterfell.slardar.core.entity;

import org.winterfell.slardar.core.entity.core.BaseTreeLikeEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 行政区域对象
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public class Region extends BaseTreeLikeEntity<Region> implements Cloneable {

    /**
     * 行政区名称
     */
    private String name;

    /**
     * 行政区代码
     */
    private String code;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    public String getName() {
        return name;
    }

    public Region setName(String name) {
        this.name = name;
        return this;
    }

    public String getCode() {
        return code;
    }

    public Region setCode(String code) {
        this.code = code;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Region setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public Region clone() {
        try {
            Region clone = (Region) super.clone();
            clone.setName(this.name).setCode(this.code);
            if (this.attributes!=null) {
                clone.setAttributes(new HashMap<>(this.attributes));
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
