package org.winterfell.slardar.starter.support.store;

import cn.piesat.v.misc.hutool.mini.*;
import cn.piesat.v.misc.hutool.mini.crypto.SmUtil;
import cn.piesat.v.misc.hutool.mini.crypto.symmetric.SM4;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @since 2025/5/21
 */
public final class KeyStoreUtil {

    /**
     * 匹配加密字符串 ENC(xxx) 则 xxx 为加密字符串
     */
    private final static String CIPHER_PATTERN = "^ENC\\((\\S*)\\)$";
    private final static String SM4_KEY = "AAFFEEDCBA2024032800009876543210";

    private static SM4 sm4;

    public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    static {
        sm4 = SmUtil.sm4(HexUtil.decodeHex(SM4_KEY));
    }

    /**
     * sm4 加密
     *
     * @param data value data
     * @return
     */
    public static String encrypt(String data) {
        try {
            return sm4.encryptHex(data);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            return StringUtil.EMPTY;
        }
    }

    /**
     * aes 对称解密
     *
     * @param encryptStr encrypted string
     * @return
     */
    public static String decrypt(String encryptStr) {
        String realValue = encryptStr;
        if (isEncrypted(encryptStr)) {
            // 先取出密文
            realValue = ReUtil.getAllGroups(Pattern.compile(CIPHER_PATTERN), encryptStr, false).get(0);
        }
        try {
            return sm4.decryptStr(realValue, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
        return StringUtil.EMPTY;
    }

    /**
     * 是否是加密后的字符串（需要解密后返回）
     *
     * @param value
     * @return
     */
    public static boolean isEncrypted(String value) {
        return value.matches(CIPHER_PATTERN);
    }

    public static String getEncryptedFromRedisUri(@NotNull String uri) {
        String param = uri.substring(8);
        String[] arr = param.split("@", 2);
        if (arr[0].contains("ENC(")) {
            // 从ENC(开始取
            return arr[0].substring(arr[0].indexOf("ENC("));
        }
        return StringUtil.EMPTY;
    }

    /**
     * 将设置的值字符串化
     *
     * @param obj
     * @return
     */
    public static String stringify(Object obj) {
        if (null == obj) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof byte[]) {
            return StringUtil.str((byte[]) obj, StringUtil.CHARSET_UTF_8);
        } else if (obj instanceof Byte[]) {
            return StringUtil.str((Byte[]) obj, StringUtil.CHARSET_UTF_8);
        } else if (obj instanceof ByteBuffer) {
            return StringUtil.str((ByteBuffer) obj, StringUtil.CHARSET_UTF_8);
        } else if (ArrayUtil.isArray(obj)) {
            return ArrayUtil.toString(obj);
        } else if (isPrimitive(obj.getClass())) {
            return String.valueOf(obj);
        }
        // TESTME: 采用Gson序列化
        return GSON.toJson(obj);
    }

    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || clazz == String.class || int.class.isAssignableFrom(clazz) || clazz == Integer.class
                || clazz == Float.class || float.class.isAssignableFrom(clazz)
                || clazz == Double.class || double.class.isAssignableFrom(clazz)
                || clazz == Boolean.class || boolean.class.isAssignableFrom(clazz)
                || clazz == Long.class || long.class.isAssignableFrom(clazz);
    }

    /**
     * stringToPrimitive
     *
     * @param str
     * @param type
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T stringToPrimitive(String str, Class<T> type) {
        if (isPrimitive(type)) {
            if (String.class.equals(type)) {
                return (T) str;
            }
            if (int.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type)) {
                return (T) Integer.valueOf(str);
            } else if (float.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type)) {
                return (T) Float.valueOf(str);
            } else if (double.class.isAssignableFrom(type) || Double.class.isAssignableFrom(type)) {
                return (T) Double.valueOf(str);
            } else if (short.class.isAssignableFrom(type) || Short.class.isAssignableFrom(type)) {
                return (T) Short.valueOf(str);
            } else if (long.class.isAssignableFrom(type) || Long.class.isAssignableFrom(type)) {
                return (T) Long.valueOf(str);
            } else if (boolean.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)) {
                return (T) Boolean.valueOf(str);
            }
        }
        return (T) str;
    }


}
