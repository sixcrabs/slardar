package org.winterfell.slardar.core.domain.core;

import org.winterfell.slardar.core.domain.Group;
import org.winterfell.slardar.core.domain.Region;

import java.util.List;

/**
 * <p>
 * 支持树形递归的实体类型
 * {@link Group}
 * {@link Region}
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public abstract class BaseTreeLikeEntity<E> extends BaseRealmEntity<Long> {

    /**
     * 用于树形结构快速检索
     * : id-pid-ppid
     */
    private String searchKey;

    /**
     * 父级 id
     */
    private Long parentId;

    /**
     * 子对象
     */
    private List<E> children;


    public List<E> getChildren() {
        return children;
    }

    @Override
    public Long getId() {
        return super.getId();
    }

    @Override
    public BaseEntity<Long> setId(Long id) {
        return super.setId(id);
    }

    public BaseTreeLikeEntity<E> setChildren(List<E> children) {
        this.children = children;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }

    public BaseTreeLikeEntity<E> setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public BaseTreeLikeEntity setSearchKey(String searchKey) {
        this.searchKey = searchKey;
        return this;
    }
}
