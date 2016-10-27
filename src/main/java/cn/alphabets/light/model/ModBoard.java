package cn.alphabets.light.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by luohao on 16/10/22.
 */
public class ModBoard extends ModBase {

    private String schema;
    private String api;
    private int type;
    private int kind;
    private String path;
    @JsonProperty("class")
    private String class_;
    private String action;
    private String description;
    private String reserved;

    public String getSchema() {
        return schema;
    }

    public String getApi() {
        return api;
    }

    public int getType() {
        return type;
    }

    public int getKind() {
        return kind;
    }

    public String getPath() {
        return path;
    }

    public String getClass_() {
        return class_;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public String getReserved() {
        return reserved;
    }

}
