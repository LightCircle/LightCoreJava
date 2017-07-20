package cn.alphabets.light.model;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.entity.ModFunction;
import cn.alphabets.light.http.Context;

import java.util.List;

/**
 * Menu
 * Created by lilin on 2017/7/20.
 */
public class Menu {

    public Plural<ModFunction> list(Context handler) {

        List<ModFunction> menu = CacheManager.INSTANCE.getFunctions();
        return new Plural<>((long) menu.size(), menu);
    }

}
