package cn.alphabets.light.model;

import java.util.List;

/**
 * Result
 * Created by lilin on 2016/11/12.
 */
public class Result <T extends ModBase> {

    private String apiVersion = "1.0";
    private Data<T> data;
    private Error error;

    public  Result(Long total, List<T> items) {
        this.data = new Data<>(total, items);
    }

    public Result(String code, String message) {
        this.error = new Error(code, message);
    }


    public List<T> getItems() {
        return this.data.getItems();
    }

    public Long getTotalItems() {
        return this.data.getTotalItems();
    }


    public static class Data <T extends ModBase> {
        public  Data(Long total, List<T> items) {
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

    public static class Error {
        public Error(String code, String message) {
            this.code = code;
            this.message = message;
        }
        private String code;
        private String message;
        private String errors;
    }
}
