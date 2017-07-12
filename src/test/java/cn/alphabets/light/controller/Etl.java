package cn.alphabets.light.controller;

import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.http.Context;

/**
 * Etl test
 * Created by lilin on 2017/7/12.
 */
public class Etl {

    public void initialize(Context handler, Model model){
        System.out.println("initialize");
    }

}
