package cn.piesat.v.slardar.starter.support.event;

import cn.piesat.v.slardar.core.event.SlardarEvent;

import java.io.Serializable;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/4/2
 */
public abstract class BaseSlardarEvent<T extends Serializable> implements SlardarEvent<T> {

    protected T data;

    public BaseSlardarEvent(T data) {
        this.data = data;
    }

    /**
     * payload with the event
     *
     * @return
     */
    @Override
    public T payload() {
        return data;
    }
}
