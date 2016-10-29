package cn.alphabets.light.model;

import java.util.HashMap;

/**
 * Created by luohao on 2016/10/29.
 */
public class ModI18n extends ModBase {
    private String key;
    private String type;
    private HashMap<String, String> lang;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HashMap<String, String> getLang() {
        return lang;
    }

    public void setLang(HashMap<String, String> lang) {
        this.lang = lang;
    }
}
