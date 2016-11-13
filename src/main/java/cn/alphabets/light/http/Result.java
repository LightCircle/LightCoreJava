package cn.alphabets.light.http;

import cn.alphabets.light.model.Error;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

/**
 * Result
 * Created by lilin on 2016/11/12.
 */
public class Result {

    private Object data;
    private Error error;

    public Result(Object data) {
        this.data = data;
    }

    public Result(String code, String message) {
        this.error = new Error(code, message);
    }

    public String getApiVersion() {
        return "1.0";
    }

    public Object getData() {
        return this.data;
    }

    public Error getError() {
        return this.error;
    }

    public String json() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.putHeader(CONTENT_TYPE, "application/json; charset=utf-8");
        response.end(this.json());
    }
}
