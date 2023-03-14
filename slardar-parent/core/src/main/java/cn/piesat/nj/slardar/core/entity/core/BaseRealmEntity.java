package cn.piesat.nj.slardar.core.entity.core;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public abstract class BaseRealmEntity<T> extends BaseEntity<T> {

    /**
     * 可以是 name 或 id
     */
    private String realm;

    public String getRealm() {
        return realm;
    }

    public BaseRealmEntity<T> setRealm(String realm) {
        this.realm = realm;
        return this;
    }
}
