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

    private String realmId;

    public String getRealmId() {
        return realmId;
    }

    public BaseRealmEntity setRealmId(String realmId) {
        this.realmId = realmId;
        return this;
    }
}
