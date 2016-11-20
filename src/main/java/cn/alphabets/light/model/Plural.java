package cn.alphabets.light.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.OutputStream;
import java.util.List;

/**
 * PluralData
 * Created by lilin on 2016/11/13.
 */
public class Plural<T extends ModCommon> {
    public Plural(Long total, List<T> items) {
        this(total, items, null);
    }

    public Plural(Long total, List<T> items, OutputStream stream) {
        this.totalItems = total;
        this.items = items;
        this.stream = stream;
    }

    public Long getTotalItems() {
        return totalItems;
    }

    public List<T> getItems() {
        return items;
    }

    public OutputStream getStream() {
        return this.stream;
    }

    @JsonIgnore
    private OutputStream stream;
    private Long totalItems;
    private List<T> items;
}
