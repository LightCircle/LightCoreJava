package cn.alphabets.light.validator;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Sanitize
 * Created by lilin on 2017/7/11.
 */
public class Sanitize {


    Object split(Object data, Map<String, Object> sanitize) {

        if (!(data instanceof String)) {
            return data;
        }

        String value = (String) data;
        if (StringUtils.isEmpty(value)) {
            return data;
        }

        return Arrays.asList(value.split((String) sanitize.get("option")));
    }

    Object join(Object data, Map<String, Object> sanitize) {
        if (!(data instanceof List)) {
            return data;
        }

        return String.join(",", (List) data);
    }

    Object fix(Object data, Map<String, Object> sanitize) {
        return sanitize.get("option");
    }

    Object numberFormat(Object data, Map<String, Object> sanitize) {
        if (!(data instanceof String)) {
            return data;
        }

        String pattern = (String) sanitize.get("option");
        return new DecimalFormat(pattern).format(data);
    }

    Object stringFormat(Object data, Map<String, Object> sanitize) {
        if (!(data instanceof String)) {
            return data;
        }

        String pattern = (String) sanitize.get("option");
        return String.format(pattern, data);
    }

    Object dateFormat(Object data, Map<String, Object> sanitize) {
        if (!(data instanceof Date)) {
            return data;
        }

        String pattern = (String) sanitize.get("option");
        return new SimpleDateFormat(pattern).format(data);
    }
}
