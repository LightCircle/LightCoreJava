package cn.alphabets.light.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.List;

/**
 * PluralData
 * Created by lilin on 2016/11/13.
 */
@JsonPropertyOrder(alphabetic = true)
public class Plural<T extends ModCommon> {

    private T item;
    private Long totalItems;
    private List<T> items;
    private HashMap<String, HashMap<String, ModCommon>> options;

    public Plural(List<T> items) {
        this((long) items.size(), items, null);
    }

    public Plural(Long totalItems, List<T> items) {
        this(totalItems, items, null);
    }

    public Plural(Long totalItems, List<T> items, HashMap<String, HashMap<String, ModCommon>> options) {
        this.totalItems = totalItems;
        this.items = items;
        this.options = options;
    }

    public Long getTotalItems() {
        return totalItems;
    }

    public List<T> getItems() {
        return items;
    }

    public T getItem() {
        return item;
    }

    public HashMap<String, HashMap<String, ModCommon>> getOptions() {
        return options;
    }

    public void setOptions(HashMap<String, HashMap<String, ModCommon>> options) {
        this.options = options;
    }

}
