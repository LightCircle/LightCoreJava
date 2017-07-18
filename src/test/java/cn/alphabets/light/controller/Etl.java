package cn.alphabets.light.controller;

import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.http.Context;
import org.bson.Document;

import java.util.List;

/**
 * Etl test
 * Created by lilin on 2017/7/12.
 */
public class Etl {

    public void initialize(Context handler, Model model) {
        System.out.println("initialize");
    }

    public void before(Context handler, List<Document> data) {
        System.out.println("before");
    }

    public void parse(Context handler, Document data) {
        System.out.println("parse");
    }

    public List<Document> valid(Context handler, Document data) {
        System.out.println("valid");
        return null;
    }

    public void after(Context handler, Document data) {
        System.out.println("after");
    }

    public void dump(Context handler, List<Document> data) {
        System.out.println("dump");
    }
}
