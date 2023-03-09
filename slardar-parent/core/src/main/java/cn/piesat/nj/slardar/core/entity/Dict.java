package cn.piesat.nj.slardar.core.entity;

import cn.piesat.nj.slardar.core.entity.core.BaseEntity;

import java.util.Objects;

/**
 * <p>
 * 字典 模型
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public class Dict extends BaseEntity<Long> {

    /**
     * 名称
     */
    private String name;

    /**
     * 键
     */
    private String key;

    /**
     * 字典项值
     */
    private String value;

    /**
     *  描述
     */
    private String description;

    /**
     * 权重 用于排序
     */
    private Integer weight;

    public String getName() {
        return name;
    }

    public Dict setName(String name) {
        this.name = name;
        return this;
    }

    public String getKey() {
        return key;
    }

    public Dict setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Dict setValue(String value) {
        this.value = value;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Dict setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getWeight() {
        return weight;
    }

    public Dict setWeight(Integer weight) {
        this.weight = weight;
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
        Dict dict = (Dict) o;
        return Objects.equals(name, dict.name) &&
                Objects.equals(key, dict.key) &&
                Objects.equals(value, dict.value) &&
                Objects.equals(description, dict.description) &&
                Objects.equals(weight, dict.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, key, value, description, weight);
    }
}
