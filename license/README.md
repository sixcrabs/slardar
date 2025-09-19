# slardar-license

- cli: 命令行工具 jar， 用于生成 公/私钥、lic授权文件(交给客户)，测试验证lic有效性等
- manager: 应用服务需要引入, 用于安装lic、验证lic有效性等（配合slardar-starter使用）

## 实现思路
生成公/私钥，使用私钥生成lic授权文件，将公钥和lic授权文件给到客户，客户将lic授权文件配置给应用服务，应用服务将lic授权文件解密，并验证lic授权文件是否有效
slardar-license-cli -----> 生成 lic & public key ----> 
lic 文件字段
- sign： 签名值
- data: customer 信息
- id: 唯一标识
- lastCheckDate: 加入一个可选的字段lastCheckTime，用于记录上一次检查的时间,每次验证许可证时，从状态文件中读取上一次检查的时间
,获取当前时间（可以尝试从NTP服务器获取，如果失败则使用系统时间，但标记为不可靠）,检查当前时间是否早于上一次检查的时间，如果是，则可能系统时间被回退，触发许可证无效,如果检查通过，
则更新上一次检查的时间为当前时间，并保存到状态文件中


```java
public class LicenseInfo {
    private String customerName;
    private String customerEmail;
    private String productCode;
    private int maxUsers;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String machineFingerprint;
    private Map<String, Object> features;
    private String licenseFile; // Base64编码的许可证文件
}
```