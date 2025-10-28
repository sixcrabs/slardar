package org.winterfell.slardar.license.core;

import java.io.Serializable;

/**
 * <p>
 * 许可对象
 * </p>
 *
 * @author Alex
 * @since 2025/9/18
 */
public class License implements Serializable {

    private String id;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 客户邮箱
     */
    private String customerEmail;

    /**
     * 客户联系方式
     */
    private String customerContact;

    /**
     * 产品名称
     */
    private String productCode;

    /**
     * 过期时间
     */
    private String expiryDate;

    /**
     * 签发时间
     */
    private String issueDate;

    /**
     * 签发人
     */
    private String issuedBy;

    /**
     * 最大用户数
     */
    private int maxUsers;

    /**
     * 机器指纹 用于机器验证
     */
    private String machineFingerprint;


    public String getId() {
        return id;
    }

    public License setId(String id) {
        this.id = id;
        return this;
    }

    public String getCustomerName() {
        return customerName;
    }

    public License setCustomerName(String customerName) {
        this.customerName = customerName;
        return this;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public License setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
        return this;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public License setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
        return this;
    }

    public String getProductCode() {
        return productCode;
    }

    public License setProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public License setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public License setIssueDate(String issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public License setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
        return this;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public License setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
        return this;
    }

    public String getMachineFingerprint() {
        return machineFingerprint;
    }

    public License setMachineFingerprint(String machineFingerprint) {
        this.machineFingerprint = machineFingerprint;
        return this;
    }
}
