package cn.alphabets.light.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Map;

/**
 * Singular 单个返回值，返回多个值的时候使用Plural
 *
 * Created by lilin on 2017/7/3.
 */
@JsonPropertyOrder(alphabetic = true)
public class Singular<T extends ModCommon> {

    private Long count;
    private T item;
    private Map<String, Map<String, ModCommon>> options;

    public Singular(T item) {
        this.item = item;
    }

    public Singular(Long count) {
        this.count = count;
    }

    public Singular(T item, Long count) {
        this.count = count;
        this.item = item;
    }

    public T getItem() {
        return this.item;
    }

    public Long getCount() {
        return this.count;
    }

    public Map<String, Map<String, ModCommon>> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Map<String, ModCommon>> options) {
        this.options = options;
    }

}
