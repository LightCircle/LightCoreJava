package cn.alphabets.light.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
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

    Object join() {
        return null;
    }

    Object fix() {

        return null;
    }

    Object numberFormat() {

        return null;
    }

    Object stringFormat() {

        return null;
    }

    Object dateFormat() {

        return null;
    }
}
