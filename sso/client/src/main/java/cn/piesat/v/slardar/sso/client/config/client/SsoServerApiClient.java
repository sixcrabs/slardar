package cn.piesat.v.slardar.sso.client.config.client;

import cn.piesat.v.remote.mrc.MrClient;
import cn.piesat.v.slardar.core.entity.Account;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/4/3
 */
@MrClient(name = "sso-server-client", url = "${slardar.sso.server-url}")
public interface SsoServerApiClient {

    /**
     * check ticket
     * @param ticket
     * @return
     */
    @POST("checkTicket")
    RestApiResult<String> checkTicket(@Query("ticket") String ticket);

    /**
     * 获取用户详情
     * @param token
     * @param userAgent  用于传递真实请求的 设备头信息（用于判断设备类型是 APP / PC）
     * @return
     */
    @POST("userDetails")
    RestApiResult<Account> getUserDetails(@Header("Authorization") String token, @Header("User-Agent") String userAgent);




}
