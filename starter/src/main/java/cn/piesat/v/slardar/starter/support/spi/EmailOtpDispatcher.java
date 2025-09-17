package cn.piesat.v.slardar.starter.support.spi;

import cn.piesat.v.misc.hutool.mini.StringUtil;
import cn.piesat.v.slardar.core.SlardarException;
import cn.piesat.v.slardar.core.entity.Account;
import cn.piesat.v.slardar.core.entity.UserProfile;
import cn.piesat.v.slardar.spi.SlardarSpiContext;
import cn.piesat.v.slardar.spi.mfa.SlardarOtpDispatcher;
import cn.piesat.v.slardar.spi.mfa.OtpDispatchResult;
import cn.piesat.v.slardar.starter.config.SlardarProperties;
import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Objects;

/**
 * <p>
 * email 发送 otp code
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/29
 */
@AutoService(SlardarOtpDispatcher.class)
public class EmailOtpDispatcher implements SlardarOtpDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(EmailOtpDispatcher.class);

    public static final String MODE = "email";

    private JavaMailSenderImpl mailSender;


    static {
        System.getProperties().setProperty("mail.mime.splitlongparameters", "false");
    }

    /**
     * 发送模式
     * - email
     * - sms
     * - ...
     *
     * @return
     */
    @Override
    public String name() {
        return MODE;
    }

    /**
     * set context
     *
     * @param context
     */
    @Override
    public void initialize(SlardarSpiContext context) {
        // 这里读取email 配置
        SlardarProperties properties = context.getBeanIfAvailable(SlardarProperties.class);
        SlardarProperties.MfaSetting mfa = properties.getMfa();
        SlardarProperties.EmailSetting emailSetting = mfa.getEmail();
        if (emailSetting != null) {
            mailSender = new JavaMailSenderImpl();
            emailSetting.applyProperties(mailSender);
        }
    }

    /**
     * 发布 code
     *
     * @param otpCode
     * @throws SlardarException
     */
    @Override
    public OtpDispatchResult dispatch(String otpCode, Account account) throws SlardarException {
        if (Objects.isNull(mailSender)) {
            throw new SlardarException("未找到任何发送者配置信息");
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            UserProfile userProfile = account.getUserProfile();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(Objects.requireNonNull(mailSender.getUsername()));
            helper.setTo(userProfile.getEmail());
            helper.setSubject(emailSubject(account));
            helper.setText(emailText(otpCode, account), true);
            mailSender.send(mimeMessage);
            return OtpDispatchResult.ofSuccess(otpCode, MODE, "邮件发送成功");

        } catch (MessagingException e) {
            logger.error(e.getLocalizedMessage());
            return OtpDispatchResult.ofFailure(otpCode, MODE, e);
        }
    }

    private String emailSubject(Account account) {
        return StringUtil.format("[slardar] OTP for logging in to your account: {}", account.getName());
    }

    private String emailText(String otpCode, Account account) {
        return StringUtil.format("<p>Hi, <strong>{}</strong></p> <p> 看起来你正试图使用你的用户名和密码登录。" +
                        "作为一项额外的安全措施(双因素认证)，您需要输入此电子邮件中提供的OTP代码（一次性密码）</p> <p> The OTP code is: <strong> {} </strong> <br> 有效期 <strong>5</strong> 分钟 </p>",
                account.getName(), otpCode);
    }

}
