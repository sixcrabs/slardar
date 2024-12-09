package cn.piesat.v.slardar.core.entity;

import cn.piesat.v.slardar.core.entity.core.BaseRealmEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户信息
 * </p>
 *
 * @author Alex
 * @version v1.0 2022/12/12
 */
public class UserProfile extends BaseRealmEntity<String> {


    /**
     * 用户姓名
     */
    private String name;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 电话
     */
    private String telephone;

    /**
     * 地址
     */
    private String address;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    /**
     * 用户所属的部门id
     */
    private String departmentId;

    /**
     * 关联的行政区
     */
    private Region region;

    /**
     * 所属部门对象
     */
    private Department department;

    /**
     * 具有的角色列表
     */
    private List<Role> roles;

    /**
     * 关联的权限内容列表
     */
    private List<Authority> authorities;


    public List<Authority> getAuthorities() {
        return authorities;
    }

    public UserProfile setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
        return this;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public UserProfile setRoles(List<Role> roles) {
        this.roles = roles;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserProfile setName(String name) {
        this.name = name;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public UserProfile setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserProfile setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getTelephone() {
        return telephone;
    }

    public UserProfile setTelephone(String telephone) {
        this.telephone = telephone;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public UserProfile setAddress(String address) {
        this.address = address;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public UserProfile setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public UserProfile setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
        return this;
    }

    public Region getRegion() {
        return region;
    }

    public UserProfile setRegion(Region region) {
        this.region = region;
        return this;
    }

    public Department getDepartment() {
        return department;
    }

    public UserProfile setDepartment(Department department) {
        this.department = department;
        return this;
    }
}
