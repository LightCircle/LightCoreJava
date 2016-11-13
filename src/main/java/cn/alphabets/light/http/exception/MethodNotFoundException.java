package cn.alphabets.light.http.exception;

/**
 * MethodNotFoundException
 * Created by luohao on 16/10/22.
 */
public class MethodNotFoundException extends RuntimeException {
    public MethodNotFoundException(String message) {
        super(message);
    }
}
