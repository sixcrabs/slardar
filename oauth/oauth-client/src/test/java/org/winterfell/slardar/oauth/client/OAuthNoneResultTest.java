package org.winterfell.slardar.oauth.client;

import org.junit.jupiter.api.Test;
import io.github.sixcrabs.slardar.oauth.client.result.OAuthEmptyResult;

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
        OAuthEmptyResult noneResult = new OAuthEmptyResult();
        noneResult.setCode(500);
        noneResult.setMsg("none");
    }
}