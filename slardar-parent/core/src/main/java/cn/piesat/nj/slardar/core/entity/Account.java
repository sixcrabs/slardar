package cn.piesat.nj.slardar.core.entity;

import cn.piesat.nj.slardar.core.AccountStatus;
import cn.piesat.nj.slardar.core.entity.core.BaseRealmEntity;

import java.util.List;

/**
 * <p>
 * 账号
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/9
 */
public class Account extends BaseRealmEntity<String> {

    /**
     * 当前账号访问状态
     */
    private AccountStatus status;

    /**
     * 关联的用户组id
     */
    private List<String> groups;

    /**
     * 绑定的角色 id
     */
    private List<String> roles;

    /**
     * 是否可用
     * @return
     */
    public boolean isAccessible() {
        return AccountStatus.accessible.equals(this.status);
    }
}
