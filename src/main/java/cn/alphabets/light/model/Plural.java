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

    public Plural(List<T> items) {
        this((long) items.size(), items, null);
    }

    public Plural(Long totalItems, List<T> items) {
        this(totalItems, items, null);
    }

    public Plural(Long totalItems, List<T> items, HashMap<String, HashMap<String, ? super ModCommon>> options) {
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

    public HashMap<String, HashMap<String, ? super ModCommon>> getOptions() {
        return options;
    }

    public void setOptions(HashMap<String, HashMap<String, ? super ModCommon>> options) {
        this.options = options;
    }

    private Long totalItems;
    private List<T> items;
    private HashMap<String, HashMap<String, ? super ModCommon>> options;
}
