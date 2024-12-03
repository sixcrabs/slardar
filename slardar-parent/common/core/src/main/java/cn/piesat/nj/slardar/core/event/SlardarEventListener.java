package cn.piesat.nj.slardar.core.event;

import cn.piesat.nj.slardar.core.SlardarException;

/**
 * <p>
 * listener for {@link SlardarEvent}
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/31
 */
public interface SlardarEventListener<T extends SlardarEvent> {


    /**
     * handle event
     *
     * @param event
     * @throws SlardarException
     */
    void onEvent(T event) throws SlardarException;

    /**
     * support event or not
     *
     * @param eventClass
     * @return
     */
    boolean support(Class<T> eventClass);


    /**
     * 控制消费的次序 值越小越在前
     *
     * @return
     */
    default int ordinal() {
        return Integer.MAX_VALUE;
    }
}
