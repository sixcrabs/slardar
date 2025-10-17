package org.winterfell.slardar.starter.support.aop;

import org.winterfell.slardar.core.annotation.AuditLogger;
import org.winterfell.slardar.core.Constants;
import org.winterfell.slardar.core.SlardarSecurityHelper;
import org.winterfell.slardar.core.entity.Account;
import org.winterfell.slardar.core.entity.AuditLog;
import org.winterfell.slardar.core.entity.UserProfile;
import org.winterfell.slardar.starter.SlardarEventManager;
import org.winterfell.slardar.starter.support.event.AuditLogEvent;
import com.google.common.base.Joiner;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.winterfell.slardar.starter.support.HttpServletUtil.getDeviceType;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2024/1/3
 */
@Component
@Aspect
public class AuditLoggerAspect {

    @Resource
    private SlardarEventManager eventManager;

    private static final Logger logger = LoggerFactory.getLogger(AuditLoggerAspect.class);

    @Pointcut("@annotation(org.winterfell.slardar.core.annotation.AuditLogger)") // 指定切入点为所有标记了注解的地方
    public void logPointCut() {
    }

    /**
     * 后置通知，在目标方法执行之后进行操作
     *
     * @param joinPoint
     */
    @After("logPointCut()")
    public void beforeAdvice(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 获取请求信息
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String url = request.getRequestURL().toString();
        String ipAddress = request.getRemoteAddr();
        String methodName = method.getName();
        // 打印日志
        logger.info("IP Address : {}, URL : {}", ipAddress, url);
        logger.info("Executing method : {}", methodName);

        // 当前登录账号信息
        Account account = SlardarSecurityHelper.getAccount();
        UserProfile userProfile = SlardarSecurityHelper.getUserProfile();
        String accountName = Objects.isNull(account) ? Constants.ANONYMOUS : account.getName();
        String accountId = Objects.isNull(account) ? Constants.ANONYMOUS : account.getId();
        String userProfileId = Objects.isNull(userProfile) ? Constants.ANONYMOUS : userProfile.getId();
        String userProfileName = Objects.isNull(userProfile) ? Constants.ANONYMOUS : userProfile.getName();
        // 触发日志事件
        try {
            eventManager.dispatch(new AuditLogEvent(new AuditLog()
                    .setLogTime(LocalDateTime.now())
                    .setClientIp(ipAddress)
                    .setLogType(getAspectLogType(joinPoint))
                    .setClientType(getDeviceType(request).name())
                    .setDetail(Joiner.on("---").join(getAspectLogDetail(joinPoint), url, ipAddress, methodName))
                    .setAccountName(accountName)
                    .setAccountId(accountId)
                    .setUserProfileId(userProfileId)
                    .setUserProfileName(userProfileName)
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取切面注解的描述
     *
     * @param joinPoint 切点
     * @return 描述信息
     * @throws Exception
     */
    private String getAspectLogType(JoinPoint joinPoint)
            throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        StringBuilder description = new StringBuilder();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    description.append(method.getAnnotation(AuditLogger.class).type());
                    break;
                }
            }
        }
        return description.toString();
    }

    private String getAspectLogDetail(JoinPoint joinPoint)
            throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        StringBuilder description = new StringBuilder();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    description.append(method.getAnnotation(AuditLogger.class).detail());
                    break;
                }
            }
        }
        return description.toString();
    }
}
