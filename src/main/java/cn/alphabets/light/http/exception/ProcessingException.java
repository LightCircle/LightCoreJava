package cn.alphabets.light.http.exception;

/**
 * Created by luohao on 2016/10/28.
 */
public class ProcessingException extends RuntimeException {
    public ProcessingException() {
    }

    public ProcessingException(String message) {
        super(message);
    }

    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessingException(Throwable cause) {
        super(cause);
    }

    public ProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
