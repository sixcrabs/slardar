package org.winterfell.slardar.core.domain.core;

import java.io.Serializable;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/13
 */
public abstract class BaseEntity<T> implements Serializable {

    private T id;

    /**
     * 逻辑删除
     */
    private Integer deleted = 0;

    public T getId() {
        return id;
    }

    public BaseEntity<T> setId(T id) {
        this.id = id;
        return this;
    }

    public boolean isDeleted() {
        return deleted==1;
    }

    public BaseEntity setDeleted(Integer deleted) {
        this.deleted = deleted;
        return this;
    }
}
