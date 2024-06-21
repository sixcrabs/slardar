package cn.piesat.nj.slardar.starter.handler;

import cn.piesat.nj.slardar.core.AccountInfoDTO;
import cn.piesat.nj.slardar.spi.SlardarSpiContext;
import cn.piesat.nj.slardar.spi.authenticate.SlardarAuthenticateResultAdapter;
import cn.piesat.nj.slardar.starter.config.SlardarProperties;
import cn.piesat.nj.slardar.starter.support.HttpServletUtil;
import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;
import java.util.Objects;

import static cn.piesat.nj.slardar.starter.support.HttpServletUtil.makeErrorResult;

/**
 * <p>
 * 默认的 认证结果处理 适配器
 * 应用集成可以参考此实现进行认证成功、失败、拒绝的返回结果的定制
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/12/21
 */
@AutoService(SlardarAuthenticateResultAdapter.class)
public class SlardarDefaultAuthenticateResultAdapter implements SlardarAuthenticateResultAdapter {
    public static final String NAME = "default";

    protected SlardarProperties slardarProperties;

    public static final Logger logger = LoggerFactory.getLogger(SlardarDefaultAuthenticateResultAdapter.class);

    /**
     * 实现名称, 区分不同的 SPI 实现，必须
     *
     * @return
     */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void initialize(SlardarSpiContext context) {
        slardarProperties = context.getBeanIfAvailable(SlardarProperties.class);
    }

    /**
     * 认证成功结果
     *
     * @param accountInfoDTO
     * @return
     */
    @Override
    public Map<String, Object> authSucceedResult(AccountInfoDTO accountInfoDTO) {
        return HttpServletUtil.makeResult(accountInfoDTO, slardarProperties.getLogin().getLoginSuccessCode(), "success");
    }

    /**
     * 认证失败结果
     *
     * @param exception
     * @return
     */
    @Override
    public Map<String, Object> authFailedResult(RuntimeException exception) {
        String errMsg = exception.getLocalizedMessage();
        logger.error("Authentication failed：{}", errMsg);
        HttpStatus status = (exception instanceof AuthenticationServiceException || exception instanceof UsernameNotFoundException) ?
                HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.UNAUTHORIZED;
        return makeErrorResult(Objects.isNull(errMsg) ? "Null" : errMsg, status.value());
    }

    /**
     * 无权限访问结果
     *
     * @param exception
     * @return
     */
    @Override
    public Map<String, Object> authDeniedResult(RuntimeException exception) {
        return makeErrorResult(exception.getLocalizedMessage(), 403);
    }
}
