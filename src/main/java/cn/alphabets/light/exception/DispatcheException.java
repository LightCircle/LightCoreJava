package cn.alphabets.light.exception;

/**
 * Created by luohao on 16/10/22.
 */
public class DispatcheException extends RuntimeException {
    public DispatcheException() {
    }

    public DispatcheException(String message) {
        super(message);
    }

    public DispatcheException(String message, Throwable cause) {
        super(message, cause);
    }

    public DispatcheException(Throwable cause) {
        super(cause);
    }

    public DispatcheException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
