package cn.piesat.v.slardar.oauth.server;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/4/15
 */
public class OauthServerRequestHandlerTest {


    @Test
    public  void testUri () {
        ArrayList<String> uris = Lists.newArrayList("/oauth/authorize?\n" +
                "  response_type=code&\n" +
                "  client_id=CLIENT_ID&\n" +
                "  redirect_uri=CALLBACK_URL&\n" +
                "  scope=read", "/oauth/token?\n" +
                " client_id=CLIENT_ID&\n" +
                " client_secret=CLIENT_SECRET&\n" +
                " grant_type=authorization_code&\n" +
                " code=AUTHORIZATION_CODE&\n" +
                " redirect_uri=CALLBACK_URL");
        for (String uri : uris) {
            String mapping = uri.replace("/oauth", "").replaceFirst("/", "");
            System.out.println(mapping);
        }

    }

}