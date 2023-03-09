package cn.piesat.nj.slardar.core.entity;

import cn.piesat.nj.slardar.core.entity.core.BaseTreeLikeEntity;

import java.util.Map;

/**
 * <p>
 * 行政区域对象
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public class Region extends BaseTreeLikeEntity<Region> {

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
}
