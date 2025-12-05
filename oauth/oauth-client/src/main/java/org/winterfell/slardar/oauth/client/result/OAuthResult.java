package org.winterfell.slardar.oauth.client.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 授权结果
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthResult<T> implements Serializable {

    /**
     * 授权响应状态码
     */
    private int code;

    /**
     * 授权响应信息
     */
    private String msg;

    /**
     * 授权响应数据
     */
    private T data;
}