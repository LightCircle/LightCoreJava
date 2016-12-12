package cn.alphabets.light.exception;

import cn.alphabets.light.entity.ModBoard;

/**
 * Created by luohao on 2016/12/1.
 */
public class DataRiderException extends RuntimeException {

    public static DataRiderException BoardNotFound(String boardAPI) {
        return new DataRiderException(String.format("Board not found for api : %s", boardAPI));
    }

    public static DataRiderException ParameterUnsatisfied(String parameter) {
        return new DataRiderException(String.format("Parameter unsatisfied : %s", parameter));
    }

    public static DataRiderException ParameterUnsatisfied(String parameter, Throwable throwable) {
        return new DataRiderException(String.format("Parameter unsatisfied : %s", parameter), throwable);
    }

    public static DataRiderException EntityClassNotFound(String clazz, ModBoard board) {
        return new DataRiderException(String.format("Can not found class : %s , board : %s", board.toDocument().toJson()));
    }

    public static DataRiderException ControllerMethodCallFailed(String methodName, Throwable throwable) {
        return new DataRiderException(String.format("Controller method call failed, method: %s", methodName), throwable);
    }

    private DataRiderException(String message) {
        super(message);
    }

    private DataRiderException(String message, Throwable cause) {
        super(message, cause);
    }
}