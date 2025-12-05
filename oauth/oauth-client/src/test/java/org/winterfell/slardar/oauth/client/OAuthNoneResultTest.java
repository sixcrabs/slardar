package org.winterfell.slardar.oauth.client;

import org.junit.jupiter.api.Test;
import org.winterfell.slardar.oauth.client.result.OAuthNoneResult;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/12/5
 */
class OAuthNoneResultTest {

    @Test
    void name() {
        OAuthNoneResult noneResult = new OAuthNoneResult();
        noneResult.setCode(500);
        noneResult.setMsg("none");
    }
}