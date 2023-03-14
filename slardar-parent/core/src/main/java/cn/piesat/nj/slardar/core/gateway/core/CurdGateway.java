package cn.piesat.nj.slardar.core.gateway.core;

import cn.piesat.nj.slardar.core.entity.core.BaseEntity;

import java.io.Serializable;
import java.util.Collection;

/**
 * <p>
 * 基础 增删改查 gateway
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/14
 */
public interface CurdGateway<ID extends Serializable, E extends BaseEntity<ID>> {


    /**
     * 创建实体
     *
     * @param entity
     * @return
     */
    ID create(E entity);

    /**
     * 更新实体
     *
     * @param entity
     * @return
     */
    boolean update(E entity);

    /**
     * delete by ID
     *
     * @param id
     * @return
     */
    boolean deleteById(ID id);

    /**
     * get by id
     *
     * @param id
     * @return
     */
    E getById(ID id);

}
