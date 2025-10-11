package org.winterfell.slardar.starter.support;

import cn.piesat.v.misc.hutool.mini.ReUtil;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * <p>
 * .
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/26
 */
class SecUtilTest {

    @Test
    void testRegex() {
        ArrayList<String> list =
                Lists.newArrayList("hasRole('ADMIN')",
                        "hasAnyRole('ADMIN','NORMAL')", "permitAll()", "denyAll()", "error('xx')");
        for (String str : list) {
            String method = ReUtil.getGroup1(SecUtil.AUTH_ANNOTATION_PATTERN, str);
            String args = ReUtil.get(SecUtil.AUTH_ANNOTATION_PATTERN, str, 2);
            System.out.print(method);
            System.out.print("-------------");
            System.out.println(args);
        }

    }
}