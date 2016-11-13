package cn.alphabets.light.model;

/**
 * ErrorData
 * Created by lilin on 2016/11/13.
 */
public class Error {
    public Error(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private String code;
    private String message;
    private String errors;
}
