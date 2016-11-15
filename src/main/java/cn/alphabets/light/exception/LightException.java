package cn.alphabets.light.exception;

/**
 * LightException
 * Created by lilin on 2016/11/15.
 */
public class LightException extends Exception {
    private String code;
    private String message;

    public LightException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
