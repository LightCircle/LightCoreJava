package cn.alphabets.light.model;

import java.util.List;

/**
 * PluralData
 * Created by lilin on 2016/11/13.
 */
public class Plural<T extends ModCommon> {
    public Plural(Long total, List<T> items) {
        this.totalItems = total;
        this.items = items;
    }

    public Long getTotalItems() {
        return totalItems;
    }

    public List<T> getItems() {
        return items;
    }

    private Long totalItems;
    private List<T> items;
}
