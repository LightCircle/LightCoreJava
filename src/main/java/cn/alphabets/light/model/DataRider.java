package cn.alphabets.light.model;


import cn.alphabets.light.db.mongo.Controller;
import cn.alphabets.light.http.Context;

import java.lang.reflect.ParameterizedType;

/**
 * DataRider
 * Created by lilin on 2016/11/12.
 */
public class DataRider {


    private String clazz;

    public DataRider(Class clazz) {
        this.clazz = clazz.getSimpleName().toLowerCase();
    }

    public DataRider(String clazz) {
        this.clazz = clazz.toLowerCase();
    }

    public String add(Context handler) {
        return null;
    }

    public String update() {
        return null;
    }

    public String remove() {
        return null;
    }

    public <T extends ModBase> Plural<T> list(Context handler) {
        return new Controller(handler, this.clazz).list();
    }

    public String search() {
        return null;
    }

    public <T extends ModBase> T get(Context handler) {
        return new Controller(handler, this.clazz).get();
    }

    public String count() {
        return null;
    }

    public String upsert() {
        return null;
    }
}
