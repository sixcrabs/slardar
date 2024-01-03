package cn.piesat.nj.slardar.example.event.aop;

import cn.piesat.nj.slardar.core.AuditLogIngest;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.core.entity.AuditLog;
import cn.piesat.nj.slardar.example.event.AccessLogEvent;
import cn.piesat.nj.slardar.starter.SlardarEventManager;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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
public class AccessLoggerAspect {


    @Resource
    private SlardarEventManager eventManager;

    private static final Logger logger = LoggerFactory.getLogger(AccessLoggerAspect.class);

    @Pointcut("@annotation(cn.piesat.nj.slardar.example.event.aop.AccessLog)") // 指定切入点为所有标记了注解的地方
    public void logPointCut(){};

    @Before("logPointCut()") // 前置通知，在目标方法执行之前进行操作
    public void beforeAdvice(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取请求信息
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String url = request.getRequestURL().toString();
        String ipAddress = request.getRemoteAddr();
        String methodName = method.getName();

        // 打印日志
        logger.info("IP Address : {}, URL : {}", ipAddress, url);
        logger.info("Executing method : {}", methodName);

        // 触发日志事件
        try {
            eventManager.dispatch(new AccessLogEvent(new AuditLog()
                    .setLogTime(LocalDateTime.now())
                    .setClientType("")
                    .setDetail(Joiner.on("---").join(url, ipAddress, methodName))
                    .setAccountName("游客")
                    .setAccountId("9999999")
                    .setUserProfileId("9999999")
                    .setUserProfileName("匿名用户")
            ));
        } catch (SlardarException e) {
            throw new RuntimeException(e);
        }
    }
}
