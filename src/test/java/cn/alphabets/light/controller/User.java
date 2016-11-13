package cn.alphabets.light.controller;

import cn.alphabets.light.http.Context;
import cn.alphabets.light.model.Plural;

import java.util.Arrays;

/**
 * User
 * Created by lilin on 2016/11/13.
 */
public class User {

    public Plural<cn.alphabets.light.entity.User> list(Context handler) {

        cn.alphabets.light.entity.User user1 = new cn.alphabets.light.entity.User();
        cn.alphabets.light.entity.User user2 = new cn.alphabets.light.entity.User();

        user1.setName("user1");
        user2.setName("user2");

        return new Plural<>(2L, Arrays.asList(user1, user2));
    }
}
