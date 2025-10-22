package org.winterfell.slardar.ext.firewall;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/10/22
 */
public final class SlardarFirewallUtil {

    /**
     * 字符串模糊匹配
     * <p>example:
     * <p> user* user-add   --  true
     * <p> user* art-add    --  false
     *
     * @param patt 表达式
     * @param str  待匹配的字符串
     * @return 是否可以匹配
     */
    public static boolean vagueMatch(String patt, String str) {
        // 两者均为 null 时，直接返回 true
        if (patt == null && str == null) {
            return true;
        }
        // 两者其一为 null 时，直接返回 false
        if (patt == null || str == null) {
            return false;
        }
        // 如果表达式不带有*号，则只需简单equals即可 (这样可以使速度提升200倍左右)
        if (!patt.contains("*")) {
            return patt.equals(str);
        }
        // 深入匹配
        return vagueMatchMethod(patt, str);
    }

    /**
     * 字符串模糊匹配
     *
     * @param pattern /
     * @param str     /
     * @return /
     */
    private static boolean vagueMatchMethod(String pattern, String str) {
        int m = str.length();
        int n = pattern.length();
        boolean[][] dp = new boolean[m + 1][n + 1];
        dp[0][0] = true;
        for (int i = 1; i <= n; ++i) {
            if (pattern.charAt(i - 1) == '*') {
                dp[0][i] = true;
            } else {
                break;
            }
        }
        for (int i = 1; i <= m; ++i) {
            for (int j = 1; j <= n; ++j) {
                if (pattern.charAt(j - 1) == '*') {
                    dp[i][j] = dp[i][j - 1] || dp[i - 1][j];
                } else if (str.charAt(i - 1) == pattern.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                }
            }
        }
        return dp[m][n];
    }
}
