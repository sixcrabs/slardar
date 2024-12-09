package cn.piesat.v.slardar.starter.support;

import cn.piesat.v.slardar.core.entity.Account;
import cn.piesat.v.slardar.starter.SlardarUserDetails;
import cn.piesat.v.slardar.starter.authenticate.SlardarAuthentication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import static cn.piesat.v.slardar.core.Constants.ANONYMOUS;

/**
 * <p>
 * .security util
 * </p>
 *
 * @author alex
 * @version v1.0 2022/9/27
 */
public final class SecUtil {

    private SecUtil() {
    }

    public static ObjectMapper objectMapper;

    static  {
        objectMapper = new ObjectMapper();
        // 默认 LocalDateTime 格式,主要是要注入这个JavaTimeModule
        JavaTimeModule timeModule = new JavaTimeModule();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        objectMapper.registerModule(timeModule);
    }

    /**
     * hasRole('') hasAnyRole('','')
     */
    public static final Pattern AUTH_ANNOTATION_PATTERN = Pattern.compile("(\\w+)\\((\\S*)\\)");

    public static final Gson GSON = new GsonBuilder().create();

    public static final String ROLE_NAME_PREFIX = "ROLE_";



    /**
     * 获取用户登录名
     *
     * @return
     */
    public static String getCurrentUsername() {
        SlardarAuthentication authentication = (SlardarAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return ANONYMOUS;
        } else {
            return String.valueOf(authentication.getPrincipal());
        }
    }

    public static boolean isAuthenticated() {
        SlardarAuthentication authentication = (SlardarAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return false;
        } else {
            return authentication.isAuthenticated();
        }
    }

    /**
     * get account
     *
     * @return
     */
    public static Account getAccount() {
        SlardarAuthentication authentication = (SlardarAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return new Account();
        } else {
            return authentication.getUserDetails().getAccount();
        }
    }

    /**
     * get user details
     *
     * @return
     */
    public static UserDetails getUserDetails() {
        SlardarAuthentication authentication = (SlardarAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return new SlardarUserDetails(new Account());
        } else {
            return authentication.getUserDetails();
        }
    }







}
