package io.github.sixcrabs.slardar.oauth.client;

import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Data;
import io.github.sixcrabs.slardar.oauth.client.token.OAuthToken;

/**
 * <p>
 * oauth 第三方平台的用户
 * 各平台可根据需要自定义实现
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@Data
@Builder
public class OAuthUser {

    /**
     * 用户第三方系统的唯一id。在调用方集成该组件时，可以用uuid + source唯一确定一个用户
     *
     * @since 1.3.3
     */
    private String uuid;
    /**
     * 用户名
     */
    private String username;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户头像
     */
    private String avatar;
    /**
     * 用户网址
     */
    private String blog;
    /**
     * 所在公司
     */
    private String company;
    /**
     * 位置
     */
    private String location;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 用户备注（各平台中的用户个人介绍）
     */
    private String remark;
    /**
     * 性别
     */
    private String gender;
    /**
     * 用户来源
     */
    private String source;

    /**
     * 用户授权的token信息
     */
    private OAuthToken token;

    /**
     * 第三方平台返回的原始用户信息
     */
    private JsonObject rawInfo;

    /**
     * 微信公众号 - 网页授权的登录时可用
     * <p>
     * 微信针对网页授权登录，增加了一个快照页的逻辑，快照页获取到的微信用户的 uid oid 和头像昵称都是虚拟的信息
     */
    private boolean snapshotUser;
}