package cn.piesat.nj.slardar.core.event;

import java.io.Serializable;

/**
 * <p>
 * event
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/31
 */
public interface SlardarEvent<T extends Serializable> {

    /**
     * payload with the event
     * @return
     */
    T payload();
}
