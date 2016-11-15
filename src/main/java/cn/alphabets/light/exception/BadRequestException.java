package cn.alphabets.light.exception;

/**
 * BadRequestException
 * Created by lilin on 2016/11/15.
 */
public class BadRequestException extends LightException {
    public BadRequestException(String message) {
        super("400", message);
    }
}
