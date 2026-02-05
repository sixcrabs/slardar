package io.github.sixcrabs.slardar.starter.authenticate.handler.impl;

import org.winterfell.misc.hutool.mini.StringUtil;
import org.winterfell.misc.timer.TimerManager;
import org.winterfell.misc.timer.job.TimerJobs;
import io.github.sixcrabs.slardar.core.Constants;
import io.github.sixcrabs.slardar.core.SlardarException;
import io.github.sixcrabs.slardar.spi.SlardarKeyStore;
import io.github.sixcrabs.slardar.core.SlardarContext;
import io.github.sixcrabs.slardar.spi.SlardarSpiFactory;
import io.github.sixcrabs.slardar.spi.crypto.SlardarCrypto;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarUserDetails;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarUserDetailsServiceImpl;
import io.github.sixcrabs.slardar.starter.authenticate.SlardarAuthentication;
import io.github.sixcrabs.slardar.starter.authenticate.handler.AbstractSlardarAuthenticateHandler;
import io.github.sixcrabs.slardar.starter.authenticate.handler.SlardarAuthenticateHandler;
import io.github.sixcrabs.slardar.starter.authenticate.mfa.MfaVerifyRequiredException;
import io.github.sixcrabs.slardar.starter.authenticate.mfa.SlardarMfaAuthService;
import io.github.sixcrabs.slardar.starter.SlardarProperties;
import io.github.sixcrabs.slardar.starter.support.RequestWrapper;
import io.github.sixcrabs.slardar.starter.support.SlardarAuthenticationException;
import io.github.sixcrabs.slardar.starter.support.CaptchaComponent;
import com.google.auto.service.AutoService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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

    private static final String LOCKED_KEY = "slardar:locked_";

    private static final Map<String, Integer> FAILED_ATTEMPTS_REPO = new WeakHashMap<>(1);

    private SlardarKeyStore keyStore;

    private Integer maxAttempts = 5;

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
    public void initialize(SlardarContext context) {
        super.initialize(context);
        SlardarSpiFactory spiFactory = context.getBean(SlardarSpiFactory.class);
        keyStore = spiFactory.findKeyStore(getProperties().getKeyStore().getType());
        maxAttempts = getProperties().getLogin().getMaxAttemptsBeforeLocked();
        // 定时器
        TimerManager timerManager = new TimerManager();
        timerManager.addTimerJob(TimerJobs.newFixedRateJob("failed-timer",
                getProperties().getLogin().getFailedLockDuration(), timeout -> FAILED_ATTEMPTS_REPO.clear()));
        timerManager.start();
    }

    /**
     * do destroy
     */
    @Override
    public void destroy() {
        FAILED_POOL.shutdown();
    }

    /**
     * 认证处理类型 用于区分
     *
     * @return
     */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * 处理认证请求
     *
     * @return
     * @throws AuthenticationException
     */
    @Override
    public SlardarAuthentication handleRequest(RequestWrapper requestWrapper) throws SlardarAuthenticationException {
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
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
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
            Integer lockedTimes = keyStore.get(LOCKED_KEY.concat(accountName));
            if (lockedTimes != null && lockedTimes > 0L) {
                throw new SlardarAuthenticationException("Your account has been locked due to login failed too many times", accountName);
            }
        }
        SlardarUserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByAccount(accountName, authentication.getRealm());
        } catch (UsernameNotFoundException e) {
            triggerFailedLock(accountName);
            throw new SlardarAuthenticationException(e.getLocalizedMessage(), accountName);
        }
        String password = authentication.getPassword();
        if (properties.getLogin().getEncrypt().isEnabled()) {
            SlardarSpiFactory spiFactory = context.getBeanIfAvailable(SlardarSpiFactory.class);
            // 解密
            try {
                SlardarCrypto crypto = spiFactory.findCrypto(properties.getLogin().getEncrypt().getMode());
                password = crypto.decrypt(password);
            } catch (SlardarException e) {
                throw new SlardarAuthenticationException(StringUtil.format("解密[{}]失败:{}", properties.getLogin().getEncrypt().getMode(),
                        e.getLocalizedMessage()), accountName);
            }
        }
        // 验证密码是否正确
        if (!Objects.isNull(userDetails)) {
            if (passwordEncoder.matches(password, userDetails.getPassword())) {
                try {
                    //TBD: 双因素认证
                    if (properties.getMfa().isEnabled()) {
                        SlardarMfaAuthService mfaAuthService = context.getBeanIfAvailable(SlardarMfaAuthService.class);
                        String key = mfaAuthService.generateAndDispatch(userDetails);
                        if (StringUtil.isBlank(key)) {
                            triggerFailedLock(accountName);
                            throw new SlardarAuthenticationException("`MFA` key error", accountName);
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
                        throw new SlardarAuthenticationException(e.getLocalizedMessage(), accountName);
                    }
                }
            } else {
                triggerFailedLock(accountName);
                throw new SlardarAuthenticationException(String.format("账户 [%s] 密码不匹配", userDetails.getUsername()), accountName);
            }
        } else {
            triggerFailedLock(accountName);
            throw new UsernameNotFoundException(StringUtil.format("account [{}] not found", accountName));
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
                keyStore.setex(LOCKED_KEY.concat(username), failedTimes, getProperties().getLogin().getFailedLockDuration().getSeconds());
                FAILED_ATTEMPTS_REPO.remove(username);
            }
        }
    }

}