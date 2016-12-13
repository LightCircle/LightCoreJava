package cn.alphabets.light.model.datarider;

import cn.alphabets.light.Helper;
import cn.alphabets.light.exception.DataRiderException;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by luohao on 2016/12/3.
 */
public class TypeConvertor {
    private static final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(TypeConvertor.class);

    private DBParams params;
    private Map<String, Function> typeConverts = new ConcurrentHashMap<String, Function>() {{

        put("string", o -> {
            if (o == null) {
                return null;
            }
            return o.toString();
        });

        put("boolean", o -> {

            if (o == null) {
                return null;
            }

            if (o instanceof Boolean) {
                return o;
            }
            if ("1".equals(o) || "true".equals(o)) {
                return true;
            }
            if ("0".equals(o) || "false".equals(o)) {
                return false;
            }

            if (o.equals(1)) {
                return true;
            }
            if (o.equals(0)) {
                return false;
            }
            throw DataRiderException.ParameterUnsatisfied(o.toString() + " -> boolean");
        });

        put("number", o -> {
            if (o == null) {
                return null;
            }

            if (o instanceof Number) {
                return o;
            }

            if (o instanceof String) {
                try {
                    String s = (String) o;
                    if (s.contains(".")) {
                        return Float.parseFloat(s);
                    } else {
                        return Long.parseLong(s);
                    }
                } catch (NumberFormatException e) {
                    throw DataRiderException.ParameterUnsatisfied(o.toString() + " -> number", e);
                }
            }
            throw DataRiderException.ParameterUnsatisfied(o.toString() + " -> number");
        });

        put("date", o -> {
            if (o == null) {
                return null;
            }

            if (o instanceof Date) {
                return o;
            }

            if (o instanceof Long) {
                return new Date((Long) o);
            }

            if (o instanceof String) {
                try {
                    return Helper.fromSupportedString((String) o, params.getHandler().getTimeZone());
                } catch (Exception e) {
                }
            }

            throw DataRiderException.ParameterUnsatisfied(o.toString() + " -> date");
        });

        put("objectid", o -> {
            if (o == null) {
                return null;
            }

            if (o instanceof ObjectId) {
                return o;
            }

            if (o instanceof String) {
                if (StringUtils.isEmpty((String) o)) {
                    return null;
                }
                return new ObjectId((String) o);
            }

            throw DataRiderException.ParameterUnsatisfied(o.toString() + " -> objectid");
        });

        put("object", o -> o);
        put("array", o -> o);

    }};

    public TypeConvertor(DBParams params) {
        this.params = params;
    }

    public Object convert(String valueType, Object value) {

        if (Arrays.asList("string", "boolean", "number", "date", "objectid")
                .contains(valueType)
                && value instanceof List) {
            return ((List) value).stream().map(item -> convert(valueType, item)).collect(Collectors.toList());
        }

        return this.typeConverts.get(valueType).apply(value);
    }
}
