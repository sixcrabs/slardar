package cn.piesat.nj.slardar.starter.authenticate.handler;

import cn.hutool.core.util.StrUtil;
import cn.piesat.nj.slardar.core.Constants;
import cn.piesat.nj.slardar.core.SlardarException;
import cn.piesat.nj.slardar.spi.SlardarSpiContext;
import cn.piesat.nj.slardar.spi.SlardarSpiFactory;
import cn.piesat.nj.slardar.spi.crypto.SlardarCrypto;
import cn.piesat.nj.slardar.starter.SlardarUserDetails;
import cn.piesat.nj.slardar.starter.SlardarUserDetailsServiceImpl;
import cn.piesat.nj.slardar.starter.authenticate.SlardarAuthentication;
import cn.piesat.nj.slardar.starter.authenticate.mfa.MfaVerifyRequiredException;
import cn.piesat.nj.slardar.starter.authenticate.mfa.SlardarMfaAuthService;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.support.RequestWrapper;
import cn.piesat.nj.slardar.starter.support.captcha.CaptchaComponent;
import cn.piesat.v.shared.timer.TimerManager;
import cn.piesat.v.shared.timer.job.TimerJobs;
import com.google.auto.service.AutoService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/23
 */
@AutoService(SlardarAuthenticateHandler.class)
public class DefaultSlardarAuthenticateHandlerImpl extends AbstractSlardarAuthenticateHandler {

    public static final String NAME = "default";

    private static final String LOCKED_KEY = "locked_";

    private static final Map<String, Integer> FAILED_ATTEMPTS_REPO = new WeakHashMap<>(1);

    private RedisClient redisClient;

    private Integer maxAttempts = 5;

    private RedisCommands<String, String> stringCommands;

    private static final ExecutorService FAILED_POOL = new ThreadPoolExecutor(16, 64,
            10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(128), new ThreadFactoryBuilder()
            .setNameFormat("auth-failed-thread-%d").setDaemon(true).build(),
            new ThreadPoolExecutor.AbortPolicy());


    /**
     * 注入 context
     *
     * @param context
     */
    @Override
    public void setSlardarContext(SlardarSpiContext context) {
        super.setSlardarContext(context);
        redisClient = context.getBean(RedisClient.class);
        stringCommands = redisClient.connect().sync();
        maxAttempts = getProperties().getLogin().getMaxAttemptsBeforeLocked();
        // 定时器
        TimerManager timerManager = new TimerManager(TimerManager.TimerConfig.DEFAULT);
        timerManager.addTimerJob(TimerJobs.newFixedRateJob("failed-timer",
                getProperties().getLogin().getFailedLockDuration(), timeout -> FAILED_ATTEMPTS_REPO.clear()));
    }

    /**
     * 认证处理类型 用于区分
     *
     * @return
     */
    @Override
    public String type() {
        return NAME;
    }

    /**
     * 处理认证请求
     *
     * @return
     * @throws AuthenticationException
     */
    @Override
    public SlardarAuthentication handleRequest(RequestWrapper requestWrapper) throws AuthenticationException {
        // 根据设置来确定是否需要启用验证码流程
        SlardarProperties properties = getProperties();
        CaptchaComponent captchaComponent = context.getBeanIfAvailable(CaptchaComponent.class);
        if (properties.getLogin().getCaptchaEnabled()) {
            String code = requestWrapper.getRequestParams().get("authCode");
            if (!StringUtils.hasLength(code)) {
                throw new AuthenticationServiceException("需要提供验证码[authCode]");
            }
            if (!captchaComponent.verify(requestWrapper.getSessionId(), code)) {
                throw new AuthenticationServiceException("验证码[authCode]无效");
            }
        }
        String username = requestWrapper.getRequestParams().get("username");
        String password = requestWrapper.getRequestParams().get("password");
        // 租户信息 默认为 master
        String realm = getRealm(requestWrapper);
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new AuthenticationServiceException("`username` and `password` should not be null");
        }
        return new SlardarAuthentication(username, Constants.AUTH_TYPE_NORMAL, null)
                .setRealm(realm)
                .setLoginDeviceType(requestWrapper.getLoginDeviceType())
                .setSessionId(requestWrapper.getSessionId())
                .setPassword(password);
    }

    /**
     * 子类实现
     *
     * @param authentication
     * @return
     */
    @Override
    protected SlardarAuthentication doAuthenticate0(SlardarAuthentication authentication) {
        SlardarUserDetailsServiceImpl userDetailsService = (SlardarUserDetailsServiceImpl) context.getBeanIfAvailable(UserDetailsService.class);
        PasswordEncoder passwordEncoder = context.getBeanOrDefault(PasswordEncoder.class, new BCryptPasswordEncoder());
        SlardarProperties properties = getProperties();
        // 用户密码方式认证
        String accountName = authentication.getAccountName();
        if (maxAttempts != null && maxAttempts > 0L) {
            if (stringCommands.exists(LOCKED_KEY.concat(accountName)) > 0L) {
                throw new AuthenticationServiceException("account is locked duo to login failed too many times");
            }
        }
        SlardarUserDetails userDetails = null;
        try {
            userDetails = userDetailsService.loadUserByAccount(accountName, authentication.getRealm());
        } catch (UsernameNotFoundException e) {
            triggerFailedLock(accountName);
            throw new AuthenticationServiceException(e.getLocalizedMessage());
        }
        String password = authentication.getPassword();
        if (properties.getLogin().getEncrypt().isEnabled()) {
            SlardarSpiFactory spiFactory = context.getBeanIfAvailable(SlardarSpiFactory.class);
            // 解密
            try {
                SlardarCrypto crypto = spiFactory.findCrypto(properties.getLogin().getEncrypt().getMode());
                password = crypto.decrypt(password);
            } catch (SlardarException e) {
                throw new AuthenticationServiceException(StrUtil.format("解密[{}]失败:{}", properties.getLogin().getEncrypt().getMode(),
                        e.getLocalizedMessage()));
            }
        }
        // 验证密码是否正确
        if (!Objects.isNull(userDetails)) {
            if (passwordEncoder.matches(password, userDetails.getPassword())) {
                try {
                    // 双因素认证
                    if (properties.getMfa().isEnabled()) {
                        SlardarMfaAuthService mfaAuthService = context.getBeanIfAvailable(SlardarMfaAuthService.class);
                        String key = mfaAuthService.generateAndDispatch(userDetails);
                        if (StrUtil.isBlank(key)) {
                            triggerFailedLock(accountName);
                            throw new AuthenticationServiceException("`MFA` key error");
                        }
                        throw new MfaVerifyRequiredException("MFA Authentication required!", key);
                    }
                    authentication.setUserDetails(userDetails).setAuthenticated(true);
                    return authentication;
                } catch (Exception e) {
                    triggerFailedLock(accountName);
                    if (e instanceof MfaVerifyRequiredException) {
                        throw (MfaVerifyRequiredException) e;
                    } else {
                        throw new AuthenticationServiceException(e.getLocalizedMessage());
                    }
                }
            } else {
                triggerFailedLock(accountName);
                throw new AuthenticationServiceException(String.format("账户 [%s] 密码不匹配", userDetails.getUsername()));
            }
        } else {
            triggerFailedLock(accountName);
            throw new UsernameNotFoundException("account not found");
        }
    }

    private void triggerFailedLock(String username) {
        if (maxAttempts != null && maxAttempts > 0L) {
            FAILED_POOL.submit(new FailedRunnable(username));
        }
    }


    class FailedRunnable implements Runnable {

        private final String username;

        FailedRunnable(String username) {
            this.username = username;
        }

        /**
         * 失败次数+1
         * 达到最大失败次数 触发锁定
         * 锁定解除时 清零失败次数
         * TODO: 每隔一段时间 清除所有记录 避免长时间累计
         */
        @Override
        public synchronized void run() {
            FAILED_ATTEMPTS_REPO.put(username, FAILED_ATTEMPTS_REPO.getOrDefault(username, 0) + 1);
            Integer failedTimes = FAILED_ATTEMPTS_REPO.get(username);
            if (failedTimes.equals(maxAttempts)) {
                // 触发锁定
                stringCommands.setex(LOCKED_KEY.concat(username),
                        getProperties().getLogin().getFailedLockDuration().getSeconds(), failedTimes.toString());
                FAILED_ATTEMPTS_REPO.remove(username);
            }
        }
    }

}
