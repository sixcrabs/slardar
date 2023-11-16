package cn.piesat.nj.slardar.sso.client.config.client;

import cn.piesat.nj.cloud.mrc.MrClient;
import cn.piesat.nj.slardar.core.entity.Account;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
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
     * @return
     */
    @POST("userDetails")
    RestApiResult<Account> getUserDetails(@Header("Authorization") String token);




}
