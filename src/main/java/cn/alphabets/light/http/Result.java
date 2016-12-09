package cn.alphabets.light.http;

import cn.alphabets.light.Helper;
import cn.alphabets.light.entity.ModFile;
import cn.alphabets.light.exception.LightException;
import cn.alphabets.light.model.Error;
import cn.alphabets.light.model.Plural;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static io.vertx.core.http.HttpHeaders.*;

/**
 * Result
 * Created by lilin on 2016/11/12.
 */
public class Result {

    private Object data;
    private Error error;

    public Result(Object data) {
        if (data instanceof LightException) {
            LightException error = (LightException) data;
            this.error = new Error(error.getCode(), error.getMessage());
        } else {
            this.data = data;
        }
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

        if (this.data instanceof Plural) {
            if (((Plural) this.data).getStream() != null) {
                sendStream(context, (Plural<ModFile>) this.data);
                return;
            }
        }

        HttpServerResponse response = context.response();
        response.putHeader(CONTENT_TYPE, "application/json; charset=utf-8");
        response.end(this.json());
    }

    public void sendStream(RoutingContext context, Plural<ModFile> plural) {
        OutputStream stream = plural.getStream();
        ModFile meta = plural.getItems().get(0);
        HttpServerResponse response = context.response();

        response.putHeader(CONTENT_TYPE, meta.getContentType());
        response.putHeader(CONTENT_LENGTH, String.valueOf(meta.getLength()));
        response.putHeader(LAST_MODIFIED, Helper.toUTCString(meta.getUpdateAt()));
        response.putHeader(ACCEPT_RANGES, "bytes");
        response.putHeader(CACHE_CONTROL, "public, max-age=34560000");

        response.write(Buffer.buffer(((ByteArrayOutputStream) stream).toByteArray()));
        response.end();
    }

    public static void redirect(RoutingContext context, String path) {
        HttpServerResponse response = context.response();
        response.putHeader("Location", path);
        response.setStatusCode(HttpResponseStatus.FOUND.code());
        response.end();
    }
}
