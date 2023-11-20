package cn.piesat.nj.slardar.spi.mfa;


/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/8/29
 */
public class OtpDispatchResult {

    private boolean isSucceeded;

    private boolean isStopRetry;

    private String otpCode;

    private String otpMode;

    private String msg;


    public static OtpDispatchResult ofSuccess(String otpCode, String mode, String msg) {
        return new OtpDispatchResult().setOtpCode(otpCode).setOtpMode(mode).setMsg(msg).setSucceeded(true);
    }

    public static OtpDispatchResult ofFailure(String otpCode, String mode, Exception ex) {
        return new OtpDispatchResult().setOtpCode(otpCode).setOtpMode(mode).setMsg(ex.getLocalizedMessage()).setSucceeded(false);
    }


    public String getMsg() {
        return msg;
    }

    public OtpDispatchResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getOtpMode() {
        return otpMode;
    }

    public OtpDispatchResult setOtpMode(String otpMode) {
        this.otpMode = otpMode;
        return this;
    }

    public boolean isSucceeded() {
        return isSucceeded;
    }

    public OtpDispatchResult setSucceeded(boolean succeeded) {
        isSucceeded = succeeded;
        return this;
    }

    public boolean isStopRetry() {
        return isStopRetry;
    }

    public OtpDispatchResult setStopRetry(boolean stopRetry) {
        isStopRetry = stopRetry;
        return this;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public OtpDispatchResult setOtpCode(String otpCode) {
        this.otpCode = otpCode;
        return this;
    }
}
